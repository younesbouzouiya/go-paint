package com.gopaint.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopaint.app.data.AuthRepository
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.SupabaseClientProvider
import com.gopaint.app.nav.GoPaintNavGraph
import com.gopaint.app.ui.theme.GoPaintTheme
import io.github.jan.supabase.auth.handleDeeplinks
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Global safety net: if anything crashes, show the real stack trace on screen
        // instead of just closing the app, so the cause can be read directly on the device.
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            runOnUiThread {
                setContent { CrashScreen(sw.toString()) }
            }
        }

        try {
            // Only relevant when the app was opened via the Google OAuth redirect link;
            // a normal launcher intent has no data, so skip it then (this alone can crash
            // some supabase-kt versions if called unconditionally).
            intent?.data?.let {
                SupabaseClientProvider.client.handleDeeplinks(intent)
            }

            val authRepository = AuthRepository()
            val contentRepository = ContentRepository()

            setContent {
                GoPaintTheme {
                    GoPaintNavGraph(
                        authRepository = authRepository,
                        contentRepository = contentRepository
                    )
                }
            }
        } catch (t: Throwable) {
            val sw = StringWriter()
            t.printStackTrace(PrintWriter(sw))
            setContent { CrashScreen(sw.toString()) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let {
            SupabaseClientProvider.client.handleDeeplinks(intent)
        }
    }
}

/** Full-screen error view showing the raw stack trace so it can be read/screenshotted from the device. */
@androidx.compose.runtime.Composable
private fun CrashScreen(trace: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070914))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("حدث خطأ عند التشغيل ⚠️", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        Text(trace, color = Color(0xFFFF5A5A), fontSize = 11.sp)
    }
}
