package com.example.photo_editor.domain.Model

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
}