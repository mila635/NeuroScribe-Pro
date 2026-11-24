package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Enterprise Design System — Neutral-First + Single Accent Palette
// Primary Accent: Deep Teal/Blue (#0E7490 Light / #22A6C4 Dark)
val EnterpriseTealLight = Color(0xFF0E7490)
val EnterpriseTealDark = Color(0xFF22A6C4)
val EnterpriseTealSubtleLight = Color(0xFFE0F2FE)
val EnterpriseTealSubtleDark = Color(0xFF132D38)

// Light Mode Neutrals
val LightBackground = Color(0xFFF7F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightElevated = Color(0xFFF2F4F7)
val LightBorder = Color(0xFFE4E7EC)
val LightTextPrimary = Color(0xFF101828)
val LightTextSecondary = Color(0xFF667085)

// Dark Mode Neutrals
val DarkBackground = Color(0xFF0B0D12)
val DarkSurface = Color(0xFF12151C)
val DarkElevated = Color(0xFF181C25)
val DarkBorder = Color(0xFF262B36)
val DarkTextPrimary = Color(0xFFF2F4F7)
val DarkTextSecondary = Color(0xFF98A2B3)

// Semantic Status Colors (Strictly for status indicators, never decorative)
val StatusSuccess = Color(0xFF12B76A)
val StatusWarning = Color(0xFFF79009)
val StatusError = Color(0xFFF04438)

// Legacy aliases mapped to the strict Enterprise Design System tokens so all modules share the single-accent restraint
val NeuroCyan = EnterpriseTealDark
val NeuroCyanDark = EnterpriseTealLight
val NeuroCyanLight = EnterpriseTealSubtleLight

val MindViolet = EnterpriseTealDark
val MindVioletDark = EnterpriseTealLight
val MindVioletLight = EnterpriseTealSubtleLight

val RetentionGold = StatusWarning
val RetentionGoldLight = Color(0xFFFEF0C7)

val VoiceGreen = StatusSuccess
val NoiseYellow = StatusWarning
val AlertRed = StatusError

val SlateNavyDark = DarkBackground
val SlateNavySurface = DarkSurface
val SlateNavyCard = DarkElevated
val SlateNavyBorder = DarkBorder
val SlateNavyMuted = DarkTextSecondary

val CleanWhite = LightSurface
val CleanSurfaceLight = LightBackground
val CleanCardLight = LightSurface
val CleanBorderLight = LightBorder
val CleanTextDark = LightTextPrimary
val CleanTextMuted = LightTextSecondary

