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

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.ChatList) }
            var customAccent by remember { mutableStateOf<Color?>(null) }

            ExteraGramTheme(customAccentColor = customAccent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "ScreenTransition"
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
            }
        }
    }
}
