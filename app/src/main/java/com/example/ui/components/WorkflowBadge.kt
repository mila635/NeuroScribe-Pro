package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MindViolet
import com.example.ui.theme.NeuroCyan

@Composable
fun WorkflowBadge(
    workflowType: String,
    modifier: Modifier = Modifier
) {
    val (badgeColor, title, icon) = when (workflowType) {
        "COMBO_MASTERWORK" -> Triple(com.example.ui.theme.RetentionGold, "Audio + Mind Combo", Icons.Default.AutoAwesome)
        "COGNITIVE_SYNTHESIS" -> Triple(MindViolet, "Cognitive Mind", Icons.Default.AutoAwesome)
        else -> Triple(NeuroCyan, "Raw Audio Note", Icons.Default.Mic)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor
            )
        }
    }
}
