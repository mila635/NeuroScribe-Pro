package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MindViolet
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.RetentionGold
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Aesthetic, Fully Animated Splash Screen.
 * Replaces static logo imagery with a procedural vector animation:
 * - Harmonically undulating neural waveforms
 * - Orbiting synaptic nodes and quantum pulse filaments
 * - Breathing aperture halo rings
 * - Smooth luxury typography entrance
 */
@Composable
fun SplashScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.72f) }
    val alphaAnim = remember { Animatable(0.0f) }
    val textAlphaAnim = remember { Animatable(0.0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_synaptic_engine")

    // Continuous Wave Phase Animation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Orbital Node Rotation
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    // Breathing Halo Rings
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    val coreGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_glow"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        textAlphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, delayMillis = 100, easing = FastOutSlowInEasing)
        )
        delay(1100)
        onDismiss()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070D18),
                        Color(0xFF020408)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Procedural Animated Vector Logo Canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.42f

                    // 1. Outer Breathing Glow Halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f * coreGlowAlpha),
                                secondaryColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.35f * haloPulse
                        ),
                        radius = radius * 1.35f * haloPulse,
                        center = center
                    )

                    // 2. Synaptic Orbit Track
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.20f),
                        radius = radius * haloPulse,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // 3. Orbiting Synaptic Nodes (12 Orbital Satellites)
                    val nodeCount = 8
                    for (i in 0 until nodeCount) {
                        val angle = Math.toRadians((orbitAngle + (i * 360f / nodeCount)).toDouble())
                        val nx = center.x + (radius * haloPulse * cos(angle)).toFloat()
                        val ny = center.y + (radius * haloPulse * sin(angle)).toFloat()

                        // Filament Line to Center Core
                        drawLine(
                            color = if (i % 2 == 0) primaryColor.copy(alpha = 0.16f) else RetentionGold.copy(alpha = 0.14f),
                            start = center,
                            end = Offset(nx, ny),
                            strokeWidth = 1f
                        )

                        // Synaptic Bead
                        drawCircle(
                            color = if (i % 2 == 0) secondaryColor else RetentionGold,
                            radius = if (i % 2 == 0) 4.5f else 3.5f,
                            center = Offset(nx, ny)
                        )
                    }

                    // 4. Central Neural Harmonic Waves
                    val waveCount = 5
                    for (w in 0 until waveCount) {
                        val path = Path()
                        val waveWidth = size.width * 0.55f
                        val startX = center.x - (waveWidth / 2f)
                        val endX = center.x + (waveWidth / 2f)
                        val phaseOffset = wavePhase + (w * 0.7f)
                        val waveAmp = (14f + w * 4f) * (if (w % 2 == 0) 1f else -0.8f)

                        path.moveTo(startX, center.y)
                        var cx = startX
                        val step = 4f
                        while (cx <= endX) {
                            val norm = (cx - startX) / waveWidth
                            // Window function (hanning window) so endpoints taper to 0
                            val window = sin(norm * Math.PI).toFloat()
                            val cy = center.y + (sin((norm * 2 * Math.PI * 1.5) + phaseOffset).toFloat() * waveAmp * window)
                            path.lineTo(cx, cy)
                            cx += step
                        }

                        val strokeColor = when (w % 3) {
                            0 -> primaryColor
                            1 -> RetentionGold
                            else -> secondaryColor
                        }

                        drawPath(
                            path = path,
                            color = strokeColor.copy(alpha = 0.85f - (w * 0.12f)),
                            style = Stroke(
                                width = 3f - (w * 0.3f),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 5. Radiant Central Mind Nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color.White,
                                primaryColor,
                                primaryColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 22f
                        ),
                        radius = 18f * haloPulse,
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Luxury Minimal Brand Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlphaAnim.value)
                    .scale(scaleAnim.value)
            ) {
                Text(
                    text = "NeuroScribe",
                    fontFamily = PlusJakartaFontFamily,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "COGNITIVE VOICE & THOUGHT SYNTHESIS",
                    fontFamily = PlusJakartaFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.2.sp,
                    color = primaryColor.copy(alpha = 0.90f)
                )
            }
        }
    }
}
