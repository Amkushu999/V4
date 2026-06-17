package com.itsme.amkush.components

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.itsme.amkush.interfaces.IVideoPlayer
import com.itsme.amkush.modules.home.controllers.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun LivePlayerDialog(homeViewModel: HomeViewModel, context: Context) {
    val videoPlayer: IVideoPlayer = koinInject()
    
    if (homeViewModel.isLiveStreamingDisplay.value && homeViewModel.liveURL.value.isNotEmpty()) {
        Dialog(onDismissRequest = {
            homeViewModel.isLiveStreamingDisplay.value = false
            videoPlayer.release()
        }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().border(1.dp, Color(0xFFFF2D7E).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live Stream Preview",
                        color = Color(0xFFFF2D7E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.Black, RoundedCornerShape(12.dp)),
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) { videoPlayer.playRTMPStream(holder, homeViewModel.liveURL.value) }
                                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                    override fun surfaceDestroyed(holder: SurfaceHolder) { videoPlayer.release() }
                                })
                            }
                        }
                    )
                    Text(text = "Tap outside to close", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}