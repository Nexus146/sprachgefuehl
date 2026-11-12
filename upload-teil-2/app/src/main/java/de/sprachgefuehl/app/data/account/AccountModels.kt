package de.sprachgefuehl.app.data.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CloudLearningState(
    @SerialName("user_id") val userId: String = "",
    val progress: JsonObject = JsonObject(emptyMap()),
    @SerialName("schema_version") val schemaVersion: Int = 1,
    val revision: Long = 0,
    @SerialName("client_updated_at") val clientUpdatedAt: String? = null,
    @SerialName("server_updated_at") val serverUpdatedAt: String? = null
)

@Serializable
data class PushStateParams(
    @SerialName("p_progress") val progress: JsonObject,
    @SerialName("p_expected_revision") val expectedRevision: Long,
    @SerialName("p_client_updated_at") val clientUpdatedAt: String? = null,
    @SerialName("p_force") val force: Boolean = false
)

@Serializable
data class PushStateResult(
    val revision: Long = 0,
    val conflict: Boolean = false,
    val progress: JsonObject? = null,
    @SerialName("server_updated_at") val serverUpdatedAt: String? = null
)

enum class SyncPhase {
    LOCAL_ONLY,
    READY,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR,
    CONFLICT
}

data class SyncConflict(
    val cloud: CloudLearningState
)

/** One row of the admin console: an auth account plus what its cloud document holds. */
@Serializable
data class AdminUser(
    val id: String = "",
    val email: String = "",
    val confirmed: Boolean = false,
    val createdAt: String? = null,
    val lastSignInAt: String? = null,
    val hasCloud: Boolean = false,
    val revision: Long = 0,
    val updatedAt: String? = null,
    val lessons: Int = 0,
    val minutes: Int = 0,
    val name: String = "",
    val onboarded: Boolean = false,
    /** Gesperrt: keine Anmeldung mehr, bis ein Admin entsperrt. Der Lernstand bleibt. */
    val banned: Boolean = false,
    val reason: String = "",
    val interests: List<String> = emptyList(),
    val dailyMinutes: Int = 0
)

/** What the admin sees when opening one account: the learning state, not just its size. */
@Serializable
data class AdminDetail(
    val id: String = "",
    val hasCloud: Boolean = false,
    val name: String = "",
    val reason: String = "",
    val interests: List<String> = emptyList(),
    val dailyMinutes: Int = 0,
    val lessons: Int = 0,
    val minutes: Int = 0,
    val onboarded: Boolean = false,
    val recordingCount: Int = 0,
    val recordingBytes: Long = 0,
    val revision: Long = 0,
    val updatedAt: String? = null
)

@Serializable
data class AdminDetailResponse(
    val detail: AdminDetail? = null,
    val error: String? = null
)

@Serializable
data class AdminUsersResponse(
    val users: List<AdminUser> = emptyList(),
    val error: String? = null
)

@Serializable
data class AdminActionResponse(
    val error: String? = null,
    val deleted: Boolean = false,
    val updated: Boolean = false
)

data class AccountUiState(
    val configured: Boolean = false,
    val signedIn: Boolean = false,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val busy: Boolean = false,
    val phase: SyncPhase = SyncPhase.LOCAL_ONLY,
    val message: String? = null,
    val error: String? = null,
    val lastSyncAt: Long? = null,
    val conflict: SyncConflict? = null,
    val passwordRecovery: Boolean = false,
    val syncEnabled: Boolean = true,
    /** Adresse des Kontos, aus dem der Fortschritt auf diesem Gerät stammt. */
    val localOwner: String? = null,
    /** Admin-Konten landen nicht in der Lern-App, sondern in der Konsole. */
    val isAdmin: Boolean = false,
    val adminUsers: List<AdminUser> = emptyList(),
    val adminBusy: Boolean = false,
    val adminLoaded: Boolean = false,
    val adminError: String? = null,
    val adminNotice: String? = null,
    /** Das gerade geöffnete Konto in der Admin-Konsole. */
    val adminDetail: AdminDetail? = null,
    val adminDetailFor: String? = null,
    val adminDetailBusy: Boolean = false
)
