package de.sprachgefuehl.app.data.account

import android.content.Context
import android.content.Intent
import de.sprachgefuehl.app.data.Progress
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.security.MessageDigest
import java.time.Instant

/**
 * Owns the optional Supabase session and the cloud copy of the local progress document.
 * Local progress is always the source the app renders. This class never deletes local data
 * on its own; it uploads, downloads or asks the learner which side wins.
 */
class CloudAccountRepository(
    private val context: Context,
    private val progressProvider: () -> Progress,
    private val importProgress: (Progress) -> Boolean,
    /** Räumt den lokalen Lernstand ab. Abmelden heißt: Das Gerät gehört wieder niemandem. */
    private val clearLocalProgress: () -> Unit = {},
    private val autoSync: Boolean = true,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val prefs = context.getSharedPreferences("cloud_account", Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client: SupabaseClient? = if (SupabaseConfig.configured) {
        createSupabaseClient(SupabaseConfig.url, SupabaseConfig.anonKey) {
            install(Auth) {
                scheme = "sprachgefuehl"
                host = "auth"
            }
            install(Postgrest)
            install(Storage)
            install(Functions)
        }
    } else {
        null
    }

    private val _state = MutableStateFlow(
        AccountUiState(
            configured = SupabaseConfig.configured,
            phase = if (SupabaseConfig.configured) SyncPhase.READY else SyncPhase.LOCAL_ONLY,
            syncEnabled = prefs.getBoolean(KEY_SYNC_ENABLED, true)
        )
    )
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    private var syncJob: Job? = null

    init {
        if (client != null) {
            scope.launch {
                runCatching { client.auth.awaitInitialization() }
                refreshUser()
                client.auth.sessionStatus.collect { refreshUser() }
            }
        }
    }

    fun register(email: String, password: String) {
        val checked = validateCredentials(email, password) ?: return
        val active = client ?: return
        _state.update { it.copy(email = checked.first) }
        runAction {
            active.auth.signUpWith(Email, redirectUrl = SupabaseConfig.AUTH_REDIRECT) {
                this.email = checked.first
                this.password = checked.second
            }
            refreshUser()
            _state.update {
                it.copy(
                    busy = false,
                    message = "Fast fertig. Wir haben dir eine Bestätigungsmail geschickt. " +
                        "Erst nach dem Klick ist die Cloud-Sicherung freigeschaltet."
                )
            }
        }
    }

    fun login(email: String, password: String) {
        val checked = validateCredentials(email, password) ?: return
        val active = client ?: return
        _state.update { it.copy(email = checked.first) }
        runAction {
            active.auth.signInWith(Email) {
                this.email = checked.first
                this.password = checked.second
            }
            refreshUser()
            _state.update { it.copy(busy = false, message = "Angemeldet.") }
        }
    }

    fun logout() {
        val active = client ?: return
        scope.launch {
            _state.update { it.copy(busy = true, error = null) }
            val user = runCatching { active.auth.currentUserOrNull() }.getOrNull()
            // Letzte Gelegenheit: Was lokal noch nicht gesichert ist, geht vorher in die Cloud.
            // Scheitert das (offline), wird trotzdem abgemeldet – der Dialog davor sagt das an.
            runCatching { syncInternal() }
            // Abmelden muss auch dann gelingen, wenn der Server nicht antwortet. Sonst bliebe die
            // Sitzung auf dem Gerät und der Nutzer wäre sichtbar weiter im Konto.
            val failure = runCatching { active.auth.signOut() }.exceptionOrNull()
            if (failure != null) runCatching { active.auth.clearSession() }
            // Der Fortschritt hängt am Konto: lokal bleibt nichts zurück. Beim nächsten Anmelden
            // kommt er aus der Cloud – ohne Hash also als Download, nicht als Konflikt.
            clearLocalProgress()
            if (user != null) clearSyncMetadata(user.id)
            prefs.edit().remove(KEY_OWNER_UID).remove(KEY_OWNER_EMAIL).apply()
            _state.update {
                it.copy(
                    busy = false,
                    signedIn = false,
                    email = null,
                    emailVerified = false,
                    phase = SyncPhase.LOCAL_ONLY,
                    conflict = null,
                    localOwner = null,
                    lastSyncAt = null,
                    isAdmin = false,
                    adminUsers = emptyList(),
                    adminLoaded = false,
                    adminError = null,
                    adminNotice = null,
                    error = null,
                    message = "Abgemeldet. Der Fortschritt auf diesem Gerät wurde entfernt – in der " +
                        "Cloud bleibt er. Melde dich wieder an, um weiterzumachen."
                )
            }
        }
    }

    fun resendConfirmation() {
        val active = client ?: return
        val email = _state.value.email ?: return
        runAction {
            active.auth.resendEmail(OtpType.Email.SIGNUP, email)
            _state.update { it.copy(busy = false, message = "Bestätigungsmail ist erneut unterwegs.") }
        }
    }

    fun sendPasswordReset(email: String? = _state.value.email) {
        val active = client ?: return
        val target = email?.trim()?.takeIf { it.isNotBlank() } ?: return
        runAction {
            active.auth.resetPasswordForEmail(target, redirectUrl = SupabaseConfig.AUTH_REDIRECT)
            _state.update { it.copy(busy = false, message = "Mail zum Zurücksetzen ist unterwegs.") }
        }
    }

    fun updatePassword(password: String) {
        if (password.length < 8) {
            _state.update { it.copy(error = "Das Passwort braucht mindestens 8 Zeichen.") }
            return
        }
        val active = client ?: return
        runAction {
            active.auth.updateUser { this.password = password }
            _state.update {
                it.copy(
                    busy = false,
                    passwordRecovery = false,
                    message = "Passwort geändert."
                )
            }
        }
    }

    /** Handles the redirect from the confirmation or password-reset mail. */
    fun handleAuthIntent(intent: Intent?) {
        val active = client ?: return
        val data = intent?.data ?: return
        val recovery = data.getQueryParameter("type") == "recovery" ||
            data.fragment?.contains("type=recovery") == true
        if (recovery) _state.update { it.copy(passwordRecovery = true) }
        scope.launch {
            runCatching { active.handleDeeplinks(intent) }
                .onFailure { error -> _state.update { it.copy(error = readableError(error)) } }
            refreshUser()
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
        _state.update { it.copy(syncEnabled = enabled) }
        if (enabled) syncNow()
    }

    fun syncNow() {
        if (client == null || !_state.value.syncEnabled) return
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            try {
                syncInternal()
            } finally {
                syncJob = null
            }
        }
    }

    /** Used by the background worker, which has to wait for the result. */
    suspend fun syncNowAndWait() {
        val active = client ?: return
        if (!_state.value.syncEnabled) return
        runCatching { active.auth.awaitInitialization() }
        syncInternal()
    }

    fun close() {
        scope.cancel()
    }

    /** The learner picked a side after a conflict. */
    fun resolveConflict(useCloud: Boolean) {
        val active = client ?: return
        val conflict = _state.value.conflict ?: return
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            _state.update { it.copy(busy = true, phase = SyncPhase.SYNCING, error = null) }
            try {
                val user = active.auth.currentUserOrNull()
                    ?: error("Die Sitzung ist abgelaufen. Bitte melde dich neu an.")
                if (user.emailConfirmedAt == null) error("Bitte bestätige zuerst deine E-Mail-Adresse.")
                if (useCloud) {
                    val cloudProgress = decodeProgress(conflict.cloud)
                        ?: error("Der Cloud-Stand konnte nicht gelesen werden.")
                    val prepared = downloadRecordings(user.id, cloudProgress)
                    if (!importProgress(prepared)) {
                        error("Der Cloud-Stand konnte nicht gespeichert werden.")
                    }
                    markSynced(user.id, conflict.cloud.revision, hashOf(progressProvider()))
                    rememberOwner(user.id)
                    _state.update {
                        it.copy(
                            busy = false,
                            phase = SyncPhase.SYNCED,
                            conflict = null,
                            lastSyncAt = System.currentTimeMillis(),
                            message = "Der Cloud-Stand wurde auf dieses Gerät geladen."
                        )
                    }
                } else {
                    pushLocal(
                        userId = user.id,
                        progress = progressProvider(),
                        expectedRevision = conflict.cloud.revision,
                        force = true
                    )
                    uploadRecordings(user.id)
                    rememberOwner(user.id)
                    _state.update {
                        it.copy(
                            busy = false,
                            phase = SyncPhase.SYNCED,
                            conflict = null,
                            lastSyncAt = System.currentTimeMillis(),
                            message = "Dieses Gerät wurde in die Cloud übernommen."
                        )
                    }
                }
            } catch (error: Throwable) {
                if (isNetworkError(error)) SyncScheduler.enqueue(context)
                _state.update {
                    it.copy(
                        busy = false,
                        phase = if (isNetworkError(error)) SyncPhase.OFFLINE else SyncPhase.ERROR,
                        error = readableError(error)
                    )
                }
            } finally {
                syncJob = null
            }
        }
    }

    fun deleteAccount() {
        val active = client ?: return
        runAction {
            active.functions.invoke("delete-account")
            val userId = active.auth.currentUserOrNull()?.id
            active.auth.signOut()
            if (userId != null) clearSyncMetadata(userId)
            _state.update {
                it.copy(
                    busy = false,
                    signedIn = false,
                    email = null,
                    emailVerified = false,
                    phase = SyncPhase.LOCAL_ONLY,
                    conflict = null,
                    passwordRecovery = false,
                    message = "Konto und Cloud-Daten wurden gelöscht. Der lokale Fortschritt bleibt."
                )
            }
        }
    }

    private suspend fun syncInternal() {
        val active = client ?: return
        val user = active.auth.currentUserOrNull() ?: run {
            _state.update { it.copy(signedIn = false, phase = SyncPhase.LOCAL_ONLY, busy = false) }
            return
        }
        if (user.emailConfirmedAt == null) {
            _state.update {
                it.copy(
                    signedIn = true,
                    email = user.email,
                    emailVerified = false,
                    phase = SyncPhase.READY,
                    busy = false,
                    error = "Bitte bestätige zuerst deine E-Mail-Adresse."
                )
            }
            return
        }

        _state.update {
            it.copy(
                signedIn = true,
                email = user.email,
                emailVerified = true,
                busy = true,
                phase = SyncPhase.SYNCING,
                error = null,
                conflict = null
            )
        }

        // Ein Fortschritt, der zu einem anderen Konto gehört, wandert nicht stillschweigend mit.
        val ownerUid = prefs.getString(KEY_OWNER_UID, null)
        val ownerEmail = prefs.getString(KEY_OWNER_EMAIL, null)
        if (ownerUid != null && ownerUid != user.id && progressProvider().onboarded) {
            _state.update {
                it.copy(
                    signedIn = true,
                    email = user.email,
                    emailVerified = true,
                    busy = false,
                    phase = SyncPhase.READY,
                    conflict = null,
                    localOwner = ownerEmail,
                    error = null,
                    message = "Auf diesem Gerät liegt der Fortschritt von " +
                        (ownerEmail ?: "einem anderen Konto") +
                        ". Er wird nicht in ein anderes Konto übertragen."
                )
            }
            return
        }

        try {
            val uid = user.id
            val local = progressProvider()
            val localHash = hashOf(local)
            val lastRevision = prefs.getLong(revisionKey(uid), 0L)
            val lastHash = prefs.getString(hashKey(uid), null)
            val dirty = lastHash == null || lastHash != localHash
            val cloud = fetchCloud(uid)
            val action = SyncPlanner.decide(
                cloudExists = cloud != null,
                localOnboarded = local.onboarded,
                localDirty = dirty,
                localRevision = lastRevision,
                cloudRevision = cloud?.revision ?: 0L
            )

            when (action) {
                SyncAction.NOOP -> {
                    _state.update {
                        it.copy(
                            busy = false,
                            phase = if (cloud != null) SyncPhase.SYNCED else SyncPhase.READY,
                            lastSyncAt = prefs.getLong(lastSyncKey(uid), 0L).takeIf { value -> value > 0L },
                            message = if (cloud != null) "Alles ist aktuell." else null
                        )
                    }
                }

                SyncAction.UPLOAD -> {
                    pushLocal(uid, local, lastRevision, force = false)
                    uploadRecordings(uid)
                    rememberOwner(uid)
                }

                SyncAction.DOWNLOAD -> {
                    val cloudProgress = cloud?.let { decodeProgress(it) }
                        ?: error("Der Cloud-Stand konnte nicht gelesen werden.")
                    val prepared = downloadRecordings(uid, cloudProgress)
                    if (!importProgress(prepared)) {
                        error("Der Cloud-Stand konnte nicht gespeichert werden.")
                    }
                    markSynced(uid, cloud.revision, hashOf(progressProvider()))
                    rememberOwner(uid)
                    _state.update {
                        it.copy(
                            busy = false,
                            phase = SyncPhase.SYNCED,
                            lastSyncAt = System.currentTimeMillis(),
                            message = "Cloud-Stand geladen."
                        )
                    }
                }

                SyncAction.CONFLICT -> {
                    val cloudState = cloud ?: error("Cloud-Stand fehlt.")
                    _state.update {
                        it.copy(
                            busy = false,
                            phase = SyncPhase.CONFLICT,
                            conflict = SyncConflict(cloudState),
                            message = "Auf beiden Seiten gibt es Änderungen."
                        )
                    }
                }
            }
        } catch (error: Throwable) {
            if (isNetworkError(error)) SyncScheduler.enqueue(context)
            _state.update {
                it.copy(
                    busy = false,
                    phase = if (isNetworkError(error)) SyncPhase.OFFLINE else SyncPhase.ERROR,
                    error = readableError(error)
                )
            }
        }
    }

    private suspend fun pushLocal(
        userId: String,
        progress: Progress,
        expectedRevision: Long,
        force: Boolean
    ) {
        val active = client ?: return
        val result = active.postgrest
            .rpc(
                "push_learning_state",
                PushStateParams(
                    progress = asJson(progress),
                    expectedRevision = expectedRevision,
                    clientUpdatedAt = Instant.now().toString(),
                    force = force
                )
            )
            .decodeSingle<PushStateResult>()

        if (result.conflict) {
            val cloud = fetchCloud(userId) ?: error("Cloud-Stand fehlt.")
            _state.update {
                it.copy(
                    busy = false,
                    phase = SyncPhase.CONFLICT,
                    conflict = SyncConflict(cloud),
                    message = "Auf beiden Seiten gibt es Änderungen."
                )
            }
            return
        }

        markSynced(userId, result.revision, hashOf(progress))
        _state.update {
            it.copy(
                busy = false,
                phase = SyncPhase.SYNCED,
                lastSyncAt = System.currentTimeMillis(),
                message = "Fortschritt in der Cloud gesichert."
            )
        }
    }

    private suspend fun fetchCloud(userId: String): CloudLearningState? {
        val active = client ?: return null
        return active.from("learning_states")
            .select { filter { eq("user_id", userId) } }
            .decodeSingleOrNull<CloudLearningState>()
    }

    private fun decodeProgress(cloud: CloudLearningState): Progress? = runCatching {
        json.decodeFromJsonElement(Progress.serializer(), cloud.progress)
    }.getOrNull()

    private suspend fun downloadRecordings(userId: String, progress: Progress): Progress {
        val active = client ?: return progress
        val directory = File(context.filesDir, "recordings").apply { mkdirs() }
        val bucket = active.storage.from(SupabaseConfig.BUCKET)
        val remapped = RecordingPaths.remap(progress, directory)
        remapped.recordings.forEach { item ->
            val target = File(item.path)
            if (!target.exists() || target.length() == 0L) {
                runCatching {
                    target.writeBytes(
                        bucket.downloadAuthenticated(
                            RecordingPaths.objectName(userId, item.id)
                        )
                    )
                }
            }
        }
        return remapped
    }

    private suspend fun uploadRecordings(userId: String) {
        val active = client ?: return
        val bucket = active.storage.from(SupabaseConfig.BUCKET)
        val remote = runCatching { bucket.list(userId).map { it.name } }.getOrDefault(emptyList())
        val local = progressProvider().recordings
        local.forEach { item ->
            val file = File(item.path)
            val objectName = RecordingPaths.objectName(userId, item.id)
            if (file.exists() && file.length() > 0L && objectName !in remote) {
                runCatching { bucket.upload(objectName, file.readBytes()) { upsert = false } }
            }
        }
        val keep = local.map { RecordingPaths.objectName(userId, it.id) }.toSet()
        remote.filterNot { it in keep }.forEach { stale ->
            runCatching { bucket.delete(stale) }
        }
    }

    /** Merkt sich, wem der Fortschritt auf diesem Gerät gehört – für die Zeit nach dem Abmelden. */
    private fun rememberOwner(userId: String) {
        prefs.edit()
            .putString(KEY_OWNER_UID, userId)
            .putString(KEY_OWNER_EMAIL, _state.value.email)
            .apply()
        _state.update { it.copy(localOwner = _state.value.email) }
    }

    private fun markSynced(userId: String, revision: Long, hash: String) {
        prefs.edit()
            .putLong(revisionKey(userId), revision)
            .putString(hashKey(userId), hash)
            .putLong(lastSyncKey(userId), System.currentTimeMillis())
            .apply()
    }

    private fun clearSyncMetadata(userId: String) {
        prefs.edit()
            .remove(revisionKey(userId))
            .remove(hashKey(userId))
            .remove(lastSyncKey(userId))
            .apply()
    }

    private suspend fun refreshUser() {
        val active = client ?: return
        val user = runCatching { active.auth.currentUserOrNull() }.getOrNull()
        val signedIn = user != null
        val verified = user?.emailConfirmedAt != null
        _state.update { current ->
            val lastSync = if (user != null) {
                prefs.getLong(lastSyncKey(user.id), 0L).takeIf { it > 0L }
            } else {
                null
            }
            current.copy(
                configured = true,
                signedIn = signedIn,
                email = user?.email ?: current.email,
                emailVerified = verified,
                phase = when {
                    !signedIn -> SyncPhase.LOCAL_ONLY
                    current.phase == SyncPhase.CONFLICT -> SyncPhase.CONFLICT
                    current.phase == SyncPhase.SYNCED -> SyncPhase.SYNCED
                    else -> SyncPhase.READY
                },
                lastSyncAt = lastSync,
                localOwner = prefs.getString(KEY_OWNER_EMAIL, null)
            )
        }
        // Admins landen in der Konsole statt in der Lern-App. Die Zugehörigkeit steht in
        // public.admins und lässt sich nur dort ändern, nicht in der App.
        val admin = if (user != null) runCatching { readAdminFlag(user.id) }.getOrDefault(false) else false
        _state.update { current -> current.copy(isAdmin = admin, adminUsers = if (admin) current.adminUsers else emptyList()) }
        if (autoSync && signedIn && verified) syncNow()
    }

    private suspend fun readAdminFlag(userId: String): Boolean {
        val active = client ?: return false
        val rows = active.from("admins")
            .select { filter { eq("user_id", userId) } }
            .decodeList<JsonObject>()
        return rows.isNotEmpty()
    }

    // ---- Admin console ------------------------------------------------------------------

    fun adminRefreshUsers() {
        if (client == null) return
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null) }
            try {
                val parsed = json.decodeFromString(AdminUsersResponse.serializer(), adminCall("list"))
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update {
                    it.copy(adminBusy = false, adminUsers = parsed.users, adminLoaded = true, adminError = null)
                }
            } catch (error: Throwable) {
                _state.update {
                    it.copy(adminBusy = false, adminLoaded = true, adminError = readableError(error))
                }
            }
        }
    }

    fun adminCreateUser(email: String, password: String) {
        if (client == null) return
        val clean = email.trim()
        if (!clean.contains("@") || clean.length < 5) {
            _state.update { it.copy(adminError = "Bitte gib eine gültige E-Mail-Adresse ein.") }
            return
        }
        if (password.length < 8) {
            _state.update { it.copy(adminError = "Das Passwort braucht mindestens 8 Zeichen.") }
            return
        }
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall("create", mapOf("email" to clean, "password" to password))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update {
                    it.copy(adminBusy = false, adminNotice = "Konto $clean ist angelegt und bestätigt.")
                }
                adminRefreshUsers()
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    fun adminDeleteUser(userId: String) {
        if (client == null) return
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall("delete", mapOf("userId" to userId))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update { it.copy(adminBusy = false, adminNotice = "Konto wurde gelöscht.") }
                adminRefreshUsers()
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    fun adminSetPassword(userId: String, password: String) {
        if (client == null) return
        if (password.length < 8) {
            _state.update { it.copy(adminError = "Das Passwort braucht mindestens 8 Zeichen.") }
            return
        }
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall("set-password", mapOf("userId" to userId, "password" to password))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update { it.copy(adminBusy = false, adminNotice = "Neues Passwort gespeichert.") }
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    fun adminSetEmail(userId: String, email: String) {
        if (client == null) return
        val clean = email.trim()
        if (!clean.contains("@") || clean.length < 5) {
            _state.update { it.copy(adminError = "Bitte gib eine gültige E-Mail-Adresse ein.") }
            return
        }
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall("set-email", mapOf("userId" to userId, "email" to clean))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update {
                    it.copy(adminBusy = false, adminNotice = "Adresse geändert auf $clean.")
                }
                adminRefreshUsers()
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    /** Sperren heißt: keine neue Anmeldung. Der Lernstand bleibt erhalten. */
    fun adminSetBanned(userId: String, banned: Boolean) {
        if (client == null) return
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall(if (banned) "ban" else "unban", mapOf("userId" to userId))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update {
                    it.copy(
                        adminBusy = false,
                        adminNotice = if (banned) "Konto ist gesperrt." else "Konto ist wieder offen."
                    )
                }
                adminRefreshUsers()
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    fun adminResetProgress(userId: String) {
        if (client == null) return
        scope.launch {
            _state.update { it.copy(adminBusy = true, adminError = null, adminNotice = null) }
            try {
                val parsed = json.decodeFromString(
                    AdminActionResponse.serializer(),
                    adminCall("reset-progress", mapOf("userId" to userId))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update {
                    it.copy(
                        adminBusy = false,
                        adminDetail = null,
                        adminDetailFor = null,
                        adminNotice = "Fortschritt zurückgesetzt. Das Konto bleibt."
                    )
                }
                adminRefreshUsers()
            } catch (error: Throwable) {
                _state.update { it.copy(adminBusy = false, adminError = readableError(error)) }
            }
        }
    }

    fun adminLoadDetail(userId: String) {
        if (client == null) return
        scope.launch {
            _state.update {
                it.copy(adminDetailBusy = true, adminDetailFor = userId, adminDetail = null, adminError = null)
            }
            try {
                val parsed = json.decodeFromString(
                    AdminDetailResponse.serializer(),
                    adminCall("detail", mapOf("userId" to userId))
                )
                parsed.error?.let { throw IllegalStateException(it) }
                _state.update { it.copy(adminDetailBusy = false, adminDetail = parsed.detail) }
            } catch (error: Throwable) {
                _state.update { it.copy(adminDetailBusy = false, adminError = readableError(error)) }
            }
        }
    }

    private suspend fun adminCall(action: String, payload: Map<String, String> = emptyMap()): String {
        val active = client ?: error("Supabase ist nicht eingerichtet.")
        val body = buildJsonObject {
            put("action", action)
            payload.forEach { (key, value) -> put(key, value) }
        }
        return active.functions.invoke("admin-users", body = body).bodyAsText()
    }

    private fun runAction(block: suspend () -> Unit) {
        val active = client ?: return
        scope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                block()
            } catch (error: Throwable) {
                if (isNetworkError(error)) SyncScheduler.enqueue(context)
                _state.update {
                    it.copy(
                        busy = false,
                        phase = if (isNetworkError(error)) SyncPhase.OFFLINE else SyncPhase.ERROR,
                        error = readableError(error)
                    )
                }
            }
        }
    }

    private fun validateCredentials(email: String, password: String): Pair<String, String>? {
        if (client == null) {
            _state.update {
                it.copy(error = "Supabase ist noch nicht eingerichtet. Siehe docs/accounts.md.")
            }
            return null
        }
        // Admin-Konten melden sich mit einem kurzen Namen an (admin-11). Für die Anmeldung wird
        // daraus eine Adresse in der internen Domain; alles andere bleibt eine echte E-Mail.
        val clean = if (email.contains("@")) {
            email.trim()
        } else {
            email.trim() + "@" + SupabaseConfig.ACCOUNT_DOMAIN
        }
        if (!clean.contains("@") || clean.length < 5) {
            _state.update { it.copy(error = "Bitte gib eine gültige E-Mail-Adresse ein.") }
            return null
        }
        if (password.length < 8) {
            _state.update { it.copy(error = "Das Passwort braucht mindestens 8 Zeichen.") }
            return null
        }
        return clean to password
    }

    private fun asJson(progress: Progress): JsonObject =
        json.encodeToJsonElement(Progress.serializer(), progress).jsonObject

    private fun hashOf(progress: Progress): String {
        val text = json.encodeToString(Progress.serializer(), progress)
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun revisionKey(userId: String) = "revision_$userId"
    private fun hashKey(userId: String) = "hash_$userId"
    private fun lastSyncKey(userId: String) = "last_sync_$userId"

    private fun isNetworkError(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return message.contains("timeout") ||
            message.contains("connect") ||
            message.contains("network") ||
            message.contains("unknownhost") ||
            message.contains("unreachable") ||
            message.contains("offline")
    }

    private fun readableError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("Email not confirmed", ignoreCase = true) ->
                "Bitte bestätige zuerst deine E-Mail-Adresse."
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "E-Mail oder Passwort stimmt nicht."
            message.contains("User already registered", ignoreCase = true) ->
                "Für diese E-Mail gibt es bereits ein Konto. Melde dich an."
            message.contains("Password should be", ignoreCase = true) ->
                "Das Passwort ist zu schwach. Nimm mindestens 8 Zeichen."
            isNetworkError(error) ->
                "Keine Verbindung. Der lokale Fortschritt bleibt sicher und wird später erneut versucht."
            message.isBlank() -> "Das hat nicht geklappt. Bitte versuche es noch einmal."
            else -> message
        }
    }

    private companion object {
        const val KEY_SYNC_ENABLED = "sync_enabled"
        /** Das Konto, aus dem der lokale Fortschritt zuletzt kam. */
        const val KEY_OWNER_UID = "owner_uid"
        const val KEY_OWNER_EMAIL = "owner_email"
    }
}
