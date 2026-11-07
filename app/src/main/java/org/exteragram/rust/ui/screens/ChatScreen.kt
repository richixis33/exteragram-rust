package org.exteragram.rust.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exteragram.rust.core.ExteraBridge
import org.exteragram.rust.telegram.ExteraChatMessage
import org.exteragram.rust.telegram.TelegramManager
import org.exteragram.rust.ui.components.LiquidTopBar
import org.exteragram.rust.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatId: Long,
    onBackClick: () -> Unit
) {
    val chatConfig = ExteraBridge.state.chat
    val glassConfig = ExteraBridge.state.glass
    var inputText by remember { mutableStateOf("") }

    val allMessages by TelegramManager.chatMessages.collectAsState()
    val messages = allMessages[chatId] ?: emptyList()

    LaunchedEffect(chatId) {
        TelegramManager.loadChatHistory(chatId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LiquidTopBar(
                title = "Диалог #$chatId",
                subtitle = if (chatConfig.showIds) "ID: $chatId" else "Telegram Чат",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBackClick,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Real Messages List
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Загрузка сообщений из Telegram...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        RealMessageBubble(
                            msg = msg,
                            cornerRadius = chatConfig.bubbleCornerRadius,
                            showIds = chatConfig.showIds,
                            showExactEdit = chatConfig.showExactEditTime
                        )
                    }
                }
            }

            // Liquid Glass Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(26.dp),
                        blurRadius = glassConfig.blurRadius.dp,
                        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = glassConfig.glassOpacity),
                        borderSheenColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.primary)
                    }

                    TextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            if (it.isNotEmpty()) {
                                TelegramManager.sendTyping(chatId)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Сообщение...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                TelegramManager.sendMessage(chatId, inputText.trim())
                                inputText = ""
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealMessageBubble(
    msg: ExteraChatMessage,
    cornerRadius: Float,
    showIds: Boolean,
    showExactEdit: Boolean
) {
    val bubbleShape = RoundedCornerShape(
        topStart = cornerRadius.dp,
        topEnd = cornerRadius.dp,
        bottomStart = if (msg.isOutgoing) cornerRadius.dp else 4.dp,
        bottomEnd = if (msg.isOutgoing) 4.dp else cornerRadius.dp
    )

    val alignment = if (msg.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (msg.isOutgoing) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    val textColor = if (msg.isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface

    val timeFormatted = remember(msg.date) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(msg.date * 1000L))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Anti-Delete Extera Badge
            if (msg.isDeletedByRemote) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "🗑️ Удалено собеседником (Anti-Delete)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Message Text
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer: ID, Edited badge, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIds) {
                    Text(
                        text = "#${msg.id} • ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.6f)
                    )
                }

                if (showExactEdit && msg.editDate > 0) {
                    val editSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val editFormatted = editSdf.format(Date(msg.editDate * 1000L))
                    Text(
                        text = "изм. $editFormatted • ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
