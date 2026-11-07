package org.exteragram.rust.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exteragram.rust.core.ExteraBridge
import org.exteragram.rust.ui.components.LiquidTopBar
import org.exteragram.rust.ui.theme.liquidGlass

data class DialogItem(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarColor: Color = Color(0xFF007AFF)
)

val sampleDialogs = listOf(
    DialogItem(1001, "Павел Дуров", "Новый билд клиента на Rust летает!", "01:24", unreadCount = 1, isOnline = true, avatarColor = Color(0xFF007AFF)),
    DialogItem(1002, "exteraGram Community", "Тестируем Liquid Glass и Ghost-режим на смартфонах", "00:58", unreadCount = 14, avatarColor = Color(0xFF9C27B0)),
    DialogItem(1003, "Rust Dev Group", "cargo-ndk успешно собрал aarch64 .so", "Вчера", avatarColor = Color(0xFF00C853)),
    DialogItem(1004, "Избранное (Cloud)", "exteragram-rust-v1.0.apk", "Вчера", avatarColor = Color(0xFFFF6D00)),
    DialogItem(1005, "Мария С.", "Встретимся завтра в кафе?", "Пт", unreadCount = 0, isOnline = true, avatarColor = Color(0xFFE91E63))
)

@Composable
fun ChatListScreen(
    onChatClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val ghost = ExteraBridge.state.ghost
    val isGhostActive = ghost.hideReadReceipts || ghost.hideTypingStatus || ghost.hideOnlineStatus

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Message")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Liquid Glass Top Bar
            LiquidTopBar(
                title = "exteraGram",
                subtitle = if (isGhostActive) "👻 Ghost Mode Active" else "Rust Engine Online",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // Ghost Mode Banner (if active)
            if (isGhostActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(14.dp),
                            blurRadius = 16.dp,
                            tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            borderSheenColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Статусы прочтения и печати скрыты ядром Rust",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Chat List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                items(sampleDialogs) { dialog ->
                    ChatItemRow(dialog = dialog, onClick = { onChatClick(dialog.id) })
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(dialog: DialogItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(dialog.avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dialog.title.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (dialog.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles and message preview
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dialog.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dialog.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dialog.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (dialog.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = dialog.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
