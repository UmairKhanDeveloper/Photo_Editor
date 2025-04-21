package com.example.photo_editor.domain.usecase

sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Succses<T>(val response: T) : ResultState<T>()
    data class Error(val error: Throwable) : ResultState<Nothing>()

}