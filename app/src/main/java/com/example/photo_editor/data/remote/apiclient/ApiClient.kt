package com.example.photo_editor.data.remote.apiclient

import com.example.photo_editor.data.remote.api.api
import com.example.photo_editor.data.remote.conatsnt.Constant
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.append
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.net.URLEncoder

object ApiClientPhoto {

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    prettyPrint = true
                }
            )
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
        }

        install(HttpTimeout) {
            socketTimeoutMillis = Constant.TIMEOUT
            requestTimeoutMillis = Constant.TIMEOUT
            connectTimeoutMillis = Constant.TIMEOUT
        }
    }

    suspend fun getAllPhoto(): api {
        val url = "${Constant.BASE_URL}/curated?per_page=20"
        val response: HttpResponse = client.get(url) {
            headers {
                append(HttpHeaders.Authorization, Constant.KEY)
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
        return response.body()
    }

    suspend fun removeBackgroundFromUrl(imageUrl: String): ByteArray {
        println("Using image URL: $imageUrl")

        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "https://api.remove.bg/v1.0/removebg",
            formData = formData {
                append("image_url", imageUrl)
                append("size", "auto")
            }
        ) {
            method = HttpMethod.Post
            headers {
                append("X-Api-Key", "5j8dUVSCRoi5P74RwPgNrpfB")
            }
        }

        if (!response.status.isSuccess()) {
            val error = response.bodyAsText()
            throw Exception("Error removing background: $error")
        }

        return response.readBytes()
    }

}