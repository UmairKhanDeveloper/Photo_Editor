package com.example.photo_editor.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlin.math.roundToInt

enum class EditTool {
    NONE, FILTERS, EFFECTS, TEXT, FRAME, BRUSH
}
enum class ImageFilterType(val label: String) {
    NONE("Original"),
    GRAYSCALE("Grayscale"),
    SEPIA("Sepia"),
    INVERT("Invert"),
    BRIGHTNESS("Brighten"),
    DARKEN("Darken"),
    CONTRAST("Contrast"),
    SATURATE("Saturate"),
    DESATURATE("Desaturate"),
    VINTAGE("Vintage")
}

data class TextItem(
    val id: Int? = null,
    val text: String,
    val position: Offset,
    val fontSize: Float,
    val rotation: Float,
    val color: Color,
    val isBold: Boolean,
    val shadowRadius: Float,
    val scale: Float = 1f
)


@Composable
fun PhotoEditor(navController: NavController, imageUri: String?) {
    val context = LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageAspectRatio by remember { mutableStateOf(1f) }
    var isProcessing by remember { mutableStateOf(false) }
    var showCropView by rememberSaveable { mutableStateOf(false) }
    var showDrawingCanvas by rememberSaveable { mutableStateOf(false) }
    var imageCrop by remember { mutableStateOf<ImageCrop?>(null) }

    var selectedTool by rememberSaveable { mutableStateOf(EditTool.NONE) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentBrushSize by remember { mutableStateOf(10f) }
    var isEraser by remember { mutableStateOf(false) }
    var currentOpacity by remember { mutableStateOf(1f) }

    val paths = remember { mutableStateListOf<PathData>() }
    var currentPath by remember { mutableStateOf(Path()) }

    val textItems = remember { mutableStateListOf<TextItem>() }
    var textSize by remember { mutableStateOf(24f) }
    var textRotation by remember { mutableStateOf(0f) }
    var showTextDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var isBold by remember { mutableStateOf(false) }
    var shadowRadius by remember { mutableStateOf(0f) }

    var currentFilter: ImageFilterType? by rememberSaveable { mutableStateOf(null) }
    var selectedFrameResId by remember { mutableStateOf<Int?>(null) }


    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Add Text") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter your text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bold Text", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isBold) Color.Gray else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { isBold = !isBold }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Choose Color:", fontWeight = FontWeight.Medium)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color.White,
                            Color.Black,
                            Color.Red,
                            Color.Green,
                            Color.Blue,
                            Color.Yellow,
                            Color.Magenta,
                            Color.Cyan,
                            Color(0xFF8B4513),
                            Color(0xFF800080),
                            Color(0xFF008000),
                            Color(0xFF000080)
                        )

                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color = color, shape = CircleShape)
                                    .clickable { selectedColor = color }
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedColor == color) Color.Gray else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        textItems.add(
                            TextItem(
                                id = null, // Pass null since the type is Int?
                                text = inputText,
                                position = Offset(0f, 0f),
                                color = selectedColor,
                                fontSize = textSize,
                                rotation = textRotation,
                                shadowRadius = shadowRadius,
                                isBold = isBold,
                                scale = 1f
                            )
                        )

                        inputText = ""
                        selectedColor = Color.White
                        textSize = 24f
                        textRotation = 0f
                        shadowRadius = 0f
                        isBold = false
                        showTextDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(imageUri) {
        imageUri?.let {
            val bitmap = if (it.startsWith("http")) {
                loadBitmapFromUrl(it)
            } else {
                val uri = Uri.parse(it)
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

    val displayedBitmap = remember(processedBitmap, currentFilter) {
        if (processedBitmap != null && currentFilter != null) {
            applyFilter(processedBitmap!!, currentFilter!!)
        } else {
            processedBitmap
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            displayedBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .height(500.dp)
                        .aspectRatio(imageAspectRatio)
                        .zIndex(0f)
                ) {

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Edited Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    Canvas(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                        paths.forEach { data ->
                            drawPath(
                                path = data.path,
                                color = data.color,
                                style = Stroke(
                                    width = data.strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    textItems.forEachIndexed { index, item ->
                        var offsetX by remember { mutableStateOf(0f) }
                        var offsetY by remember { mutableStateOf(0f) }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (item.position.x + offsetX).roundToInt(),
                                        (item.position.y + offsetY).roundToInt()
                                    )
                                }
                                .graphicsLayer {
                                    rotationZ = item.rotation
                                    scaleX = item.scale
                                    scaleY = item.scale
                                    transformOrigin = TransformOrigin.Center
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            offsetX = 0f
                                            offsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetX += dragAmount.x
                                            offsetY += dragAmount.y
                                            textItems[index] = item.copy(
                                                position = Offset(
                                                    item.position.x + dragAmount.x,
                                                    item.position.y + dragAmount.y
                                                )
                                            )
                                        }
                                    )
                                }
                                .zIndex(2f)
                        ) {
                            Text(
                                text = item.text,
                                color = item.color,
                                fontSize = item.fontSize.sp,
                                fontWeight = if (item.isBold) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    if (showCropView && imageCrop != null) {
                        imageCrop!!.ImageCropView(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(3f),
                            guideLineColor = Color.LightGray,
                            guideLineWidth = 2.dp,
                            edgeCircleSize = 5.dp,
                            showGuideLines = true,
                            cropType = CropType.SQUARE,
                            edgeType = EdgeType.CIRCULAR
                        )
                    }

                    if (showDrawingCanvas) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                        },
                                        onDrag = { change, _ ->
                                            if (isEraser) {
                                                paths.removeAll { it.path.getBounds().contains(change.position) }
                                            } else {
                                                currentPath.lineTo(change.position.x, change.position.y)
                                                paths.add(
                                                    PathData(currentPath, currentColor, currentBrushSize)
                                                )
                                            }
                                        },
                                        onDragEnd = {
                                            currentPath = Path()
                                        }
                                    )
                                }
                                .zIndex(4f)
                        ) {}
                    }

                    // Frame Overlay - APPLYING THE FRAME
                    selectedFrameResId?.let { frameResId ->
                        Image(
                            painter = painterResource(id = frameResId),
                            contentDescription = "Frame",
                            modifier = Modifier.fillMaxSize(), // Fill the whole image area
                            contentScale = ContentScale.FillBounds // Stretch frame to fill the bounds
                        )
                    }

                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            if (displayedBitmap == null && !isProcessing) {
                Text(
                    text = "No image selected",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(3f),
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
                            imageAspectRatio =
                                croppedBitmap.width.toFloat() / croppedBitmap.height.toFloat()
                            showCropView = false
                        } else {
                            if (currentFilter != null) {
                                processedBitmap = applyFilter(displayedBitmap!!, currentFilter!!)
                            }
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
            Spacer(modifier = Modifier.weight(1f))

            if (selectedTool != EditTool.NONE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedTool) {
                                    EditTool.FILTERS -> "Filters"
                                    EditTool.EFFECTS -> "Effects"
                                    EditTool.TEXT -> "Add Text"
                                    EditTool.FRAME -> "Background Frame"
                                    else -> ""
                                },
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        when (selectedTool) {
                            EditTool.FILTERS -> {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Back",
                                    tint = Color.Black, modifier = Modifier.align(Alignment.End).clickable {
                                        selectedTool = EditTool.NONE
                                    },
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    items(ImageFilterType.values()) { filterType ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            processedBitmap?.let {
                                                val previewBitmap = remember(filterType) {
                                                    applyFilter(it, filterType)
                                                }

                                                Image(
                                                    bitmap = previewBitmap.asImageBitmap(),
                                                    contentDescription = filterType.label,
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            currentFilter = filterType
                                                        }
                                                )
                                            }
                                            Text(
                                                filterType.label,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            EditTool.TEXT -> {
                                showTextDialog = true
                                selectedTool = EditTool.NONE
                            }

                            EditTool.FRAME -> {
                                val frameList = listOf(
                                    R.drawable.frame1,
                                    R.drawable.frame2,
                                    R.drawable.frame3,
                                    R.drawable.frame4,
                                    R.drawable.frame5,
                                    R.drawable.frame6,
                                    R.drawable.frame7,
                                    R.drawable.frame8,
                                    R.drawable.frame9,
                                    R.drawable.frame10,
                                    R.drawable.frame11,
                                    R.drawable.frame12,

                                )

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Select a Frame",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(frameList) { frameResId ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                                border = BorderStroke(
                                                    2.dp,
                                                    if (selectedFrameResId == frameResId) MaterialTheme.colorScheme.primary else Color.Transparent
                                                ),
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .clickable { selectedFrameResId = frameResId }
                                            ) {
                                                Image(
                                                    painter = painterResource(id = frameResId),
                                                    contentDescription = "Frame Preview",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }



                            EditTool.BRUSH -> {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Back",
                                    tint = Color.Black, modifier = Modifier.align(Alignment.End).clickable {
                                        selectedTool = EditTool.NONE
                                    },
                                )
                                if (selectedTool == EditTool.BRUSH) {
                                    BrushToolOptions(
                                        selectedColor = currentColor,
                                        brushSize = currentBrushSize,
                                        opacity = currentOpacity,
                                        onColorChange = { currentColor = it },
                                        onBrushSizeChange = { currentBrushSize = it },
                                        onOpacityChange = { currentOpacity = it }
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                }
            } else {
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
                            BackgroundRemover.bitmapForProcessing(
                                bitmap,
                                true,
                                object : OnBackgroundChangeListener {
                                    override fun onSuccess(result: Bitmap) {
                                        processedBitmap = result
                                        imageAspectRatio =
                                            result.width.toFloat() / result.height.toFloat()
                                        imageCrop = ImageCrop(result)
                                        isProcessing = false
                                    }

                                    override fun onFailed(exception: Exception) {
                                        isProcessing = false
                                        Toast.makeText(
                                            context,
                                            "Background removal failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        }
                    }

                    PhotoEditItem(icon = painterResource(id = R.drawable.crop), title = "Crop") {
                        showCropView = true
                        showDrawingCanvas = false
                    }

                    PhotoEditItem(
                        icon = painterResource(id = R.drawable.brush),
                        title = "Brush"
                    ) {
                        showCropView = false
                        showDrawingCanvas = true
                        selectedTool = EditTool.BRUSH
                    }

                    PhotoEditItem(
                        icon = painterResource(id = R.drawable.filters),
                        title = "Filters"
                    ) {
                        showDrawingCanvas = false
                        showCropView = false
                        selectedTool = EditTool.FILTERS
                    }
                    PhotoEditItem(icon = painterResource(id = R.drawable.text), title = "Text") {
                        showCropView = false
                        selectedTool = EditTool.TEXT
                        showDrawingCanvas = false
                    }

                    PhotoEditItem(
                        icon = painterResource(id = R.drawable.frame),
                        title = "Bg Frame"
                    ) {
                        showDrawingCanvas = false
                        showCropView = false
                        selectedTool = EditTool.FRAME
                    }
                }
            }
        }
    }
}


data class PathData(val path: Path, val color: Color, val strokeWidth: Float)

@Composable
fun PhotoEditItem(icon: Painter, title: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
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


@Composable
fun BrushToolOptions(
    selectedColor: Color,
    brushSize: Float,
    opacity: Float,
    onColorChange: (Color) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().height(200.dp)
            .background(Color.White),
    ) {

        Text("Brush", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = brushSize,
            onValueChange = onBrushSizeChange,
            valueRange = 1f..100f,
            modifier = Modifier.fillMaxWidth()
        )


        Text("Opacity", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )


        val colors = listOf(
            Color(0xFF2962FF),
            Color(0xFF8D6E63),
            Color(0xFF2E7D32),
            Color(0xFFFFA000),
            Color(0xFFFF5252),
            Color(0xFF000000),
            Color(0xFFFF7043),
            Color(0xFF4DD0E1),
            Color(0xFF9575CD),
            Color(0xFFFFC107)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 2.dp else 1.dp,
                            color = if (selectedColor == color) Color.DarkGray else Color.LightGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onColorChange(color) }
                )
            }
        }
    }
}

fun applyFilter(bitmap: Bitmap, filterType: ImageFilterType): Bitmap {
    val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(bmp)
    val paint = android.graphics.Paint()
    val colorMatrix = android.graphics.ColorMatrix()

    when (filterType) {
        ImageFilterType.GRAYSCALE -> colorMatrix.setSaturation(0f)

        ImageFilterType.SEPIA -> colorMatrix.set(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,    1f, 0f
        ))

        ImageFilterType.INVERT -> colorMatrix.set(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))

        ImageFilterType.BRIGHTNESS -> colorMatrix.set(floatArrayOf(
            1.3f, 0f, 0f, 0f, 30f,
            0f, 1.3f, 0f, 0f, 30f,
            0f, 0f, 1.3f, 0f, 30f,
            0f, 0f, 0f, 1f, 0f
        ))

        ImageFilterType.DARKEN -> colorMatrix.set(floatArrayOf(
            0.8f, 0f, 0f, 0f, -20f,
            0f, 0.8f, 0f, 0f, -20f,
            0f, 0f, 0.8f, 0f, -20f,
            0f, 0f, 0f, 1f, 0f
        ))

        ImageFilterType.CONTRAST -> {
            val contrast = 1.5f
            val translate = (-0.5f * contrast + 0.5f) * 255f
            colorMatrix.set(floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        ImageFilterType.SATURATE -> {
            colorMatrix.setSaturation(1.5f)
        }

        ImageFilterType.DESATURATE -> {
            colorMatrix.setSaturation(0.3f)
        }

        ImageFilterType.VINTAGE -> colorMatrix.set(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.0f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 0.8f, 0.0f, -10f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))

        else -> return bitmap
    }

    paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bmp, 0f, 0f, paint)
    return bmp
}


