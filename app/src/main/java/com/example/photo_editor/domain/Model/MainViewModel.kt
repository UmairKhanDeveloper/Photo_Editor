package com.example.photo_editor.domain.Model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photo_editor.data.remote.api.api
import com.example.photo_editor.domain.repository.Repository
import com.example.photo_editor.domain.usecase.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {


    private val _allWeatherReport = MutableStateFlow<ResultState<api>>(ResultState.Loading)
    val allPhoto: StateFlow<ResultState<api>> = _allWeatherReport.asStateFlow()


    private val _processingState = MutableStateFlow(false)
    val processingState: StateFlow<Boolean> = _processingState.asStateFlow()

    private val _processedImage = MutableStateFlow<Bitmap?>(null)
    val processedImage: StateFlow<Bitmap?> = _processedImage.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()


    fun getPhoto() {
        viewModelScope.launch {
            _allWeatherReport.value = ResultState.Loading
            try {
                val response = repository.getPhoto()
                _allWeatherReport.value = ResultState.Succses(response)
            } catch (e: Exception) {
                _allWeatherReport.value = ResultState.Error(e)
            }
        }
    }


    fun removeBackground(imageUrl: String) {
        viewModelScope.launch {
            try {
                _processingState.value = true
                val byteArray = repository.removeBackgroundFromUrl(imageUrl)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                _processedImage.value = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                _errorState.value = "Error removing background"
            } finally {
                _processingState.value = false
            }
        }
    }
}
