package org.exteragram.rust.telegram

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import org.exteragram.rust.core.ExteraBridge
import java.io.File

sealed class AuthState {
    object Initial : AuthState()
    object WaitPhoneNumber : AuthState()
    data class WaitCode(val phoneNumber: String) : AuthState()
    data class WaitPassword(val hint: String?) : AuthState()
    data class LoggedIn(val user: TdApi.User) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class ExteraChatMessage(
    val id: Long,
    val senderId: Long,
    val senderName: String,
    val text: String,
    val date: Int,
    val isOutgoing: Boolean,
    val isDeletedByRemote: Boolean = false,
    val editDate: Int = 0
)

data class ExteraChat(
    val id: Long,
    val title: String,
    val lastMessageText: String,
    val date: Int,
    val unreadCount: Int,
    val isOnline: Boolean = false
)

object TelegramManager : Client.ResultHandler {
    private const val TAG = "TelegramManager"
    private const val API_ID = 94575
    private const val API_HASH = "a3406de8d171bb422bb6ddf3bbd800e2"

    private var client: Client? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _chats = MutableStateFlow<List<ExteraChat>>(emptyList())
    val chats: StateFlow<List<ExteraChat>> = _chats.asStateFlow()

    private val _chatMessages = MutableStateFlow<Map<Long, List<ExteraChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<Long, List<ExteraChatMessage>>> = _chatMessages.asStateFlow()

    private val userCache = mutableMapOf<Long, TdApi.User>()
    private val rawChatMap = mutableMapOf<Long, TdApi.Chat>()

    private var currentPhoneNumber: String = ""

    fun initialize(context: Context) {
        if (client != null) return

        val tdlibDir = File(context.filesDir, "tdlib").apply { mkdirs() }
        val filesDir = File(context.filesDir, "tdlib_files").apply { mkdirs() }

        client = Client.create({ update ->
            handleUpdate(update, tdlibDir, filesDir)
        }, null, null)

        Log.i(TAG, "TDLib Client instantiated successfully.")
    }

    private fun handleUpdate(update: TdApi.Object, tdlibDir: File, filesDir: File) {
        when (update) {
            is TdApi.UpdateAuthorizationState -> {
                handleAuthState(update.authorizationState, tdlibDir, filesDir)
            }
            is TdApi.UpdateNewChat -> {
                val chat = update.chat
                rawChatMap[chat.id] = chat
                updateChatList()
            }
            is TdApi.UpdateChatLastMessage -> {
                val chat = rawChatMap[update.chatId]
                if (chat != null) {
                    chat.lastMessage = update.lastMessage
                    updateChatList()
                }
            }
            is TdApi.UpdateNewMessage -> {
                val msg = update.message
                val text = extractMessageText(msg.content)
                val isOutgoing = msg.isOutgoing
                val senderId = when (val sender = msg.senderId) {
                    is TdApi.MessageSenderUser -> sender.userId
                    else -> 0L
                }

                // Pass to Rust Core for Anti-Delete and Ghost indexing
                ExteraBridge.recordMessage(
                    chatId = msg.chatId,
                    msgId = msg.id,
                    senderId = senderId,
                    senderName = "User $senderId",
                    text = text,
                    isOutgoing = isOutgoing
                )

                addMessageToChat(msg.chatId, ExteraChatMessage(
                    id = msg.id,
                    senderId = senderId,
                    senderName = "User $senderId",
                    text = text,
                    date = msg.date,
                    isOutgoing = isOutgoing,
                    editDate = msg.editDate
                ))
            }
            is TdApi.UpdateDeleteMessages -> {
                if (update.isPermanent) {
                    // Anti-Delete Interception via Rust: Keep messages locally!
                    val chatId = update.chatId
                    update.messageIds.forEach { msgId ->
                        ExteraBridge.markMessageDeleted(chatId, msgId)
                    }
                    markMessagesDeletedInState(chatId, update.messageIds.toList())
                }
            }
            is TdApi.UpdateUser -> {
                userCache[update.user.id] = update.user
            }
        }
    }

