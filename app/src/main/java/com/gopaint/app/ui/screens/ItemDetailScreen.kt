package com.gopaint.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.ui.theme.*
import com.gopaint.app.util.QrCodeGenerator
import kotlinx.coroutines.launch

@Composable
fun ItemDetailScreen(item: ContentItem, repository: ContentRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var liked by remember { mutableStateOf(false) }

    LaunchedEffect(item.id) {
        liked = try { repository.isLiked(item.id) } catch (e: Exception) { false }
    }

    LaunchedEffect(item.fileUrl) {
        val target = item.fileUrl ?: item.sourceUrl
        if (!target.isNullOrBlank()) {
            qrBitmap = QrCodeGenerator.generate(target)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgApp)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(170.dp).background(CardBg2),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(model = item.imageUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize())
            } else {
                Text("🎨", fontSize = 44.sp)
            }
            IconButtonBack(onBack, modifier = Modifier.align(Alignment.TopStart))
            IconButtonLike(
                liked = liked,
                onToggle = {
                    scope.launch {
                        liked = try { repository.toggleLike(item.id) } catch (e: Exception) { liked }
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Column(modifier = Modifier.padding(18.dp).weight(1f)) {
            Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("قسم: ${labelFor(item.type, item.subcategory)}", fontSize = 12.sp, color = TextDim)

            if (!item.description.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(item.description, fontSize = 13.sp, color = TextDim)
            }

            Spacer(Modifier.height(16.dp))

            // Palette swatches, only for palette items
            item.paletteColors?.let { colors ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.take(6).forEach { hex ->
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                .background(parseHexColorSafe(hex))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // QR box (shown for brushes / anything with an external file link)
            if (qrBitmap != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR code",
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "امسح الكود لفتح الملف مباشرة، أو استعمل الزر بالأسفل",
                        fontSize = 11.5.sp, color = TextDim
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val target = item.fileUrl ?: item.sourceUrl
                if (!target.isNullOrBlank()) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) { Text("فتح الرابط") }
                }
            }

            Spacer(Modifier.weight(1f))

            if (!item.sourceName.isNullOrBlank() || !item.sourceUrl.isNullOrBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .border(width = 1.dp, color = BorderColor)
                        .padding(top = 10.dp)
                ) {
                    Text(
                        "المصدر: ${item.sourceName ?: "غير معروف"}",
                        fontSize = 10.5.sp, color = TextDim
                    )
                    if (!item.sourceUrl.isNullOrBlank()) {
                        Text(item.sourceUrl, fontSize = 10.sp, color = BrandBlue, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButtonBack(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(14.dp)
            .clip(RoundedCornerShape(50))
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
            .size(32.dp)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            "←",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun IconButtonLike(liked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(14.dp)
            .clip(RoundedCornerShape(50))
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
            .size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "المفضلة",
                tint = if (liked) BrandPink else androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

private fun labelFor(type: String, subcategory: String?): String = when (type) {
    "brush" -> "فرشاة"
    "palette" -> "باليت ألوان"
    "reference" -> when (subcategory) {
        "eyes" -> "مرجعية عيون"
        "body" -> "مرجعية هيكل جسم"
        else -> "مرجعية"
    }
    else -> type
}

private fun parseHexColorSafe(hex: String): androidx.compose.ui.graphics.Color = try {
    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    CardBg2
}
