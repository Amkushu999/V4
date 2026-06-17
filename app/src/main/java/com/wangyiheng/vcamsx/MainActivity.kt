package com.wangyiheng.vcamsx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.wangyiheng.vcamsx.navigation.NavDirection
import com.wangyiheng.vcamsx.navigation.Screen
import com.wangyiheng.vcamsx.navigation.ScreenTransition
import com.wangyiheng.vcamsx.navigation.rememberNavController
import com.wangyiheng.vcamsx.ui.ActivationScreen
import com.wangyiheng.vcamsx.ui.FaceGateScreen
import com.wangyiheng.vcamsx.ui.HomeScreen
import com.wangyiheng.vcamsx.ui.SplashScreen
import com.wangyiheng.vcamsx.ui.theme.FaceGateTheme
import com.wangyiheng.vcamsx.utils.AntiTamperManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🛡️ ANTI-TAMPER CHECK (Runs before UI loads)
        // ⚠️ COMMENTED OUT FOR DEBUG TESTING
        // Uncomment this line ONLY when building your final signed release APK
        // AntiTamperManager.checkAndEnforce(this)

        setContent {
            FaceGateTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    
                    // 🆕 SPLASH SCREEN STATE
                    var showSplash by remember { mutableStateOf(true) }
                    
                    if (showSplash) {
                        // Show the animated splash screen for 2 seconds
                        SplashScreen(onSplashFinished = {
                            showSplash = false
                        })
                    } else {
                        // 🎨 Initialize the custom Navigation Controller
                        val navController = rememberNavController(initial = Screen.FACE_GATE)
                        
                        // State for the Payment Overlay (Activation Screen)
                        var showActivation by remember { mutableStateOf(false) }

                        // 🚀 Wrap the screens in the ScreenTransition host for smooth sliding animations
                        ScreenTransition(controller = navController) { screen ->
                            when (screen) {
                                Screen.FACE_GATE -> {
                                    FaceGateScreen(
                                        onShowActivation = { 
                                            // Triggered when user taps "HOOK CAMERA" but hasn't paid
                                            showActivation = true 
                                        },
                                        onHookSuccess = { 
                                            // Triggered when user is paid and taps "HOOK CAMERA"
                                            // Slide FORWARD to Settings Screen
                                            navController.navigateTo(Screen.HOME, NavDirection.FORWARD) 
                                        }
                                    )
                                }
                                Screen.HOME -> {
                                    HomeScreen(
                                        onBackClick = { 
                                            // Slide BACKWARD to Target Selection (FaceGate)
                                            navController.goBack(Screen.FACE_GATE) 
                                        }
                                    )
                                }
                            }
                        }

                        // 💳 Overlay Payment Screen if needed
                        if (showActivation) {
                            ActivationScreen(
                                onActivated = { 
                                    showActivation = false 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}