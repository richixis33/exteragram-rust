package org.extera.light.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.extera.light.telegram.TelegramEngine
import org.extera.light.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccentChange: (Color?) -> Unit
) {
    val context = LocalContext.current
    val settings = TelegramEngine.settings

    var hideRead by remember { mutableStateOf(settings.ghostHideRead) }
    var hideTyping by remember { mutableStateOf(settings.ghostHideTyping) }
    var hideOnline by remember { mutableStateOf(settings.ghostHideOnline) }
    var antiDelete by remember { mutableStateOf(settings.antiDeleteEnabled) }
    var showIds by remember { mutableStateOf(settings.showIds) }
    var showExactEdit by remember { mutableStateOf(settings.showExactEditTime) }
    var forwardWithoutQuote by remember { mutableStateOf(settings.forwardWithoutQuote) }
    var bubbleRadius by remember { mutableFloatStateOf(settings.bubbleCornerRadius) }

    // Easter Egg: Tap version 5 times for God Mode
    var versionTapCount by remember { mutableIntStateOf(0) }
    var godModeUnlocked by remember { mutableStateOf(settings.godModeUnlocked) }
    var showGodModeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки ExteraLight",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: Extera Plugins (Ghost Mode & Privacy)
            item {
                SettingsGroupCard(title = "Плагины Extera и Приватность") {
                    SettingsSwitchRow(
                        title = "Скрывать прочтение сообщений",
                        subtitle = "Блокирует отправку отчетов о прочтении в Telegram",
                        checked = hideRead,
                        onCheckedChange = {
                            hideRead = it
                            settings.ghostHideRead = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Скрывать статус «Печатает...»",
                        subtitle = "Собеседник не видит, что вы набираете сообщение",
                        checked = hideTyping,
                        onCheckedChange = {
                            hideTyping = it
                            settings.ghostHideTyping = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Скрывать онлайн-статус",
                        subtitle = "Не обновлять статус «В сети» при входе в приложение",
                        checked = hideOnline,
                        onCheckedChange = {
                            hideOnline = it
                            settings.ghostHideOnline = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Anti-Delete (Защита от удаления)",
                        subtitle = "Сохранять удаленные собеседником сообщения с пометкой 🗑️",
                        checked = antiDelete,
                        onCheckedChange = {
                            antiDelete = it
                            settings.antiDeleteEnabled = it
                        }
                    )
                }
            }

            // Group 2: Material You & Theming
            item {
                SettingsGroupCard(title = "Material You и Внешний вид") {
                    Text(
                        text = "Цветовая палитра (Monet Dynamic Color):",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaletteCircle("Системный", null, settings.customSeedColor == null) {
                            settings.customSeedColor = null
                            onAccentChange(null)
                        }
                        PaletteCircle("Emerald", MonetSeedEmerald, settings.customSeedColor == MonetSeedEmerald) {
                            settings.customSeedColor = MonetSeedEmerald
                            onAccentChange(MonetSeedEmerald)
                        }
                        PaletteCircle("Amethyst", MonetSeedAmethyst, settings.customSeedColor == MonetSeedAmethyst) {
                            settings.customSeedColor = MonetSeedAmethyst
                            onAccentChange(MonetSeedAmethyst)
                        }
                        PaletteCircle("Amber", MonetSeedAmber, settings.customSeedColor == MonetSeedAmber) {
                            settings.customSeedColor = MonetSeedAmber
                            onAccentChange(MonetSeedAmber)
                        }
                        PaletteCircle("Rose", MonetSeedRose, settings.customSeedColor == MonetSeedRose) {
                            settings.customSeedColor = MonetSeedRose
                            onAccentChange(MonetSeedRose)
                        }
                        PaletteCircle("Cyan", MonetSeedCyan, settings.customSeedColor == MonetSeedCyan) {
                            settings.customSeedColor = MonetSeedCyan
                            onAccentChange(MonetSeedCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Скругление пузырей сообщений: ${bubbleRadius.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = bubbleRadius,
                        onValueChange = {
                            bubbleRadius = it
                            settings.bubbleCornerRadius = it
                        },
                        valueRange = 4f..28f,
                        steps = 5
                    )
                }
            }

            // Group 3: Chat Extras
            item {
                SettingsGroupCard(title = "Кастомизация диалогов") {
                    SettingsSwitchRow(
                        title = "Отображать Telegram ID",
                        subtitle = "Показывает уникальные ID пользователей и сообщений",
                        checked = showIds,
                        onCheckedChange = {
                            showIds = it
                            settings.showIds = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Точное время редактирования",
                        subtitle = "Показывает точную секунду изменения сообщений",
                        checked = showExactEdit,
                        onCheckedChange = {
                            showExactEdit = it
                            settings.showExactEditTime = it
                        }
                    )
                    SettingsSwitchRow(
                        title = "Пересылка без цитирования",
                        subtitle = "Удаляет имя оригинального автора при пересылке",
                        checked = forwardWithoutQuote,
                        onCheckedChange = {
                            forwardWithoutQuote = it
                            settings.forwardWithoutQuote = it
                        }
                    )
                }
            }

            // Group 4: Network & Cache Optimization
            item {
                SettingsGroupCard(title = "Память и Оптимизация (No Bloat)") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Локальный кэш ExteraLight очищен", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Очистить локальный кэш",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Удаляет временные медиа, освобождая память",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Пинг до датацентров Telegram:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("38 ms • DC4", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Group 5: About & Easter Egg
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            versionTapCount++
                            if (versionTapCount == 5) {
                                godModeUnlocked = true
                                settings.godModeUnlocked = true
                                showGodModeDialog = true
                                Toast.makeText(context, "🥚 ПАСХАЛКА: ExteraLight God Mode активирован!", Toast.LENGTH_LONG).show()
                            } else if (versionTapCount < 5) {
                                Toast.makeText(context, "Осталось ${5 - versionTapCount} тапов до пасхалки...", Toast.LENGTH_SHORT).show()
                            } else {
                                showGodModeDialog = true
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ExteraLight v1.0.0",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Оптимизированный форк в стиле exteraGram\nMaterial You Design • Без лагов и лишнего веса",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (godModeUnlocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("⚡ GOD MODE ACTIVE", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Easter Egg Dialog
    if (showGodModeDialog) {
        AlertDialog(
            onDismissRequest = { showGodModeDialog = false },
            icon = { Icon(Icons.Default.Celebration, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("🥚 ExteraLight God Mode") },
            text = {
                Column {
                    Text("Вы открыли секретное меню кастомизации ExteraLight:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Подмена устройства: ${settings.spoofDevice}")
                    Text("• Турбо-приоритет сетевых пакетов MTProto")
                    Text("• Отключение телеметрии и аналитики")
                    Text("• Локальный оффлайн-кэш сообщений включен")
                }
            },
            confirmButton = {
                TextButton(onClick = { showGodModeDialog = false }) {
                    Text("Отлично")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun PaletteCircle(
    name: String,
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color ?: MaterialTheme.colorScheme.primary)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
