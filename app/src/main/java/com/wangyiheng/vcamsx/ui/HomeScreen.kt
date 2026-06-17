package com.wangyiheng.vcamsx.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wangyiheng.vcamsx.components.LivePlayerDialog
import com.wangyiheng.vcamsx.components.SettingRow
import com.wangyiheng.vcamsx.components.VideoPlayerDialog
import com.wangyiheng.vcamsx.interfaces.IVideoFileManager
import com.wangyiheng.vcamsx.modules.home.controllers.HomeViewModel
import com.wangyiheng.vcamsx.utils.MediaTransformState
import com.wangyiheng.vcamsx.utils.PlaybackState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val homeViewModel: HomeViewModel = viewModel()
    val videoFileManager: IVideoFileManager = koinInject()

    LaunchedEffect(Unit) {
        homeViewModel.init()
        MediaTransformState.initUI(context)
    }
    val videoPath = videoFileManager.getVideoPath()

    val selectVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                videoFileManager.copyVideoToAppDir(it)
                val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("image_uri").apply()
                MediaTransformState.reset()
                PlaybackState.reset()
                Toast.makeText(context, "Video Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Save Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) selectVideoLauncher.launch("video/*")
            else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) Toast.makeText(context, "Permission Required", Toast.LENGTH_SHORT).show()
            else selectVideoLauncher.launch("video/*")
        }
    )

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                videoFileManager.deleteSavedVideo()
                val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("image_uri", it.toString()).apply()
                MediaTransformState.reset()
                Toast.makeText(context, "Image Selected! Hook will inject this.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Image Save Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Camera Settings", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF00D4FF))
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0A0A0F))
            )
        },
        containerColor = Color(0xFF0A0A0F)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFF0A0A0F))) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = homeViewModel.liveURL.value,
                    onValueChange = { homeViewModel.liveURL.value = it },
                    label = { Text("RTMP/RTSP Stream URL", color = Color.Gray) },
                    placeholder = { Text("Tap to type or paste link...", color = Color.DarkGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (homeViewModel.liveURL.value.isNotBlank()) {
                                homeViewModel.saveState()
                                Toast.makeText(context, "Stream URL Saved Successfully!", Toast.LENGTH_SHORT).show()
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF00D4FF),
                        focusedBorderColor = Color(0xFF00D4FF),
                        unfocusedBorderColor = Color(0xFF1E2130)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                NeonButton(text = "Select Local Video", borderColor = Color(0xFF00D4FF), textColor = Color(0xFF00D4FF)) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                NeonButton(text = "Preview Local Video", borderColor = Color(0xFFFF2D7E), textColor = Color(0xFFFF2D7E)) {
                    homeViewModel.toggleVideoDisplay()
                }

                NeonButton(text = "Preview Live Stream", borderColor = Color(0xFFFF2D7E), textColor = Color(0xFFFF2D7E)) {
                    homeViewModel.toggleLiveStreamDisplay()
                }

                NeonButton(text = "Select Local Image", borderColor = Color(0xFF00FF7F), textColor = Color(0xFF00FF7F)) {
                    selectImageLauncher.launch("image/*")
                }

                Spacer(modifier = Modifier.height(8.dp))

                SettingRow(label = "Inject Local Video", checkedState = homeViewModel.isVideoEnabled, onCheckedChange = { homeViewModel.saveState() }, context = context)
                SettingRow(label = "Inject Live Stream", checkedState = homeViewModel.isLiveStreamingEnabled, onCheckedChange = { homeViewModel.saveState() }, context = context)
                SettingRow(label = "Stream Audio", checkedState = homeViewModel.isVolumeEnabled, onCheckedChange = { homeViewModel.saveState() }, context = context)
                SettingRow(label = if (homeViewModel.codecType.value) "Hardware Decoding" else "Software Decoding", checkedState = homeViewModel.codecType, onCheckedChange = { homeViewModel.saveState() }, context = context)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    LivePlayerDialog(homeViewModel, context)
    VideoPlayerDialog(homeViewModel, context, videoPath)
}

@Composable
fun NeonButton(text: String, borderColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth().height(52.dp).border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12141C))
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
