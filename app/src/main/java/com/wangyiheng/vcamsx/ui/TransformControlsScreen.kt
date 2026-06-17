package com.wangyiheng.vcamsx.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wangyiheng.vcamsx.utils.MediaTransformState
import com.wangyiheng.vcamsx.utils.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformControlsScreen() {
    val context = LocalContext.current
    
    var currentScale by remember { mutableStateOf(MediaTransformState.scale) }
    var joystickOffset by remember { mutableStateOf(IntOffset.Zero) }
    
    val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
    val imageUriStr = prefs.getString("image_uri", null)
    val isVideoSelected = imageUriStr == null
    
    var isPlaying by remember { mutableStateOf(PlaybackState.isPlaying) }
    
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUriStr) {
        if (imageUriStr != null) {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUriStr))
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    previewBitmap = bmp
                } catch (e: Exception) {
                    previewBitmap = null
                }
            }
        } else {
            previewBitmap = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Media Controls", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold) 
                },
                actions = {
                    IconButton(onClick = { 
                        MediaTransformState.reset()
                        PlaybackState.reset()
                        currentScale = 1.0f
                        joystickOffset = IntOffset.Zero
                        isPlaying = true
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Reset", tint = Color(0xFFFF2D7E))
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0A0A0F))
            )
        },
        containerColor = Color(0xFF0A0A0F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .border(2.dp, Color(0xFF00D4FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF000000))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = MediaTransformState.scale,
                                    scaleY = MediaTransformState.scale,
                                    translationX = MediaTransformState.offsetX,
                                    translationY = MediaTransformState.offsetY,
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                )
                        )
                    } else if (isVideoSelected) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PlayCircleFilled, 
                                contentDescription = null, 
                                tint = Color(0xFF00D4FF), 
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "VIDEO ACTIVE", 
                                color = Color(0xFF00D4FF), 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 16.sp
                            )
                            Text(
                                "Adjusting controls here\napplies to target app", 
                                color = Color.Gray, 
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text("Select an image or video", color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            val newScale = (currentScale - 0.1f).coerceAtLeast(MediaTransformState.MIN_SCALE)
                            MediaTransformState.scale = newScale
                            currentScale = newScale
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2130))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ZOOM", color = Color(0xFF6B7280), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${"%.1f".format(currentScale)}x", color = Color(0xFF00D4FF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newScale = (currentScale + 0.1f).coerceAtMost(MediaTransformState.MAX_SCALE)
                            MediaTransformState.scale = newScale
                            currentScale = newScale
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2130))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (isVideoSelected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                PlaybackState.togglePlayback()
                                isPlaying = PlaybackState.isPlaying
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) Color(0xFF00D4FF) else Color(0xFFFF2D7E))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PLAYBACK", color = Color(0xFF6B7280), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(if (isPlaying) "PLAYING" else "PAUSED", color = if (isPlaying) Color(0xFF00FF7F) else Color(0xFFFF2D7E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C)),
                border = BorderStroke(2.dp, Color(0xFF1E2130))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0xFF0A0A0F), CircleShape)
                            .border(2.dp, Color(0xFF1E2130), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset { joystickOffset }
                                .background(Color(0xFF00D4FF), CircleShape)
                                .border(4.dp, Color(0xFF0A0A0F), CircleShape)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            joystickOffset = IntOffset.Zero
                                            MediaTransformState.offsetX = 0f
                                            MediaTransformState.offsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newX = (joystickOffset.x + dragAmount.x.roundToInt()).coerceIn(-50, 50)
                                            val newY = (joystickOffset.y + dragAmount.y.roundToInt()).coerceIn(-50, 50)
                                            joystickOffset = IntOffset(newX, newY)
                                            
                                            val panMultiplier = MediaTransformState.MAX_PAN / 50f
                                            MediaTransformState.offsetX = newX * panMultiplier
                                            MediaTransformState.offsetY = newY * panMultiplier
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("MOVE", color = Color(0xFF0A0A0F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                if (isVideoSelected) "Preview updates in real-time.\nUse Play/Pause to control target video."
                else "Preview updates in real-time.\nImage injection active.",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}