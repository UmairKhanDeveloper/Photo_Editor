package com.example.photo_editor.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    Scaffold (topBar = {
        TopAppBar(title = {
            Text(text = "Photo Editor")
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0XFF5a5a5a)))
    }){


    }
    
}