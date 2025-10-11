package com.dagimg.expensms.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dagimg.expensms.ui.theme.AppColors
import com.dagimg.expensms.ui.theme.AppTheme
import com.dagimg.expensms.ui.theme.LocalAppTheme

@Composable
fun ThemeToggleButton(
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTheme = LocalAppTheme.current
    val isDarkTheme = currentTheme == AppTheme.DARK

    val animatedPosition by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "toggle_position",
    )

    val buttonSize = 44.dp
    val toggleSize = 20.dp
    val padding = 2.dp

    Box(
        modifier =
            modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(AppColors.Card.copy(alpha = 0.8f))
                .clickable { onThemeChange(if (isDarkTheme) "light" else "dark") }
                .padding(padding),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Background track
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(AppColors.Muted),
        )

        // Animated toggle
        Box(
            modifier =
                Modifier
                    .size(toggleSize)
                    .graphicsLayer {
                        translationX = animatedPosition * (buttonSize - toggleSize - padding * 2).toPx()
                    }.clip(CircleShape)
                    .background(AppColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            // Sun/Moon icon
            Text(
                text = if (isDarkTheme) "🌙" else "☀️",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
