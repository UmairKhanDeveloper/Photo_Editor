package com.example.photo_editor.data.remote.apiclient

import com.example.photo_editor.data.remote.api.api

interface PhotoClient {
    suspend fun getPhoto(): api

    suspend fun removeBackgroundFromUrl(imageUrl: String): ByteArray
}
