package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Enterprise neutral-first flat surface container.
 * Eliminates radial mesh gradients, glowing orbs, and decorative background noise
 * in accordance with the Linear / Notion / Bloomberg Terminal minimalism design system.
 */
@Composable
fun SpreadedAnimatedBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

