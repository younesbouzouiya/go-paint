package com.gopaint.app.data

import com.gopaint.app.data.model.ContentItem
import com.gopaint.app.data.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class ContentRepository {

    private val client = SupabaseClientProvider.client

    /** Approved items for one of the 4 home tabs (optionally filtered by subcategory and a search term). */
    suspend fun getApprovedItems(type: String, subcategory: String? = null, search: String? = null): List<ContentItem> {
        return client.postgrest["content_items"].select {
            filter {
                eq("type", type)
                eq("status", "approved")
                subcategory?.let { eq("subcategory", it) }
                if (!search.isNullOrBlank()) ilike("title", "%$search%")
            }
            order("created_at", Order.DESCENDING)
        }.decodeList()
    }

    suspend fun submitItem(item: ContentItem) {
        client.postgrest["content_items"].insert(item)
    }

    suspend fun getMyProfile(): Profile? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest["profiles"].select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull()
    }

    suspend fun isCurrentUserAdmin(): Boolean {
        return getMyProfile()?.role == "admin"
    }

    // ---- Favorites / likes ----

    suspend fun isLiked(contentId: String): Boolean {
        val userId = client.auth.currentUserOrNull()?.id ?: return false
        return client.postgrest["likes"].select {
            filter { eq("user_id", userId); eq("content_id", contentId) }
        }.decodeList<Map<String, String>>().isNotEmpty()
    }

    suspend fun toggleLike(contentId: String): Boolean {
        val userId = client.auth.currentUserOrNull()?.id ?: return false
        val alreadyLiked = isLiked(contentId)
        if (alreadyLiked) {
            client.postgrest["likes"].delete {
                filter { eq("user_id", userId); eq("content_id", contentId) }
            }
        } else {
            client.postgrest["likes"].insert(
                mapOf("user_id" to userId, "content_id" to contentId)
            )
        }
        return !alreadyLiked
    }

    /** All items the current user has favorited, newest first. */
    suspend fun getFavoriteItems(): List<ContentItem> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        val likedIds = client.postgrest["likes"].select(columns = Columns.list("content_id")) {
            filter { eq("user_id", userId) }
        }.decodeList<Map<String, String>>().mapNotNull { it["content_id"] }

        if (likedIds.isEmpty()) return emptyList()

        return client.postgrest["content_items"].select {
            filter { isIn("id", likedIds) }
        }.decodeList()
    }

    // ---- Storage upload ----

    /** Uploads raw image bytes to the public content-media bucket and returns a public URL. */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String {
        val path = "covers/${System.currentTimeMillis()}_$fileName"
        client.storage["content-media"].upload(path, bytes)
        return client.storage["content-media"].publicUrl(path)
    }
}
