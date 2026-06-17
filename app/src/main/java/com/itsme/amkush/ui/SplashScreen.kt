package com.itsme.amkush.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsme.amkush.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    var targetScale by remember { mutableStateOf(0f) }
    var targetAlpha by remember { mutableStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "iconScale"
    )
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 600),
        label = "iconAlpha"
    )

    LaunchedEffect(Unit) {
        targetScale = 1f   // Kicks off the scale frame interpolation
        targetAlpha = 1f   // Kicks off the fade alpha interpolation
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF12141C)) // Sleek dark surface matching FaceGate dashboard aesthetic
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
