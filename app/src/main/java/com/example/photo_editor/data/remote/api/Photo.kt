package com.example.photo_editor.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Photo(
    @SerialName("alt")
    val alt: String,
    @SerialName("avg_color")
    val avgColor: String,
    @SerialName("height")
    val height: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("liked")
    val liked: Boolean,
    @SerialName("photographer")
    val photographer: String,
    @SerialName("photographer_id")
    val photographerId: Long,
    @SerialName("photographer_url")
    val photographerUrl: String,
    @SerialName("src")
    val src: Src,
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int
)