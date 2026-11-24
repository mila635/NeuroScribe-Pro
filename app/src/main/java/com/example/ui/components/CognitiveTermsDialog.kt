package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MindViolet
import com.example.ui.theme.NeuroCyan
import com.example.ui.theme.RetentionGold

/**
 * First-Launch Terms & Conditions / Cognitive Privacy Shield Dialog.
 * Enforces security transparency, zero dummy data assurance, on-device SQLite storage,
 * and audio processing ethics with an explicit acceptance gate.
 */
@Composable
fun CognitiveTermsDialog(
    onAccepted: () -> Unit
) {
    val context = LocalContext.current
    var isAgreed by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = { /* Modal must be explicitly accepted to proceed */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(primaryColor, MindViolet, RetentionGold)),
                    shape = RoundedCornerShape(22.dp)
                )
                .testTag("terms_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Shield Emblem Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            .border(1.5.dp, primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Shield",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "NEUROSCRIBE PRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = primaryColor
                        )
                        Text(
                            text = "Cognitive Privacy Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Welcome to NeuroScribe Pro. Before deploying neural voice capture and cognitive synthesis, please review and accept our architecture and data governance protocols:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pillar 1: On-Device Sovereign Vault
                SecurityPillarRow(
                    icon = Icons.Default.Lock,
                    iconTint = NeuroCyan,
                    title = "100% On-Device Local SQLite Vault",
                    description = "Your audio recordings, transcripts, flashcards, and concept maps are saved directly in your device's private sandbox storage. No arbitrary external cloud databases."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pillar 2: Continuous Multilingual Processing
                SecurityPillarRow(
                    icon = Icons.Default.VerifiedUser,
                    iconTint = MindViolet,
                    title = "Continuous Voice & Telephony Safety",
                    description = "Audio recording utilizes hardware MediaRecorder with automatic phone call conflict protection and foreground status telemetry in the notification chamber."
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pillar 3: Zero Third-Party Advertising
                SecurityPillarRow(
                    icon = Icons.Default.Security,
                    iconTint = RetentionGold,
                    title = "Zero Advertising & Zero Dummy Data",
                    description = "NeuroScribe Pro contains zero third-party ad networks, zero data brokers, and zero persistent dummy samples. Every metric reflects your actual captured intelligence."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Agreement Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreed,
                        onCheckedChange = { isAgreed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.testTag("terms_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I accept the NeuroScribe Pro architecture terms and cognitive data governance policy.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unlock App Action Button
                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("neuroscribe_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("has_accepted_terms_pro", true).apply()
                        onAccepted()
                    },
                    enabled = isAgreed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("unlock_neuroscribe_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.35f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock NeuroScribe Pro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityPillarRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
