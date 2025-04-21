package com.example.photo_editor.domain.repository

import com.example.photo_editor.data.remote.api.api
import com.example.photo_editor.data.remote.apiclient.ApiClientPhoto
import com.example.photo_editor.data.remote.apiclient.PhotoClient

class Repository:PhotoClient {
    override suspend fun getPhoto(): api {
        return ApiClientPhoto.getAllPhoto()
    }
}