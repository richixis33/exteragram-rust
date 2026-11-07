package org.exteragram.rust.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exteragram.rust.core.ExteraBridge
import org.exteragram.rust.ui.components.LiquidTopBar
import org.exteragram.rust.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccentChange: (Color) -> Unit
) {
    val state = ExteraBridge.state
    var hideRead by remember { mutableStateOf(state.ghost.hideReadReceipts) }
    var hideTyping by remember { mutableStateOf(state.ghost.hideTypingStatus) }
    var hideOnline by remember { mutableStateOf(state.ghost.hideOnlineStatus) }
    var ghostStream by remember { mutableStateOf(state.ghost.ghostStreamMode) }

    var blurRadius by remember { mutableFloatStateOf(state.glass.blurRadius) }
    var glassOpacity by remember { mutableFloatStateOf(state.glass.glassOpacity) }
    var noiseSheen by remember { mutableStateOf(state.glass.noiseSheen) }
    var borderGlow by remember { mutableFloatStateOf(state.glass.borderGlowIntensity) }

    var bubbleRadius by remember { mutableFloatStateOf(state.chat.bubbleCornerRadius) }
    var showIds by remember { mutableStateOf(state.chat.showIds) }
    var showExactEdit by remember { mutableStateOf(state.chat.showExactEditTime) }
    var antiDelete by remember { mutableStateOf(state.chat.antiDeleteEnabled) }
    var forwardWithoutQuote by remember { mutableStateOf(state.chat.forwardWithoutQuote) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LiquidTopBar(
                title = "Настройки exteraGram",
                subtitle = "Кастомизация, Liquid Glass & Rust Core",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBackClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category: Ghost Mode
            item {
                SettingsCategoryCard(title = "👻 Режим невидимки (Ghost Mode)") {
                    SettingsSwitchRow(
                        title = "Скрывать прочтение сообщений",
                        subtitle = "Блокирует пакеты messages.readHistory в ядре Rust",
                        checked = hideRead,
                        onCheckedChange = {
                            hideRead = it
                            state.ghost.hideReadReceipts = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Скрывать статус «Печатает...»",
                        subtitle = "Собеседник никогда не узнает, что вы набираете текст",
                        checked = hideTyping,
                        onCheckedChange = {
                            hideTyping = it
                            state.ghost.hideTypingStatus = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Скрывать статус «В сети»",
                        subtitle = "Не отправлять статус активности на сервера MTProto",
                        checked = hideOnline,
                        onCheckedChange = {
                            hideOnline = it
                            state.ghost.hideOnlineStatus = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Ghost Stream",
                        subtitle = "Фоновая синхронизация без регистрации времени входа",
                        checked = ghostStream,
                        onCheckedChange = {
                            ghostStream = it
                            state.ghost.ghostStreamMode = it
                        }
                    )
                }
            }

            // Category: Liquid Glass UI
            item {
                SettingsCategoryCard(title = "🫧 Жидкое стекло (Liquid Glass Engine)") {
                    Text(
                        text = "Размытие фона (Blur Radius): ${blurRadius.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = blurRadius,
                        onValueChange = {
                            blurRadius = it
                            state.glass.blurRadius = it
                        },
                        valueRange = 0f..40f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Прозрачность стекла: ${(glassOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = glassOpacity,
                        onValueChange = {
                            glassOpacity = it
                            state.glass.glassOpacity = it
                        },
                        valueRange = 0.2f..0.95f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SettingsSwitchRow(
                        title = "Эффект зеркального блика (Specular Sheen)",
                        subtitle = "Градиентная фаска на гранях карточек",
                        checked = noiseSheen,
                        onCheckedChange = {
                            noiseSheen = it
                            state.glass.noiseSheen = it
                        }
                    )

                    Text(
                        text = "Подсветка граней (Border Glow): ${(borderGlow * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = borderGlow,
                        onValueChange = {
                            borderGlow = it
                            state.glass.borderGlowIntensity = it
                        },
                        valueRange = 0f..1f
                    )
                }
            }

            // Category: Material You Monet Palette
            item {
                SettingsCategoryCard(title = "🎨 Акцент Material You & Monet") {
                    Text(
                        text = "Выберите оттенок динамической палитры:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ColorCircle(MonetOceanBlue, "Синий") { onAccentChange(MonetOceanBlue) }
                        ColorCircle(MonetEmerald, "Изумруд") { onAccentChange(MonetEmerald) }
                        ColorCircle(MonetAmethyst, "Аметист") { onAccentChange(MonetAmethyst) }
                        ColorCircle(MonetSunsetAmber, "Янтарь") { onAccentChange(MonetSunsetAmber) }
                        ColorCircle(MonetRose, "Розовый") { onAccentChange(MonetRose) }
                    }
                }
            }

            // Category: Chat Customization & Anti-Delete
            item {
                SettingsCategoryCard(title = "🛡️ Кастомизация сообщений & Anti-Delete") {
                    Text(
                        text = "Скругление пузырей сообщений: ${bubbleRadius.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = bubbleRadius,
                        onValueChange = {
                            bubbleRadius = it
                            state.chat.bubbleCornerRadius = it
                        },
                        valueRange = 4f..32f
                    )

                    SettingsSwitchRow(
                        title = "Anti-Delete (Сохранять удаленные)",
                        subtitle = "Удаленные собеседником сообщения остаются в чате с меткой 🗑️",
                        checked = antiDelete,
                        onCheckedChange = {
                            antiDelete = it
                            state.chat.antiDeleteEnabled = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Показывать ID пользователей и чатов",
                        subtitle = "Отображает Telegram ID в заголовках и сообщениях",
                        checked = showIds,
                        onCheckedChange = {
                            showIds = it
                            state.chat.showIds = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Точное время редактирования",
                        subtitle = "Показывает точную секунду изменения сообщения",
                        checked = showExactEdit,
                        onCheckedChange = {
                            showExactEdit = it
                            state.chat.showExactEditTime = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Пересылка без цитирования",
                        subtitle = "Удаляет имя оригинального автора при пересылке",
                        checked = forwardWithoutQuote,
                        onCheckedChange = {
                            forwardWithoutQuote = it
                            state.chat.forwardWithoutQuote = it
                        }
                    )
                }
            }

            // Category: About Core
            item {
                SettingsCategoryCard(title = "⚙️ Информация о движке") {
                    Text(
                        text = "exteraGram Rust v1.0.0-mobile",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Архитектура ядра: Rust libextera_core.so (aarch64 / armv7)\nИнтерфейс: Jetpack Compose + Liquid Glass Shaders\nСборка: GitHub Actions CI/CD Cloud Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ColorCircle(color: Color, name: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
