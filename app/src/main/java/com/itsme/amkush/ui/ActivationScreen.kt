package com.itsme.amkush.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itsme.amkush.utils.ActivationApi
import com.itsme.amkush.utils.ActivationPrefs
import com.itsme.amkush.utils.DeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) { }
}

private val BgDark       = Color(0xFF060B12)
private val CardDark     = Color(0xFF0C1220)
private val CyanAccent   = Color(0xFF00E5FF)
private val PinkAccent   = Color(0xFFFF2D7E)
private val PurpleAccent = Color(0xFF7C3AED)
private val TextPrimary  = Color(0xFFE0F7FA)
private val TextSecondary= Color(0xFF546E7A)
private val BorderCyan   = Color(0xFF00E5FF).copy(alpha = 0.35f)
private val BorderPurple = Color(0xFF7C3AED).copy(alpha = 0.5f)
private val BorderPink   = Color(0xFFFF2D7E).copy(alpha = 0.5f)
private val GreenSuccess = Color(0xFF00E676)
private val RedError     = Color(0xFFFF1744)

sealed class ActivationState {
    object Idle       : ActivationState()
    object Verifying  : ActivationState()
    object Success    : ActivationState()
    data class Error(val message: String) : ActivationState()
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(onActivated: () -> Unit = {}) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var keyInput   by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var state      by remember { mutableStateOf<ActivationState>(ActivationState.Idle) }
    var showSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .drawBehind {
                val step = 48.dp.toPx()
                val lineColor = Color(0xFF0D2030)
                var x = 0f
                while (x < size.width) { drawLine(lineColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 1f); x += step }
                var y = 0f
                while (y < size.height) { drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f); y += step }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF003344), Color(0xFF001A2E))))
                        .border(1.dp, BorderCyan, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "FACEGATE",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "v2.0",
                    color = CyanAccent,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.openUrl("tg://resolve?domain=facegateofficialbot&start=BuyAccess") },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, BorderCyan)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF003344), Color(0xFF2A0040))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF001A2E).copy(alpha = 0.7f))
                                .border(1.dp, BorderCyan, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "GET YOUR KEY",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "View plans & pay on Telegram",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val color = CyanAccent.copy(alpha = 0.6f)
                        val len   = 24.dp.toPx()
                        val stroke = Stroke(2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(0f, len), androidx.compose.ui.geometry.Offset(0f, 0f), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(len, 0f), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(size.width - len, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, len), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height - len), androidx.compose.ui.geometry.Offset(0f, size.height), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(len, size.height), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(size.width - len, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), 2f)
                        drawLine(color, androidx.compose.ui.geometry.Offset(size.width, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height - len), 2f)
                    }
                    .background(CardDark, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ACTIVATION KEY",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    val fieldBorder = when {
                        localError != null || state is ActivationState.Error -> BorderPink
                        keyInput.isNotEmpty() -> BorderCyan
                        else -> Color(0xFF1A2540)
                    }

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isLetterOrDigit() }.uppercase().take(13)
                            keyInput = buildString {
                                clean.forEachIndexed { i, c ->
                                    if (i in listOf(4, 8, 12)) append('-')
                                    append(c)
                                }
                            }
                            localError = null
                            if (state is ActivationState.Error) state = ActivationState.Idle
                        },
                        placeholder = {
                            Text(
                                "XXXX - XXXX - XXXX - X",
                                color = Color(0xFF2A3A50),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor            = TextPrimary,
                            cursorColor          = CyanAccent,
                            focusedBorderColor   = CyanAccent.copy(alpha = 0.7f),
                            unfocusedBorderColor = fieldBorder,
                            containerColor       = Color(0xFF050D18)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily  = FontFamily.Monospace,
                            fontSize    = 18.sp,
                            letterSpacing = 2.sp,
                            textAlign   = TextAlign.Center,
                            color       = TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction      = ImeAction.Done
                        )
                    )

                    val errorMsg = localError
                        ?: (state as? ActivationState.Error)?.message

                    AnimatedVisibility(
                        visible = errorMsg != null,
                        enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                        exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PinkAccent.copy(alpha = 0.08f))
                                .border(1.dp, PinkAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg ?: "",
                                color = PinkAccent,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    val isVerifying = state is ActivationState.Verifying

                    Button(
                        onClick = {
                            val clean = keyInput.filter { it.isLetterOrDigit() }
                            if (clean.length < 13) {
                                localError = "Key too short — check your Telegram bot."
                                return@Button
                            }
                            localError = null
                            state = ActivationState.Verifying

                            scope.launch {
                                val deviceId = DeviceId.get(context)
                                val result   = withContext(Dispatchers.IO) {
                                    ActivationApi.validateKey(context, keyInput.replace("-", ""), deviceId)
                                }
                                val valid = result.optBoolean("valid", false)
                                if (valid) {
                                    val token = result.optString("token", keyInput)
                                    ActivationPrefs.saveToken(context, token)
                                    state       = ActivationState.Success
                                    showSuccess = true
                                } else {
                                    val err = result.optString("error", "Invalid Key — Null in db")
                                    state = ActivationState.Error(err)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isVerifying
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF003344), Color(0xFF1A0040), Color(0xFF003344))
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(CyanAccent.copy(0.6f), PurpleAccent.copy(0.6f))),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isVerifying,
                                transitionSpec = { fadeIn(tween(250)) with fadeOut(tween(200)) },
                                label = "btnContent"
                            ) { verifying ->
                                if (verifying) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier  = Modifier.size(20.dp),
                                            color     = CyanAccent,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "VERIFYING...",
                                            color      = CyanAccent,
                                            fontSize   = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "ACTIVATE KEY",
                                            color      = CyanAccent,
                                            fontSize   = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderPurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("HOW IT WORKS", color = PurpleAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                        HowStep("01", "Open Bot", "Visit Telegram")
                        HowStep("02", "Purchase Plan", "Pick a tier")
                        HowStep("03", "Activate", "Paste key here")
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderPink)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("FACEGATE", color = PinkAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                        FaceGateLink(
                            label   = "FaceGate Group",
                            color   = Color(0xFF004D4D),
                            iconTint = CyanAccent,
                            icon    = Icons.Default.Tag,
                            url     = "https://t.me/+Tx-rhbl-VcgyNDg0"
                        )
                        FaceGateLink(
                            label   = "FaceGate Channel",
                            color   = Color(0xFF4A0030),
                            iconTint = PinkAccent,
                            icon    = Icons.Default.Person,
                            url     = "https://t.me/facegateofficial"
                        )
                        FaceGateLink(
                            label   = "FaceGate Bot",
                            color   = Color(0xFF2A3300),
                            iconTint = Color(0xFFCDDC39),
                            icon    = Icons.Default.SmartToy,
                            url     = "tg://resolve?domain=facegateofficialbot&start=BuyAccess"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showSuccess) {
            SuccessDialog(onDismiss = { showSuccess = false; onActivated() })
        }
    }
}

@Composable
private fun HowStep(num: String, title: String, sub: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(num, color = PurpleAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.width(24.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun FaceGateLink(
    label: String,
    color: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    url: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { context.openUrl(url) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace)
        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    var tickProgress by remember { mutableStateOf(0f) }
    var showContent  by remember { mutableStateOf(false) }

    val animatedTick by animateFloatAsState(
        targetValue = tickProgress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "tick"
    )

    val ringScale by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "ringScale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        tickProgress = 1f
        delay(400)
        showContent = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1220)),
                border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().scale(ringScale)) {
                            drawCircle(
                                color  = GreenSuccess.copy(alpha = 0.15f),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                color  = GreenSuccess.copy(alpha = 0.4f),
                                radius = size.minDimension / 2f,
                                style  = Stroke(2f)
                            )
                        }
                        Canvas(modifier = Modifier.size(72.dp)) {
                            drawCircle(color = GreenSuccess.copy(alpha = 0.15f))
                            drawCircle(color = GreenSuccess, style = Stroke(3f))
                            if (animatedTick > 0f) {
                                val path = Path()
                                val cx   = size.width / 2f
                                val cy   = size.height / 2f
                                val p1 = androidx.compose.ui.geometry.Offset(cx - 16f, cy)
                                val p2 = androidx.compose.ui.geometry.Offset(cx - 4f,  cy + 12f)
                                val p3 = androidx.compose.ui.geometry.Offset(cx + 18f, cy - 14f)

                                val fullLen = dist(p1, p2) + dist(p2, p3)
                                val drawn   = fullLen * animatedTick

                                if (drawn <= dist(p1, p2)) {
                                    val t  = drawn / dist(p1, p2)
                                    path.moveTo(p1.x, p1.y)
                                    path.lineTo(lerp(p1.x, p2.x, t), lerp(p1.y, p2.y, t))
                                } else {
                                    val rem = drawn - dist(p1, p2)
                                    val t   = rem / dist(p2, p3)
                                    path.moveTo(p1.x, p1.y)
                                    path.lineTo(p2.x, p2.y)
                                    path.lineTo(lerp(p2.x, p3.x, t), lerp(p2.y, p3.y, t))
                                }

                                drawPath(
                                    path  = path,
                                    color = GreenSuccess,
                                    style = Stroke(
                                        width = 6f,
                                        cap   = StrokeCap.Round,
                                        join  = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showContent,
                        enter   = fadeIn(tween(400)) + expandVertically(tween(400))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "ACTIVATED!",
                                color      = GreenSuccess,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "FaceGate is now unlocked.",
                                color    = TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    AnimatedVisibility(visible = showContent, enter = fadeIn(tween(500))) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape  = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                        ) {
                            Text(
                                "CONTINUE",
                                color      = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dist(a: androidx.compose.ui.geometry.Offset, b: androidx.compose.ui.geometry.Offset) =
    Math.sqrt(((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)).toDouble()).toFloat()

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)