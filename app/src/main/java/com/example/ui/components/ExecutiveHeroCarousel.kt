package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MindViolet
import com.example.ui.theme.NeuroCyan
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.AppScreen
import kotlinx.coroutines.delay

data class HeroSlide(
    val title: String,
    val tag: String,
    val summary: String,
    val actionText: String,
    val targetScreen: AppScreen,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val accentColor: Color
)

@Composable
fun ExecutiveHeroCarousel(
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            HeroSlide(
                title = "Continuous Voice Scribe",
                tag = "44.1kHz • Urdu / English / Multilingual",
                summary = "Uninterrupted speech capture with zero silence dropouts, automatic language detection & optical document OCR.",
                actionText = "Start Recording",
                targetScreen = AppScreen.CAPTURE,
                icon = Icons.Default.Mic,
                gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0284C7)),
                accentColor = NeuroCyan
            ),
            HeroSlide(
                title = "Neural Knowledge Vault",
                tag = "Autonomous Synthesis & Deep Grounding",
                summary = "Transforms spoken thoughts into structured executive briefs, key takeaways, and interconnected concept graphs.",
                actionText = "Explore Vault",
                targetScreen = AppScreen.KNOWLEDGE,
                icon = Icons.Default.Hub,
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488)),
                accentColor = Color(0xFF10B981)
            ),
            HeroSlide(
                title = "Dual-Engine Ask AI",
                tag = "Vault Grounding & Global Science",
                summary = "Query personal notes with precise source citations or tap frontier reasoning for structured executive answers.",
                actionText = "Launch Copilot",
                targetScreen = AppScreen.ASK_AI,
                icon = Icons.Default.QuestionAnswer,
                gradientColors = listOf(Color(0xFF3B0764), Color(0xFF581C87), Color(0xFF7E22CE)),
                accentColor = MindViolet
            ),
            HeroSlide(
                title = "Retention Lab (SM-2)",
                tag = "SuperMemo-2 Spaced Recall",
                summary = "Active recall flashcards and automated AI assessments generated directly from your audio recordings.",
                actionText = "Practice Recall",
                targetScreen = AppScreen.RETENTION,
                icon = Icons.Default.School,
                gradientColors = listOf(Color(0xFF78350F), Color(0xFF92400E), Color(0xFFB45309)),
                accentColor = RetentionGold
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    // Subtle auto-advance every 5 seconds
    LaunchedEffect(currentIndex) {
        delay(5000)
        currentIndex = (currentIndex + 1) % slides.size
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val slide = slides[currentIndex]

        // 3D Volumetric Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            slide.accentColor.copy(alpha = 0.8f),
                            slide.accentColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onNavigate(slide.targetScreen) }
                .testTag("hero_carousel_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = slide.gradientColors,
                            radius = 600f
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    // Tag & Icon Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, slide.accentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = slide.tag.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = slide.accentColor,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = slide.summary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color.White.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = slide.accentColor,
                            modifier = Modifier.clickable { onNavigate(slide.targetScreen) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = slide.actionText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (slide.accentColor == RetentionGold) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (slide.accentColor == RetentionGold) Color.Black else Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Dot Page Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            slides.indices.forEach { index ->
                                val isSelected = index == currentIndex
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(if (isSelected) 18.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) slide.accentColor else Color.White.copy(alpha = 0.35f))
                                        .clickable { currentIndex = index }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
