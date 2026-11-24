package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * Enterprise Single-Color Audio-Level Meter.
 * Calm, precise vertical bar waveform using the primary accent color when recording
 * and neutral outline when on standby. No gradients, no neon glows.
 */
@Composable
fun NeuralSoundwaveVisualizer(
    amplitude: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val neutralColor = MaterialTheme.colorScheme.outline

    val infiniteTransition = rememberInfiniteTransition(label = "audio_level_meter")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meter_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Baseline reference rule
        drawLine(
            color = neutralColor.copy(alpha = 0.5f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        val barCount = 40
        val barSpacing = width / (barCount + 1)
        val barWidth = (barSpacing * 0.48f).coerceIn(2.5f, 6f)

        for (b in 0 until barCount) {
            val barX = barSpacing * (b + 1) - (barWidth / 2f)
            val normalizedPos = b.toFloat() / (barCount - 1)
            val envelope = sin(normalizedPos * Math.PI.toFloat())

            val barHeight = if (isRecording) {
                val harmonic = abs(sin(phase + b * 0.45f))
                val activeAmp = (amplitude * 1.4f).coerceIn(0.08f, 1f)
                (height * 0.82f * envelope * harmonic * activeAmp).coerceIn(4.dp.toPx(), height * 0.86f)
            } else {
                4.dp.toPx()
            }

            val barTop = centerY - (barHeight / 2f)

            drawRoundRect(
                color = if (isRecording) primaryColor else neutralColor,
                topLeft = Offset(barX, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

