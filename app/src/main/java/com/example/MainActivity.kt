package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.example.ui.components.FloatingActionSideDock
import com.example.ui.screens.AskNeuroScribeScreen
import com.example.ui.screens.CaptureScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KnowledgeVaultScreen
import com.example.ui.screens.MediaLibraryScreen
import com.example.ui.screens.MemoryRetentionScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        val prefs = getSharedPreferences("neuroscribe_prefs", Context.MODE_PRIVATE)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            var showSplash by rememberSaveable { mutableStateOf(true) }
            var hasAcceptedTerms by remember {
                mutableStateOf(prefs.getBoolean("has_accepted_terms_pro", false))
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (showSplash) {
                    SplashScreen(onDismiss = { showSplash = false })
                } else {
                    MainAppContent(viewModel = viewModel)

                    if (!hasAcceptedTerms) {
                        com.example.ui.components.CognitiveTermsDialog(
                            onAccepted = { hasAcceptedTerms = true }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        try {
            val serviceIntent = android.content.Intent(this, com.example.service.FloatingOverlayService::class.java)
            stopService(serviceIntent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isSideDockEnabled.value && android.provider.Settings.canDrawOverlays(this)) {
            try {
                val serviceIntent = android.content.Intent(this, com.example.service.FloatingOverlayService::class.java)
                startService(serviceIntent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        when (intent.getStringExtra("action_route")) {
            "capture_voice" -> viewModel.navigateTo(AppScreen.CAPTURE)
            "camera_scan" -> viewModel.triggerCameraLaunch()
            "archive_lookup" -> viewModel.navigateTo(AppScreen.ARCHIVE)
            "media_library" -> viewModel.navigateTo(AppScreen.MEDIA_LIBRARY)
            "retention_recall" -> viewModel.navigateTo(AppScreen.RETENTION)
            "language_select" -> viewModel.navigateTo(AppScreen.CAPTURE)
        }

        if (action == android.content.Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
            val text = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
            viewModel.handleIncomingShare(uri, text, type, this)
        } else if (action == android.content.Intent.ACTION_SEND_MULTIPLE) {
            val uris = intent.getParcelableArrayListExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
            uris?.firstOrNull()?.let { uri ->
                viewModel.handleIncomingShare(uri, null, type, this)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            // Show bottom navigation bar on primary screens (hidden on Detail & Ask AI for full-screen typing)
            if (currentScreen != AppScreen.DETAIL && currentScreen != AppScreen.ASK_AI) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CAPTURE,
                        onClick = { viewModel.navigateTo(AppScreen.CAPTURE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.CAPTURE) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                                contentDescription = "Capture"
                            )
                        },
                        label = { Text("Capture", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_capture")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.KNOWLEDGE,
                        onClick = { viewModel.navigateTo(AppScreen.KNOWLEDGE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.KNOWLEDGE) Icons.Filled.Hub else Icons.Outlined.Hub,
                                contentDescription = "Knowledge"
                            )
                        },
                        label = { Text("Vault", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_knowledge")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.ASK_AI,
                        onClick = { viewModel.navigateTo(AppScreen.ASK_AI) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.ASK_AI) Icons.Filled.QuestionAnswer else Icons.Outlined.QuestionAnswer,
                                contentDescription = "Ask AI"
                            )
                        },
                        label = { Text("Ask AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = secondaryColor,
                            selectedTextColor = secondaryColor,
                            indicatorColor = secondaryColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_ask_ai")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.LIBRARY || currentScreen == AppScreen.ARCHIVE || currentScreen == AppScreen.MEDIA_LIBRARY,
                        onClick = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.LIBRARY || currentScreen == AppScreen.ARCHIVE) Icons.Filled.Archive else Icons.Outlined.Archive,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_library")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val isSideDockEnabled by viewModel.isSideDockEnabled.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (currentScreen == AppScreen.DETAIL || currentScreen == AppScreen.ASK_AI) 0.dp else innerPadding.calculateBottomPadding()
                )
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)) +
                     slideIntoContainer(
                         towards = if (targetState.ordinal >= initialState.ordinal) androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Start else androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.End,
                         animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                     )).togetherWith(
                         androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                     )
                },
                modifier = Modifier.fillMaxSize(),
                label = "screen_lazy_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.CAPTURE -> CaptureScreen(viewModel = viewModel)
                    AppScreen.KNOWLEDGE -> KnowledgeVaultScreen(viewModel = viewModel)
                    AppScreen.ASK_AI -> AskNeuroScribeScreen(viewModel = viewModel)
                    AppScreen.LIBRARY, AppScreen.ARCHIVE -> NotesListScreen(viewModel = viewModel)
                    AppScreen.MEDIA_LIBRARY -> MediaLibraryScreen(viewModel = viewModel)
                    AppScreen.RETENTION -> MemoryRetentionScreen(viewModel = viewModel)
                    AppScreen.DETAIL -> {
                        selectedNote?.let { note ->
                            NoteDetailScreen(viewModel = viewModel, note = note)
                        } ?: run {
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Optional Floating Action Side Dock
            if (isSideDockEnabled && currentScreen != AppScreen.DETAIL) {
                FloatingActionSideDock(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
