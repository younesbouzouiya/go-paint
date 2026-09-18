package com.gopaint.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopaint.app.data.AuthRepository
import com.gopaint.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoggedIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgApp)
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("مرحبا بك 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            if (isSignUp) "أنشئ حساب جديد للبدء" else "سجّل دخولك للوصول لكل الفرش والمرجعيات",
            fontSize = 13.sp, color = TextDim
        )
        Spacer(Modifier.height(20.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("اسم المستخدم") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = DangerRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                loading = true; error = null
                scope.launch {
                    try {
                        if (isSignUp) authRepository.signUpWithEmail(email, password, username)
                        else authRepository.signInWithEmail(email, password)
                        onLoggedIn()
                    } catch (e: Exception) {
                        error = e.message ?: "حدث خطأ، حاول مجدداً"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Text(if (isSignUp) "إنشاء حساب" else "تسجيل الدخول", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { scope.launch { authRepository.signInWithGoogle() } },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("المتابعة عبر Google")
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isSignUp) "لديك حساب؟ تسجيل الدخول" else "ليس لديك حساب؟ إنشاء حساب",
                color = BrandPink, fontSize = 13.sp,
                modifier = Modifier.clickable { isSignUp = !isSignUp }
            )
        }
    }
}
