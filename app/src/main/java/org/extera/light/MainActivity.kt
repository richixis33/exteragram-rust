package org.extera.light

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
import org.extera.light.telegram.AuthState
import org.extera.light.telegram.TelegramEngine
import org.extera.light.ui.screens.*
import org.extera.light.ui.theme.ExteraLightTheme

sealed class Screen {
    object ChatList : Screen()
    data class Chat(val chatId: Long) : Screen()
    data class Profile(val chatId: Long) : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TelegramEngine.initialize(applicationContext)

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.ChatList) }
            var customAccent by remember { mutableStateOf<Color?>(null) }
            val authState by TelegramEngine.authState.collectAsState()

            ExteraLightTheme(customSeedColor = customAccent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (authState) {
                        is AuthState.LoggedIn -> {
                            AnimatedContent(
                                targetState = currentScreen,
                                label = "ScreenNavigation"
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
                                            onBackClick = { currentScreen = Screen.ChatList },
                                            onProfileClick = { currentScreen = Screen.Profile(target.chatId) }
                                        )
                                    }
                                    is Screen.Profile -> {
                                        ProfileScreen(
                                            chatId = target.chatId,
                                            onBackClick = { currentScreen = Screen.Chat(target.chatId) }
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
