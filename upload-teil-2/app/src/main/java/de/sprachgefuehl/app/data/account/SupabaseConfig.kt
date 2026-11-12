package de.sprachgefuehl.app.data.account

import de.sprachgefuehl.app.BuildConfig

/**
 * Supabase stays optional. Empty values are a valid configuration: the app then works exactly
 * like before and the account screen only shows how to connect a project.
 */
object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim()
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val configured: Boolean
        get() = url.startsWith("https://") && anonKey.isNotBlank()

    const val AUTH_REDIRECT = "sprachgefuehl://auth/confirm"

    /**
     * Admin accounts sign in with a short name (`admin-11`), not a mailbox. The app turns that into
     * an address inside this domain so the normal email login can carry it.
     */
    const val ACCOUNT_DOMAIN = "sprachgefuehl.local"
    const val BUCKET = "recordings"
}
