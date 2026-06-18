package com.itsme.amkush.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Process
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.itsme.amkush.data.models.AppInfo
import com.itsme.amkush.data.models.VideoStatues
import com.itsme.amkush.utils.ActivationApi
import com.itsme.amkush.utils.ActivationPrefs
import com.itsme.amkush.utils.InfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BgDark        = Color(0xFF0A0A0F)
private val CardDark      = Color(0xFF12141C)
private val CardDarker    = Color(0xFF0E1018)
private val CyanAccent    = Color(0xFF00D4FF)
private val PinkAccent    = Color(0xFFFF2D7E)
private val TextPrimary   = Color(0xFFE8EAF0)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor   = Color(0xFF1E2130)
private val PurpleRing    = Color(0xFF7C3AED)
private val GreenActive   = Color(0xFF00FF7F)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FaceGateScreen(
    onShowActivation: () -> Unit,
    onHookSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTarget by remember { mutableStateOf<AppInfo?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var hookingState by remember { mutableStateOf(false) }
    var hookedState by remember { mutableStateOf(false) }

    // Adjusted rotation baseline timing upwards from 3000ms to 12000ms for a more premium, slower speed
    val fgRotation by rememberInfiniteTransition(label = "fgSpin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 12000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "fgRot"
    )

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    Canvas(modifier = Modifier.size(60.dp)) {
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(Color(0xFF7C3AED), Color(0xFF00D4FF), Color(0xFFFF2D7E), Color(0xFF7C3AED))),
                            radius = size.minDimension / 2f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                        )
                    }
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF1A1A2E)), contentAlignment = Alignment.Center) {
                        Text(text = "FG", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.rotate(fgRotation))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "FACEGATE", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(text = "Camera Access System", color = CyanAccent, fontSize = 12.sp, letterSpacing = 0.5.sp)
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1A1030)).border(1.dp, PurpleRing.copy(alpha = 0.5f), CircleShape).clickable { }) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = PurpleRing, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable(enabled = selectedTarget == null) { showAppPicker = true },
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(0.5.dp, if (selectedTarget != null) PinkAccent.copy(alpha = 0.4f) else BorderColor)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (selectedTarget == null) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A2E)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SELECT TARGET", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 1.sp)
                            Text("Choose app to hook camera", color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    } else {
                        AppIconImage(drawable = selectedTarget!!.icon, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TARGET LOCKED", color = PinkAccent, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp)
                            Text(selectedTarget!!.label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF3D1020)).clickable {
                            selectedTarget = null; hookingState = false; hookedState = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = PinkAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { 
                    val pm = context.packageManager
                    val intent = pm.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        val restartIntent = Intent.makeRestartActivityTask(intent.component)
                        context.startActivity(restartIntent)
                        Process.killProcess(Process.myPid())
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(52.dp), 
                shape = RoundedCornerShape(14.dp), 
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)), 
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restart System", fontSize = 14.sp, letterSpacing = 0.5.sp)
            }

            OutlinedButton(
                onClick = { 
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/facegateofficial"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }, 
                modifier = Modifier.fillMaxWidth().height(52.dp), 
                shape = RoundedCornerShape(14.dp), 
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)), 
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Support & Updates", fontSize = 14.sp, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = Triple(selectedTarget != null, hookingState, hookedState),
                transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(200)) }, label = "bottomBtn"
            ) { (hasTarget, isHooking, isHooked) ->
                when {
                    isHooking -> {
                        Card(modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardDarker), border = BorderStroke(1.dp, PinkAccent.copy(alpha = 0.3f))) {
                            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = CyanAccent, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("HOOKING CAMERA", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
                                    Text("Verifying & Injecting...", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    isHooked -> {
                        Button(onClick = { hookedState = false }, modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenActive)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("CAMERA HOOKED", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp, color = Color.Black)
                                Text("Tap to configure settings", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f))
                            }
                        }
                    }
                    hasTarget -> {
                        Button(
                            onClick = {
                                hookingState = true
                                scope.launch {
                                    val isPaid = withContext(Dispatchers.IO) {
                                        val token = ActivationPrefs.getToken(context)
                                        if (token != null) ActivationApi.checkToken(context, token) else false
                                    }

                                    if (!isPaid) {
                                        hookingState = false
                                        onShowActivation()
                                        return@launch
                                    }

                                    val infoManager = InfoManager(context)
                                    val currentStatus = infoManager.getVideoStatus() ?: VideoStatues()
                                    infoManager.saveVideoStatus(currentStatus.copy(targetPackageName = selectedTarget!!.packageName))

                                    delay(2000)
                                    hookingState = false
                                    hookedState = true
                                    
                                    delay(1500)
                                    onHookSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("HOOK CAMERA", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp, color = Color.White)
                                Text("Tap to inject camera hook", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                    else -> {
                        Button(onClick = { showAppPicker = true }, modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = CardDarker), border = BorderStroke(1.dp, BorderColor)) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(22.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SELECT TARGET")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showAppPicker) {
            AppPickerSheet(onDismiss = { showAppPicker = false }, onAppSelected = { app ->
                selectedTarget = app; showAppPicker = false; hookedState = false; hookingState = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(onDismiss: () -> Unit, onAppSelected: (AppInfo) -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val appList = packages
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 }
                .mapNotNull {
                    try { AppInfo(label = pm.getApplicationLabel(it).toString(), packageName = it.packageName, icon = pm.getApplicationIcon(it.packageName)) } catch (_: Exception) { null }
                }.sortedBy { it.label.lowercase() }
            withContext(Dispatchers.Main) { apps = appList; isLoading = false }
        }
    }

    val filtered = remember(apps, searchQuery) { 
        if (searchQuery.isBlank()) apps 
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) } 
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(onClick = onDismiss), contentAlignment = Alignment.BottomCenter) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.78f).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(Color(0xFF12141C)).clickable(enabled = false) {}) {
            Box(modifier = Modifier.padding(top = 12.dp).width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(TextSecondary.copy(alpha = 0.4f)).align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "SELECT TARGET APP", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(14.dp))
            
            OutlinedTextField(
                value = searchQuery, 
                onValueChange = { searchQuery = it }, 
                placeholder = { Text("Search apps...", color = TextSecondary, fontSize = 14.sp) }, 
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) }, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                shape = RoundedCornerShape(14.dp), 
                // Migrated layout colors mapping logic to standard non-deprecated M3 specs to fix recomposition listening bugs
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CyanAccent,
                    focusedBorderColor = CyanAccent.copy(alpha = 0.6f),
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardDarker,
                    unfocusedContainerColor = CardDarker
                ), 
                singleLine = true, 
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            if (!isLoading) Text(text = "${filtered.size} apps found", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            if (isLoading) Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CyanAccent, strokeWidth = 2.dp, modifier = Modifier.size(32.dp)) } 
            else LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { items(filtered, key = { it.packageName }) { app -> AppListItem(app = app, onSelect = { onAppSelected(app) }) } }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onSelect: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val itemScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "itemScale")
    Row(modifier = Modifier.fillMaxWidth().scale(itemScale).clip(RoundedCornerShape(12.dp)).background(if (pressed) Color(0xFF1A1C28) else Color.Transparent).clickable { pressed = true; onSelect() }.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        AppIconImage(drawable = app.icon, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)))
        Spacer(modifier = Modifier.width(14.dp))
        Text(app.label, color = TextPrimary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun AppIconImage(drawable: Drawable?, modifier: Modifier = Modifier) {
    if (drawable != null) {
        val bitmap = remember(drawable) { drawable.toBitmap(96, 96) }
        Image(painter = BitmapPainter(bitmap.asImageBitmap()), contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier.background(Color(0xFF1E2130)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Android, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
        }
    }
}
