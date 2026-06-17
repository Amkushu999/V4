package com.itsme.amkush.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*

enum class Screen {
    FACE_GATE,
    HOME
}

enum class NavDirection { FORWARD, BACKWARD }

class NavController<T>(initial: T) {
    var current   by mutableStateOf(initial)
    var direction by mutableStateOf(NavDirection.FORWARD)

    fun navigateTo(screen: T, dir: NavDirection = NavDirection.FORWARD) {
        direction = dir
        current   = screen
    }

    fun goBack(screen: T) = navigateTo(screen, NavDirection.BACKWARD)
}

@Composable
fun <T> rememberNavController(initial: T) = remember { NavController(initial) }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> ScreenTransition(
    controller: NavController<T>,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState   = controller.current,
        transitionSpec = {
            when (controller.direction) {
                NavDirection.FORWARD ->
                    (slideInHorizontally(
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        initialOffsetX = { it }
                    ) + fadeIn(tween(300))) with (
                        slideOutHorizontally(
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            targetOffsetX  = { -it / 3 }
                        ) + fadeOut(tween(250))
                    )
                NavDirection.BACKWARD ->
                    (slideInHorizontally(
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it }
                    ) + fadeIn(tween(300))) with (
                        slideOutHorizontally(
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            targetOffsetX  = { it }
                        ) + fadeOut(tween(250))
                    )
            }
        },
        label = "screenTransition"
    ) { screen ->
        content(screen)
    }
}