package com.example.photo_editor.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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

@OptIn(ExperimentalMaterial3Api::class)
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
            Color(0xFFffcb82),
            Color(0xFFffae81),
            Color(0xFFff8381)
        )
    )



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(gradientBrush),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photo Editor",
                fontWeight = FontWeight.W900,
                fontSize = 20.sp,
                color = Color(0XFF654f25),
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_king),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color(0XFF654f25)),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "",
                tint = Color(0XFF654f25),
                modifier = Modifier.size(25.dp)
            )
        }

        Spacer(modifier = Modifier.height(300.dp))


        Text(
            text = "CREATE NEW",
            fontWeight = FontWeight.W500,
            fontSize = 16.sp,
            color = Color(0XFF654f25),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 30.dp, bottom = 16.dp)
        )


        Card(
            modifier = Modifier
                .padding(10.dp)
                .width(375.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditItem(icon = R.drawable.ic_video, text = "Video") {

                }
                EditItem(icon = R.drawable.ic_photo, text = "Photo") {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                EditItem(icon = R.drawable.ic_collage, text = "Collage") {

                }
            }
        }
    }
}

@Composable
fun EditItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier

    ) {
        Box(
            modifier = Modifier
                .clickable { onClick() }
                .clip(CircleShape)
                .size(70.dp)
                .background(color = Color(0XFFfd5378)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = text,
            color = Color(0xFF654f25),
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun PhotoItem(photo: Photo, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clickable {
                val encodedUrl =
                    URLEncoder.encode(photo.src.medium, StandardCharsets.UTF_8.toString())
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

