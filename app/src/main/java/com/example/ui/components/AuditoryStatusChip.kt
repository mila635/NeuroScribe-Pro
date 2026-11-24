package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AuditoryFilterState
import com.example.ui.theme.NoiseYellow
import com.example.ui.theme.VoiceGreen

@Composable
fun AuditoryStatusChip(
    filterState: AuditoryFilterState,
    detectedLang: String,
    modifier: Modifier = Modifier
) {
    val (dotColor, statusLabel) = when (filterState) {
        AuditoryFilterState.SPEECH_NLP_DETECTED -> Pair(
            VoiceGreen,
            "VOICE DETECTED"
        )
        AuditoryFilterState.FILTERING_NOISE -> Pair(
            NoiseYellow,
            "FILTERING NOISE"
        )
        AuditoryFilterState.IDLE -> Pair(
            Color(0xFF94A3B8),
            "SENSOR READY"
        )
    }

    val animatedDotColor by animateColorAsState(targetValue = dotColor, label = "dot_color")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(1.dp, animatedDotColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(animatedDotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
