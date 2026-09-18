package com.gopaint.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val role: String = "user"
)

enum class ContentType(val value: String) {
    BRUSH("brush"),
    PALETTE("palette"),
    REFERENCE("reference")
}

enum class ContentStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected")
}

@Serializable
data class ContentItem(
    val id: String = "",
    val type: String,
    val subcategory: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("artist_id") val artistId: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("qr_code_url") val qrCodeUrl: String? = null,
    @SerialName("palette_colors") val paletteColors: List<String>? = null,
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val status: String = "pending",
    @SerialName("downloads_count") val downloadsCount: Int = 0,
    @SerialName("likes_count") val likesCount: Int = 0
)

/** The four browsable sections shown on the home screen. */
enum class HomeTab(val label: String, val type: ContentType, val subcategory: String? = null) {
    BRUSHES("فرش", ContentType.BRUSH),
    PALETTES("باليت", ContentType.PALETTE),
    EYES("عيون", ContentType.REFERENCE, "eyes"),
    BODY("هيكل", ContentType.REFERENCE, "body")
}
