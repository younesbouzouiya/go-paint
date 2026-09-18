package com.gopaint.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.data.model.HomeTab
import com.gopaint.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    repository: ContentRepository,
    onItemClick: (ContentItem) -> Unit,
    onUploadClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.BRUSHES) }
    var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab, searchQuery) {
        delay(250) // small debounce so we don't hit the DB on every keystroke
        loading = true; errorMsg = null
        try {
            contentItems = repository.getApprovedItems(selectedTab.type.value, selectedTab.subcategory, searchQuery)
        } catch (e: Exception) {
            errorMsg = "تعذر تحميل المحتوى، تحقق من الاتصال"
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgApp)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp, 14.dp, 18.dp, 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("GO Paint!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(BrandPurple)
                    .clickable { onFavoritesClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "المفضلة", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            placeholder = { Text("ابحث في هذا القسم...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        // Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HomeTab.values().toList()) { tab ->
                val isActive = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) BrandBlue else CardBg)
                        .border(1.dp, if (isActive) Color.Transparent else BorderColor, RoundedCornerShape(20.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tab.label, color = if (isActive) Color.White else TextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = BrandBlue
                )
                errorMsg != null -> Text(errorMsg!!, color = DangerRed, modifier = Modifier.align(Alignment.Center))
                contentItems.isEmpty() -> Text("لا يوجد محتوى بعد في هذا القسم", color = TextDim, modifier = Modifier.align(Alignment.Center))
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(contentItems, key = { it.id }) { item -> ContentCard(item, onClick = { onItemClick(item) }) }
                }
            }
        }

        // Upload FAB row (simple bottom action instead of full nav bar for brevity)
        Button(
            onClick = onUploadClick,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
        ) {
            Text("➕ إضافة محتوى جديد")
        }
    }
}

@Composable
private fun ContentCard(item: ContentItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(90.dp).background(CardBg2),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(model = item.imageUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize())
            } else {
                Text("🎨", fontSize = 26.sp)
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(item.sourceName ?: "غير معروف", color = TextDim, fontSize = 10.sp, maxLines = 1)
        }
    }
}
