package com.example.photo_editor.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.photo_editor.R
import com.example.photo_editor.data.remote.api.Photo
import com.example.photo_editor.data.remote.api.api
import com.example.photo_editor.domain.Model.MainViewModel
import com.example.photo_editor.domain.repository.Repository
import com.example.photo_editor.domain.usecase.ResultState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { Repository() }
    val viewModel = remember { MainViewModel(repository) }
    val state by viewModel.allPhoto.collectAsState()
    var allWeatherData by remember { mutableStateOf<api?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var uri by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uri = uris
            val selectedUri = uris.firstOrNull()
            selectedUri?.let {
                navController.navigate("PhotoEditor/${Uri.encode(it.toString())}")
            }
        }
    )

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedImage = bitmap
            val imageUri = bitmap.toUri(context)
            navController.navigate("PhotoEditor/${Uri.encode(imageUri.toString())}")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getPhoto()
    }

    when (state) {
        is ResultState.Error -> {
            isLoading = false
            val error = (state as ResultState.Error).error
            Text(text = "$error")
        }

        ResultState.Loading -> {
            isLoading = true
        }

        is ResultState.Succses -> {
            isLoading = false
            val succses = (state as ResultState.Succses).response
            allWeatherData = succses
        }
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFa92c76),
            Color(0xFFea553e)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Photo Editor",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFea553e),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Create New",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier.clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                            .clip(RoundedCornerShape(16.dp))
                            .size(width = 150.dp, height = 180.dp)
                            .background(color = Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(gradientBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "From Files",
                                color = Color(0xFFea553e),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch()
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                            .size(width = 150.dp, height = 180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFec5c6d))
                            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                modifier = Modifier.size(50.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "By Camera",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.roboto1)),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = "For you",
            fontSize = 30.sp,
            fontFamily = FontFamily(Font(R.font.roboto1)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(1500.dp)
        ) {
            allWeatherData?.photos?.let { photoList ->
                items(photoList) { photo ->
                    PhotoItem(photo,navController)
                }
            }
        }
    }
}

@Composable
fun PhotoItem(photo: Photo, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable {
            val encodedUrl = URLEncoder.encode(photo.src.medium, StandardCharsets.UTF_8.toString())
            navController.navigate("PhotoEditor/$encodedUrl")
        }
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            AsyncImage(
                model = photo.src.medium,
                contentDescription = "Photo by ${photo.photographer}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}




fun Bitmap.toUri(context: Context): Uri? {

    val file = File(context.cacheDir, "image_${UUID.randomUUID()}.png")
    try {
        val outputStream = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    return FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        file
    )
}

