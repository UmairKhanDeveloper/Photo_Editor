package com.example.photo_editor.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.photo_editor.R
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun PhotoEditor(navController: NavController, imageUri: String?) {
    val context = LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageAspectRatio by remember { mutableStateOf(1f) }
    var isProcessing by remember { mutableStateOf(false) }
    var showCropView by remember { mutableStateOf(false) }


    var imageCrop by remember { mutableStateOf<ImageCrop?>(null) }

    LaunchedEffect(imageUri) {
        imageUri?.let {
            val bitmap = if (it.startsWith("http")) {
                loadBitmapFromUrl(it)
            } else {
                val decodedUri = Uri.decode(it)
                val uri = Uri.parse(decodedUri)
                uriToBitmap(context, uri)
            }

            bitmap?.let {
                originalBitmap = it
                processedBitmap = it
                imageAspectRatio = it.width.toFloat() / it.height.toFloat()
                imageCrop = ImageCrop(it)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                showCropView && imageCrop != null -> {
                    imageCrop!!.ImageCropView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageAspectRatio),
                        guideLineColor = Color.LightGray,
                        guideLineWidth = 2.dp,
                        edgeCircleSize = 5.dp,
                        showGuideLines = true,
                        cropType = CropType.SQUARE,
                        edgeType = EdgeType.CIRCULAR
                    )
                }

                processedBitmap != null -> {
                    Image(
                        bitmap = processedBitmap!!.asImageBitmap(),
                        contentDescription = "Edited Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageAspectRatio),
                        contentScale = ContentScale.Fit
                    )
                }

                else -> {
                    Text("No image selected", modifier = Modifier.align(Alignment.Center))
                }
            }
        }


        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0XFF333333))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Cancel", tint = Color.White)
                }

                IconButton(
                    onClick = {

                        if (showCropView && imageCrop != null) {
                            val croppedBitmap = imageCrop!!.onCrop()
                            processedBitmap = croppedBitmap
                            imageAspectRatio = croppedBitmap.width.toFloat() / croppedBitmap.height.toFloat()
                            showCropView = false
                        } else {

                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0XFF56c367))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                PhotoEditItem(
                    icon = painterResource(id = R.drawable.layer),
                    title = "Bg Remove"
                ) {
                    originalBitmap?.let { bitmap ->
                        isProcessing = true
                        lastBitmap = processedBitmap
                        BackgroundRemover.bitmapForProcessing(
                            bitmap,
                            true,
                            object : OnBackgroundChangeListener {
                                override fun onSuccess(result: Bitmap) {
                                    processedBitmap = result
                                    imageAspectRatio = result.width.toFloat() / result.height.toFloat()
                                    imageCrop = ImageCrop(result)
                                    isProcessing = false
                                }

                                override fun onFailed(exception: Exception) {
                                    isProcessing = false
                                    Toast.makeText(context, "Background removal failed", Toast.LENGTH_SHORT).show()
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
                }




                PhotoEditItem(icon = painterResource(id = R.drawable.crop), title = "Crop") {
                    showCropView = true
                }

                PhotoEditItem(icon = painterResource(id = R.drawable.brush), title = "Canvas") {}
                PhotoEditItem(icon = painterResource(id = R.drawable.filters), title = "Filters") {}
                PhotoEditItem(icon = painterResource(id = R.drawable.light), title = "Effect") {}
                PhotoEditItem(icon = painterResource(id = R.drawable.text), title = "Text") {}
                PhotoEditItem(icon = painterResource(id = R.drawable.frame), title = "Bg Frame") {}
            }
        }
    }
}



@Composable
fun PhotoEditItem(icon: Painter, title: String,onClick: () -> Unit ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
            .width(72.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun loadBitmapFromUrl(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}