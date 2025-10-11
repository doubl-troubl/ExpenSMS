package com.dagimg.expensms.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagimg.expensms.R
import com.dagimg.expensms.ui.theme.AppColors
import kotlin.math.*

@Composable
fun SplashScreen(
    isLoading: Boolean = true,
    loadingMessage: String = "Loading...",
    onAnimationComplete: () -> Unit = {},
) {
    val isDark =
        AppColors.Background ==
            androidx.compose.ui.graphics
                .Color(0xFF1A1A1A)

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Rotating animation for the outer ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotation",
    )

    // Pulsing animation for the logo
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse",
    )

    // Fade in animation for text
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "textFade",
    )

    // Wave animation for loading dots
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "wave",
    )

    // Background gradient colors
    val backgroundColors =
        if (isDark) {
            listOf(
                Color(0xFF0F0F0F), // Deep black
                Color(0xFF1A1A2E), // Dark purple
                Color(0xFF16213E), // Dark blue
                Color(0xFF0F0F0F), // Deep black
            )
        } else {
            listOf(
                Color(0xFFF8FAFC), // Light gray
                Color(0xFFE0E7FF), // Light purple
                Color(0xFFDEF7FF), // Light blue
                Color(0xFFF8FAFC), // Light gray
            )
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.radialGradient(
                            colors = backgroundColors,
                            center = Offset(0.5f, 0.4f),
                            radius = 1000f,
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Background particles/stars
        BackgroundParticles(isDark = isDark)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Main logo container with rotating ring
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Outer rotating ring
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .rotate(rotation),
                ) {
                    drawRotatingRing(isDark)
                }

                // Inner pulsing logo
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            if (isDark) {
                                                listOf(
                                                    Color(0xFF4F46E5), // Indigo
                                                    Color(0xFF7C3AED), // Purple
                                                    Color(0xFF2563EB), // Blue
                                                )
                                            } else {
                                                listOf(
                                                    Color(0xFF6366F1), // Indigo
                                                    Color(0xFF8B5CF6), // Purple
                                                    Color(0xFF3B82F6), // Blue
                                                )
                                            },
                                    ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Birr currency icon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_birr),
                        contentDescription = "Birr currency symbol",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name with elegant typography
            Text(
                text = "ExpenSMS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1F2937),
                modifier = Modifier.alpha(textAlpha),
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smart SMS-Based Expense Tracker",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280),
                modifier = Modifier.alpha(textAlpha * 0.8f),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator (only show when loading)
            if (isLoading) {
                LoadingDots(waveOffset = waveOffset, isDark = isDark)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = loadingMessage,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF9CA3AF),
                    modifier = Modifier.alpha(textAlpha * 0.6f),
                )
            }
        }

        // Bottom branding
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Made with ❤️ for Ethiopian Banking",
                fontSize = 11.sp,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFFAB8FDB),
                modifier = Modifier.alpha(textAlpha * 0.5f),
            )
        }
    }
}

@Composable
private fun BackgroundParticles(isDark: Boolean) {
    val particleColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = 50
        val width = size.width
        val height = size.height

        repeat(particleCount) { i ->
            val x = (i * 137.5f) % width // Golden ratio for even distribution
            val y = (i * 234.7f) % height
            val radius = (i % 3 + 1) * 1.5f

            drawCircle(
                color = particleColor,
                radius = radius,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun LoadingDots(
    waveOffset: Float,
    isDark: Boolean,
) {
    val dotColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF6366F1)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val animatedScale = 1f + 0.3f * sin(waveOffset + index * PI.toFloat() / 2)

            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(dotColor),
            )
        }
    }
}

private fun DrawScope.drawRotatingRing(isDark: Boolean) {
    val strokeWidth = 4.dp.toPx()
    val radius = size.minDimension / 2 - strokeWidth

    val colors =
        if (isDark) {
            listOf(
                Color(0xFF4F46E5).copy(alpha = 0.8f),
                Color(0xFF7C3AED).copy(alpha = 0.6f),
                Color(0xFF2563EB).copy(alpha = 0.4f),
                Color.Transparent,
            )
        } else {
            listOf(
                Color(0xFF6366F1).copy(alpha = 0.6f),
                Color(0xFF8B5CF6).copy(alpha = 0.4f),
                Color(0xFF3B82F6).copy(alpha = 0.3f),
                Color.Transparent,
            )
        }

    drawCircle(
        brush = Brush.sweepGradient(colors),
        radius = radius,
        style =
            androidx.compose.ui.graphics.drawscope
                .Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
}

