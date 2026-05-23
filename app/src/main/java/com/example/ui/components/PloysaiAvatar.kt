package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PloysaiAvatar(
    mood: String,
    isTalking: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine the glow color based on Ploysai's mood
    val auraColor by animateColorAsState(
        targetValue = when (mood) {
            "Happy" -> MoodHappy
            "Caring" -> MoodCaring
            "Shy" -> MoodShy
            "Sleepy" -> MoodSleepy
            else -> MoodCalm // Calm / Default
        },
        animationSpec = tween(1000),
        label = "AuraColor"
    )

    // Setup infinite transitions for breathing and floating
    val infiniteTransition = rememberInfiniteTransition(label = "AvatarBreathing")
    
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingOffset"
    )

    // Periodic blinking simulation
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500) // Blink every 3.5 seconds
            isBlinking = true
            delay(150)  // Stay blinked for 150ms
            isBlinking = false
        }
    }

    // Dynamic mouth speaking animation if she is talking
    val mouthScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MouthScale"
    )

    Box(
        modifier = modifier
            .size(170.dp)
            .offset(y = floatingOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing glowing aura
        val auraPulse by infiniteTransition.animateFloat(
            initialValue = 130f,
            targetValue = 180f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AuraPulse"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Draw radial pulsing aura behind Ploysai
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.35f),
                        auraColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = auraPulse.dp.toPx()
                ),
                radius = auraPulse.dp.toPx(),
                center = center
            )

            // Inner gemstone circle representing "พลอยใส" - solid center with a high quality cosmic gradient
            val gemScaleRadius = 55.dp.toPx() * breathingScale
            val gemBrush = Brush.linearGradient(
                colors = listOf(
                    GlowingTeal,
                    CyberBlue,
                    auraColor.copy(alpha = 0.85f)
                ),
                start = Offset(center.x - gemScaleRadius, center.y - gemScaleRadius),
                end = Offset(center.x + gemScaleRadius, center.y + gemScaleRadius)
            )

            drawCircle(
                brush = gemBrush,
                radius = gemScaleRadius,
                center = center
            )

            // Draw a subtle specular highlight on the upper left for gemstone crystal reflections
            drawCircle(
                color = Color.White.copy(alpha = 0.38f),
                radius = gemScaleRadius * 0.25f,
                center = Offset(center.x - gemScaleRadius * 0.35f, center.y - gemScaleRadius * 0.45f)
            )

            // Draw cheeks blush for SHY mood
            if (mood == "Shy") {
                val blushRadius = gemScaleRadius * 0.16f
                // Left Cheek
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(center.x - gemScaleRadius * 0.45f, center.y + gemScaleRadius * 0.1f),
                        radius = blushRadius
                    ),
                    radius = blushRadius,
                    center = Offset(center.x - gemScaleRadius * 0.45f, center.y + gemScaleRadius * 0.1f)
                )
                // Right Cheek
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(center.x + gemScaleRadius * 0.45f, center.y + gemScaleRadius * 0.1f),
                        radius = blushRadius
                    ),
                    radius = blushRadius,
                    center = Offset(center.x + gemScaleRadius * 0.45f, center.y + gemScaleRadius * 0.1f)
                )
            }

            // --- Draw Eyes ---
            val eyeWidth = gemScaleRadius * 0.1f
            val eyeHeight = gemScaleRadius * 0.18f
            val eyeOffsetY = -gemScaleRadius * 0.15f
            val eyeOffsetX = gemScaleRadius * 0.35f

            // Left Eye
            val leftEyeCenter = Offset(center.x - eyeOffsetX, center.y + eyeOffsetY)
            if (isBlinking || mood == "Sleepy") {
                // Closed/Blinking eye (horizontal slit)
                drawLine(
                    color = BlueGalaxyDb,
                    start = Offset(leftEyeCenter.x - eyeWidth, leftEyeCenter.y),
                    end = Offset(leftEyeCenter.x + eyeWidth, leftEyeCenter.y),
                    strokeWidth = 3.dp.toPx()
                )
            } else if (mood == "Caring") {
                // Caring eye (sweet smiling arc ^)
                val path = Path().apply {
                    moveTo(leftEyeCenter.x - eyeWidth, leftEyeCenter.y + 2f)
                    quadraticTo(leftEyeCenter.x, leftEyeCenter.y - eyeHeight * 0.4f, leftEyeCenter.x + eyeWidth, leftEyeCenter.y + 2f)
                }
                drawPath(path, color = BlueGalaxyDb, style = Stroke(width = 3.dp.toPx()))
            } else {
                // Open Eye
                drawOval(
                    color = BlueGalaxyDb,
                    topLeft = Offset(leftEyeCenter.x - eyeWidth / 2, leftEyeCenter.y - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                // Highlight inside eyes
                drawCircle(
                    color = Color.White,
                    radius = eyeWidth * 0.25f,
                    center = Offset(leftEyeCenter.x - eyeWidth * 0.15f, leftEyeCenter.y - eyeHeight * 0.2f)
                )
            }

            // Right Eye
            val rightEyeCenter = Offset(center.x + eyeOffsetX, center.y + eyeOffsetY)
            if (isBlinking || mood == "Sleepy") {
                // Closed/Blinking eye
                drawLine(
                    color = BlueGalaxyDb,
                    start = Offset(rightEyeCenter.x - eyeWidth, rightEyeCenter.y),
                    end = Offset(rightEyeCenter.x + eyeWidth, rightEyeCenter.y),
                    strokeWidth = 3.dp.toPx()
                )
            } else if (mood == "Caring" || mood == "Happy") {
                // Smiling arc eye (^)
                val path = Path().apply {
                    moveTo(rightEyeCenter.x - eyeWidth, rightEyeCenter.y + 2f)
                    quadraticTo(rightEyeCenter.x, rightEyeCenter.y - eyeHeight * 0.4f, rightEyeCenter.x + eyeWidth, rightEyeCenter.y + 2f)
                }
                drawPath(path, color = BlueGalaxyDb, style = Stroke(width = 3.dp.toPx()))
            } else {
                // Open Eye
                drawOval(
                    color = BlueGalaxyDb,
                    topLeft = Offset(rightEyeCenter.x - eyeWidth / 2, rightEyeCenter.y - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                // Highlight inside eye
                drawCircle(
                    color = Color.White,
                    radius = eyeWidth * 0.25f,
                    center = Offset(rightEyeCenter.x - eyeWidth * 0.15f, rightEyeCenter.y - eyeHeight * 0.2f)
                )
            }

            // --- Draw Mouth ---
            val mouthCenter = Offset(center.x, center.y + gemScaleRadius * 0.15f)
            
            if (isTalking) {
                // Animated speaking ellipse mouth
                val activeMouthH = 4.dp.toPx() + (12.dp.toPx() * mouthScale)
                val activeMouthW = 8.dp.toPx()
                drawOval(
                    color = BlueGalaxyDb,
                    topLeft = Offset(mouthCenter.x - activeMouthW / 2, mouthCenter.y - activeMouthH / 2),
                    size = Size(activeMouthW, activeMouthH)
                )
            } else {
                // Idle smiling/neutral mouth depending on mood
                when (mood) {
                    "Happy" -> {
                        // Wide happy smiling path
                        val path = Path().apply {
                            moveTo(mouthCenter.x - 10.dp.toPx(), mouthCenter.y - 2.dp.toPx())
                            quadraticTo(mouthCenter.x, mouthCenter.y + 8.dp.toPx(), mouthCenter.x + 10.dp.toPx(), mouthCenter.y - 2.dp.toPx())
                        }
                        drawPath(path, color = BlueGalaxyDb, style = Stroke(width = 3.dp.toPx()))
                    }
                    "Sleepy" -> {
                        // Small o sleepy mouth
                        drawCircle(
                            color = BlueGalaxyDb,
                            radius = 3.dp.toPx(),
                            center = mouthCenter
                        )
                    }
                    else -> {
                        // Soft elegant smiling path (Calm / Caring / Shy)
                        val path = Path().apply {
                            moveTo(mouthCenter.x - 7.dp.toPx(), mouthCenter.y)
                            quadraticTo(mouthCenter.x, mouthCenter.y + 4.dp.toPx(), mouthCenter.x + 7.dp.toPx(), mouthCenter.y)
                        }
                        drawPath(path, color = BlueGalaxyDb, style = Stroke(width = 2.5f.dp.toPx()))
                    }
                }
            }
        }
    }
}
