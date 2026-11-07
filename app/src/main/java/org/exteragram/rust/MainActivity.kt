package org.exteragram.rust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.exteragram.rust.telegram.AuthState
import org.exteragram.rust.telegram.TelegramManager
import org.exteragram.rust.ui.screens.AuthScreen
import org.exteragram.rust.ui.screens.ChatListScreen
import org.exteragram.rust.ui.screens.ChatScreen
import org.exteragram.rust.ui.screens.SettingsScreen
import org.exteragram.rust.ui.theme.ExteraGramTheme

sealed class Screen {
    object ChatList : Screen()
    data class Chat(val chatId: Long) : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start TDLib & Rust Core Service
        TelegramManager.initialize(applicationContext)

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.ChatList) }
            var customAccent by remember { mutableStateOf<Color?>(null) }
            val authState by TelegramManager.authState.collectAsState()

            ExteraGramTheme(customAccentColor = customAccent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (authState) {
                        is AuthState.LoggedIn -> {
                            AnimatedContent(
                                targetState = currentScreen,
                                label = "MainNavigation"
                            ) { target ->
                                when (target) {
                                    is Screen.ChatList -> {
                                        ChatListScreen(
                                            onChatClick = { id -> currentScreen = Screen.Chat(id) },
                                            onSettingsClick = { currentScreen = Screen.Settings }
                                        )
                                    }
                                    is Screen.Chat -> {
                                        ChatScreen(
                                            chatId = target.chatId,
                                            onBackClick = { currentScreen = Screen.ChatList }
                                        )
                                    }
                                    is Screen.Settings -> {
                                        SettingsScreen(
                                            onBackClick = { currentScreen = Screen.ChatList },
                                            onAccentChange = { color -> customAccent = color }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            AuthScreen(state = authState)
                        }
                    }
                }
            }
        }
    }
}
