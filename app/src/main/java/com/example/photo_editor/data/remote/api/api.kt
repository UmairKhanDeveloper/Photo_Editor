package com.example.photo_editor.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class api(
    @SerialName("next_page")
    val nextPage: String,
    @SerialName("page")
    val page: Int,
    @SerialName("per_page")
    val perPage: Int,
    @SerialName("photos")
    val photos: List<Photo>,
    @SerialName("total_results")
    val totalResults: Int
)