package com.gopaint.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gopaint.app.data.ContentRepository
import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.ui.theme.*

@Composable
fun FavoritesScreen(repository: ContentRepository, onItemClick: (ContentItem) -> Unit) {
    var favorites by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        favorites = try { repository.getFavoriteItems() } catch (e: Exception) { emptyList() }
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(BgApp)) {
        Text(
            "المفضلة",
            fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary,
            modifier = Modifier.padding(18.dp, 16.dp, 18.dp, 8.dp)
        )
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
            favorites.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لم تضف أي عنصر للمفضلة بعد", color = TextDim)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites, key = { it.id }) { item -> FavoriteCard(item, onClick = { onItemClick(item) }) }
            }
        }
    }
}

@Composable
private fun FavoriteCard(item: ContentItem, onClick: () -> Unit) {
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
            Text(item.sourceName ?: "غير معروف", color = TextDim, fontSize = 10.sp, maxLines = 1)
        }
    }
}
