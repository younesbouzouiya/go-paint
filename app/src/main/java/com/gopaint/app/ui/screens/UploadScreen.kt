package com.gopaint.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.SupabaseClientProvider
import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.data.model.ContentType
import com.gopaint.app.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

private val typeOptions = listOf(
    "فرشاة" to (ContentType.BRUSH to null),
    "باليت ألوان" to (ContentType.PALETTE to null),
    "مرجعية - عيون" to (ContentType.REFERENCE to "eyes"),
    "مرجعية - هيكل جسم" to (ContentType.REFERENCE to "body")
)

@Composable
fun UploadScreen(repository: ContentRepository, onDone: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(typeOptions.first()) }
    var fileUrl by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var pickedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var uploadingImage by remember { mutableStateOf(false) }
    var sourceName by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploadingImage = true
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        pickedImageBytes = bytes
                        imageUrl = repository.uploadImage(bytes, "img_${System.currentTimeMillis()}.jpg")
                    }
                } catch (e: Exception) {
                    error = "تعذر رفع الصورة: ${e.message}"
                } finally {
                    uploadingImage = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BgApp).verticalScroll(rememberScrollState()).padding(18.dp)
    ) {
        Text("إضافة عنصر جديد", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            "سيظهر عملك للعموم بعد موافقة الإدارة \uD83D\uDD52",
            fontSize = 11.5.sp, color = BrandPink
        )
        Spacer(Modifier.height(18.dp))

        Text("القسم", fontSize = 11.sp, color = TextDim)
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedType.first)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                typeOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option.first) }, onClick = {
                        selectedType = option; expanded = false
                    })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("عنوان العنصر") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("وصف مختصر (اختياري)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Text("صورة العرض (thumbnail)", fontSize = 11.sp, color = TextDim)
        Spacer(Modifier.height(4.dp))
        if (imageUrl != null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(CardBg2),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = imageUrl, contentDescription = "معاينة", modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(6.dp))
        }
        OutlinedButton(
            onClick = { imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uploadingImage
        ) {
            Text(
                when {
                    uploadingImage -> "جاري رفع الصورة..."
                    imageUrl != null -> "تغيير الصورة"
                    else -> "اختيار صورة من الجهاز"
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = fileUrl, onValueChange = { fileUrl = it },
            label = { Text("رابط الملف (Drive/Dropbox) - للفرش") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = sourceName, onValueChange = { sourceName = it },
            label = { Text("اسم المصدر الأصلي") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = sourceUrl, onValueChange = { sourceUrl = it },
            label = { Text("رابط المصدر الأصلي") }, modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = DangerRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                submitting = true; error = null
                scope.launch {
                    try {
                        val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                        repository.submitItem(
                            ContentItem(
                                type = selectedType.second.first.value,
                                subcategory = selectedType.second.second,
                                title = title,
                                description = description.ifBlank { null },
                                artistId = userId,
                                fileUrl = fileUrl.ifBlank { null },
                                imageUrl = imageUrl,
                                sourceName = sourceName.ifBlank { null },
                                sourceUrl = sourceUrl.ifBlank { null }
                            )
                        )
                        onDone()
                    } catch (e: Exception) {
                        error = e.message ?: "تعذر الإرسال، حاول مجدداً"
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = !submitting && title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
        ) {
            Text(if (submitting) "جاري الإرسال..." else "إرسال للمراجعة", fontWeight = FontWeight.Bold)
        }
    }
}
