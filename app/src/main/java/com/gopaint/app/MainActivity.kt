package com.gopaint.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gopaint.app.data.AuthRepository
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.SupabaseClientProvider
import com.gopaint.app.nav.GoPaintNavGraph
import com.gopaint.app.ui.theme.GoPaintTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepository()
    private val contentRepository = ContentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Completes the Google OAuth flow when the browser redirects back into the app.
        SupabaseClientProvider.client.handleDeeplinks(intent)

        setContent {
            GoPaintTheme {
                GoPaintNavGraph(
                    authRepository = authRepository,
                    contentRepository = contentRepository
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SupabaseClientProvider.client.handleDeeplinks(intent)
    }
}