    private fun handleAuthState(state: TdApi.AuthorizationState, tdlibDir: File, filesDir: File) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val params = TdApi.SetTdlibParameters().apply {
                    databaseDirectory = tdlibDir.absolutePath
                    useMessageDatabase = true
                    useSecretChats = true
                    apiId = API_ID
                    apiHash = API_HASH
                    systemLanguageCode = "ru"
                    deviceModel = "exteraGram Rust Phone"
                    systemVersion = "Android ${Build.VERSION.RELEASE}"
                    applicationVersion = "1.0.0-extera"
                    enableStorageOptimizer = true
                }
                client?.send(params, this)
            }
            is TdApi.AuthorizationStateWaitEncryptionKey -> {
                client?.send(TdApi.CheckDatabaseEncryptionKey(), this)
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authState.value = AuthState.WaitPhoneNumber
            }
            is TdApi.AuthorizationStateWaitCode -> {
                _authState.value = AuthState.WaitCode(currentPhoneNumber)
            }
            is TdApi.AuthorizationStateWaitPassword -> {
                _authState.value = AuthState.WaitPassword(state.passwordHint)
            }
            is TdApi.AuthorizationStateReady -> {
                client?.send(TdApi.GetMe()) { meObj ->
                    if (meObj is TdApi.User) {
                        _authState.value = AuthState.LoggedIn(meObj)
                    }
                }
                // Load actual chats from Telegram cloud
                client?.send(TdApi.LoadChats(null, 50), this)
            }
            is TdApi.AuthorizationStateLoggingOut, is TdApi.AuthorizationStateClosed -> {
                _authState.value = AuthState.WaitPhoneNumber
            }
        }
    }

    fun submitPhoneNumber(phone: String) {
        currentPhoneNumber = phone.trim()
        val settings = TdApi.PhoneNumberAuthenticationSettings()
        client?.send(TdApi.SetAuthenticationPhoneNumber(currentPhoneNumber, settings), this)
    }

    fun submitCode(code: String) {
        client?.send(TdApi.CheckAuthenticationCode(code.trim()), this)
    }

    fun submitPassword(password: String) {
        client?.send(TdApi.CheckAuthenticationPassword(password), this)
    }

    fun loadChatHistory(chatId: Long) {
        client?.send(TdApi.GetChatHistory(chatId, 0, 0, 50, false)) { res ->
            if (res is TdApi.Messages) {
                val list = res.messages.mapNotNull { m ->
                    m?.let {
                        val senderId = when (val sender = it.senderId) {
                            is TdApi.MessageSenderUser -> sender.userId
                            else -> 0L
                        }
                        ExteraChatMessage(
                            id = it.id,
                            senderId = senderId,
                            senderName = userCache[senderId]?.firstName ?: "Пользователь",
                            text = extractMessageText(it.content),
                            date = it.date,
                            isOutgoing = it.isOutgoing,
                            editDate = it.editDate
                        )
                    }
                }
                _chatMessages.value = _chatMessages.value + (chatId to list.reversed())
            }
        }

        // Ghost Mode Interception on Opening Chat:
        // If hide_read_receipts is enabled, do NOT send TdApi.OpenChat or TdApi.ViewMessages!
        if (!ExteraBridge.shouldBlockReadReceipt(chatId, 0)) {
            client?.send(TdApi.OpenChat(chatId), null)
        } else {
            Log.d(TAG, "[Ghost Mode] Blocked openChat / viewMessages for chatId $chatId")
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val content = TdApi.InputMessageText(
            TdApi.FormattedText(text, emptyArray()),
            null,
            false
        )
        client?.send(TdApi.SendMessage(chatId, 0, null, null, null, content), this)
    }

    fun sendTyping(chatId: Long) {
        // Ghost Mode Interception on Typing:
        if (!ExteraBridge.shouldBlockTyping(chatId, "typing")) {
            client?.send(TdApi.SendChatAction(chatId, 0, TdApi.ChatActionTyping()), null)
        } else {
            Log.d(TAG, "[Ghost Mode] Suppressed typing indicator for chatId $chatId")
        }
    }

    private fun updateChatList() {
        val list = rawChatMap.values.map { chat ->
            ExteraChat(
                id = chat.id,
                title = chat.title,
                lastMessageText = extractMessageText(chat.lastMessage?.content),
                date = chat.lastMessage?.date ?: 0,
                unreadCount = chat.unreadCount
            )
        }.sortedByDescending { it.date }
        _chats.value = list
    }

    private fun addMessageToChat(chatId: Long, msg: ExteraChatMessage) {
        val current = _chatMessages.value[chatId] ?: emptyList()
        _chatMessages.value = _chatMessages.value + (chatId to (current + msg))
    }

    private fun markMessagesDeletedInState(chatId: Long, ids: List<Long>) {
        val current = _chatMessages.value[chatId] ?: return
        val updated = current.map { m ->
            if (ids.contains(m.id)) m.copy(isDeletedByRemote = true) else m
        }
        _chatMessages.value = _chatMessages.value + (chatId to updated)
    }

    private fun extractMessageText(content: TdApi.MessageContent?): String {
        return when (content) {
            is TdApi.MessageText -> content.text.text
            is TdApi.MessagePhoto -> content.caption?.text?.ifEmpty { "📷 Фотография" } ?: "📷 Фотография"
            is TdApi.MessageVideo -> content.caption?.text?.ifEmpty { "🎥 Видео" } ?: "🎥 Видео"
            is TdApi.MessageVoiceNote -> "🎙️ Голосовое сообщение"
            is TdApi.MessageSticker -> "Стикер ${content.sticker.emoji}"
            is TdApi.MessageDocument -> "📄 ${content.document.fileName}"
            else -> "Сообщение"
        }
    }

    override fun onResult(obj: TdApi.Object?) {
        if (obj is TdApi.Error) {
            Log.e(TAG, "TDLib Error: ${obj.code} - ${obj.message}")
            if (_authState.value !is AuthState.LoggedIn) {
                _authState.value = AuthState.Error(obj.message)
            }
        }
    }
}
