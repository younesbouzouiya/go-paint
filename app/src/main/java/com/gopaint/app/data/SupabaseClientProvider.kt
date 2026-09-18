package com.gopaint.app.data

import com.gopaint.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Single shared Supabase client for the whole app.
 * URL and anon key come from BuildConfig (see app/build.gradle.kts),
 * which are safe to ship in the client - RLS policies on the backend
 * do the actual access control.
 */
object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            // Deep link used to catch the OAuth redirect, must match
            // AndroidManifest.xml's intent-filter (scheme + host) and the
            // Redirect URL configured in Supabase Dashboard > Authentication > URL Configuration.
            scheme = "gopaint"
            host = "login-callback"
        }
        install(Postgrest)
        install(Storage)
    }
}
