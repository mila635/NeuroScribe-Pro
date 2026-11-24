package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MindViolet
import com.example.ui.theme.NeuroCyan

enum class PipelineStage(
    val stepNumber: Int,
    val shortLabel: String,
    val progress: Float
) {
    IDLE(0, "READY", 0f),
    STAGE_1_CAPTURE(1, "INGESTION", 0.20f),
    STAGE_2_ENCODE(2, "ENCODING", 0.40f),
    STAGE_3_TRANSCRIBE(3, "TRANSCRIBING", 0.65f),
    STAGE_4_SYNTHESIS(4, "SYNTHESIS", 0.85f),
    STAGE_5_ARCHIVED(5, "ARCHIVED", 1.0f);

    companion object {
        // Compatibility alias for earlier references
        val STAGE_1_INGESTION = STAGE_1_CAPTURE
        val STAGE_2_ENCODING = STAGE_2_ENCODE
        val STAGE_3_SYNTHESIS = STAGE_4_SYNTHESIS
        val STAGE_4_ARCHIVED = STAGE_5_ARCHIVED
    }
}

/**
 * Ultra-sleek, minimal-text, industrial HUD for post-recording processing.
 * Displays 5 clear steps with icon nodes and progress bar.
 */
@Composable
fun ProcessingPipelineHud(
    currentStage: PipelineStage,
    statusDetail: String,
    fileName: String? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = currentStage.progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "pipeline_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_node")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = Color(0xFF10B981)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.5.dp,
                if (currentStage == PipelineStage.STAGE_5_ARCHIVED) successColor.copy(alpha = 0.7f) else primaryColor.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            // Stage Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentStage == PipelineStage.STAGE_5_ARCHIVED) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(successColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = successColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "STEP ${currentStage.stepNumber} OF 5: ${currentStage.shortLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (currentStage == PipelineStage.STAGE_5_ARCHIVED) successColor else primaryColor
                    )
                }

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (currentStage == PipelineStage.STAGE_5_ARCHIVED) successColor else primaryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Icon Stage Pipeline Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PipelineNode(
                    icon = Icons.Default.Mic,
                    isActive = currentStage.stepNumber >= 1,
                    isCurrent = currentStage == PipelineStage.STAGE_1_CAPTURE,
                    pulseScale = pulseScale,
                    activeColor = primaryColor
                )

                NodeConnector(isPassed = currentStage.stepNumber >= 2, color = primaryColor)

                PipelineNode(
                    icon = Icons.Default.Save,
                    isActive = currentStage.stepNumber >= 2,
                    isCurrent = currentStage == PipelineStage.STAGE_2_ENCODE,
                    pulseScale = pulseScale,
                    activeColor = primaryColor
                )

                NodeConnector(isPassed = currentStage.stepNumber >= 3, color = primaryColor)

                PipelineNode(
                    icon = Icons.Default.Translate,
                    isActive = currentStage.stepNumber >= 3,
                    isCurrent = currentStage == PipelineStage.STAGE_3_TRANSCRIBE,
                    pulseScale = pulseScale,
                    activeColor = primaryColor
                )

                NodeConnector(isPassed = currentStage.stepNumber >= 4, color = primaryColor)

                PipelineNode(
                    icon = Icons.Default.AutoAwesome,
                    isActive = currentStage.stepNumber >= 4,
                    isCurrent = currentStage == PipelineStage.STAGE_4_SYNTHESIS,
                    pulseScale = pulseScale,
                    activeColor = primaryColor
                )

                NodeConnector(isPassed = currentStage.stepNumber >= 5, color = successColor)

                PipelineNode(
                    icon = Icons.Default.Storage,
                    isActive = currentStage.stepNumber >= 5,
                    isCurrent = currentStage == PipelineStage.STAGE_5_ARCHIVED,
                    pulseScale = pulseScale,
                    activeColor = successColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Concise Clean Status Detail
            if (statusDetail.isNotBlank()) {
                Text(
                    text = statusDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PipelineNode(
    icon: ImageVector,
    isActive: Boolean,
    isCurrent: Boolean,
    pulseScale: Float,
    activeColor: Color
) {
    val scale = if (isCurrent) pulseScale else 1f
    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                1.5.dp,
                if (isActive) activeColor else Color.Transparent,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun NodeConnector(isPassed: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .width(16.dp)
            .height(2.dp)
            .background(
                if (isPassed) color.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.25f)
            )
    )
}
