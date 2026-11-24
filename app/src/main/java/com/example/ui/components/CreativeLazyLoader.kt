package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RetentionGold
import kotlin.math.sin

/**
 * Creative, High-Aesthetic Lazy Loader & Shimmer Wave Transition.
 * Displays animated sinusoidal neural wave paths, luminous particle nodes,
 * and sleek skeletal shimmers during page switching and cognitive processing.
 */
@Composable
fun CreativeLazyLoader(
    modifier: Modifier = Modifier,
    message: String = "Synthesizing Cognitive Lattice…",
    subMessage: String = "Streaming Neural Buffers • Zero Precision Loss"
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "creative_lazy_loader")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("creative_lazy_loader"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic Neural Wave & Orbit Canvas
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.5.dp, primaryColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(68.dp)) {
                val midY = size.height / 2f
                val width = size.width
                val amplitude = 14f * pulseScale

                // Sine Wave 1 (Primary Neural Stream)
                val path1 = Path()
                path1.moveTo(0f, midY)
                val step = 4f
                var x = 0f
                while (x <= width) {
                    val angle = (x / width) * 2 * Math.PI + wavePhase
                    val y = midY + (sin(angle) * amplitude).toFloat()
                    path1.lineTo(x, y)
                    x += step
                }
                drawPath(
                    path = path1,
                    brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, RetentionGold)),
                    style = Stroke(width = 3f)
                )

                // Sine Wave 2 (Harmonic Resonance Stream)
                val path2 = Path()
                path2.moveTo(0f, midY)
                x = 0f
                while (x <= width) {
                    val angle = (x / width) * 2 * Math.PI - wavePhase * 1.3f
                    val y = midY + (sin(angle) * (amplitude * 0.7f)).toFloat()
                    path2.lineTo(x, y)
                    x += step
                }
                drawPath(
                    path = path2,
                    color = primaryColor.copy(alpha = 0.45f),
                    style = Stroke(width = 1.8f)
                )

                // Central Synaptic Core Node
                drawCircle(
                    color = secondaryColor,
                    radius = 4f * pulseScale,
                    center = Offset(size.width / 2f, midY)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Header
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-line Shimmer Skeleton Bars
        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                primaryColor.copy(alpha = 0.22f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            start = Offset(shimmerX, 0f),
            end = Offset(shimmerX + 220f, 0f)
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(shimmerBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
