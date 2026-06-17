package com.itsme.amkush.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itsme.amkush.ui.theme.FaceGateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(onBackClick: () -> Unit) {
    var currentScreen by remember { mutableStateOf("settings") }
    
    FaceGateTheme {
        Scaffold(
            containerColor = Color(0xFF0A0A0F),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF12141C),
                    contentColor = Color(0xFF00D4FF)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentScreen == "settings",
                        onClick = { currentScreen = "settings" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D4FF),
                            selectedTextColor = Color(0xFF00D4FF),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = Color(0xFF1E2130)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.VisibilityOff, contentDescription = "HideApp") },
                        label = { Text("HideApp") },
                        selected = currentScreen == "hideapp",
                        onClick = { currentScreen = "hideapp" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D4FF),
                            selectedTextColor = Color(0xFF00D4FF),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = Color(0xFF1E2130)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Build, contentDescription = "Spoofer") },
                        label = { Text("Spoofer") },
                        selected = currentScreen == "spoofer",
                        onClick = { currentScreen = "spoofer" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D4FF),
                            selectedTextColor = Color(0xFF00D4FF),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = Color(0xFF1E2130)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Gamepad, contentDescription = "Controls") },
                        label = { Text("Controls") },
                        selected = currentScreen == "controls",
                        onClick = { currentScreen = "controls" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D4FF),
                            selectedTextColor = Color(0xFF00D4FF),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = Color(0xFF1E2130)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "settings" -> SettingsScreenContent(onBackClick = onBackClick)
                    "hideapp" -> HideAppScreen()
                    "spoofer" -> SpooferScreen()
                    "controls" -> TransformControlsScreen()
                }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(onBackClick: () -> Unit) {
    HomeScreen(onBackClick = onBackClick)
}
