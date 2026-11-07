package org.exteragram.rust.core

import android.util.Log

data class GhostConfig(
    var hideReadReceipts: Boolean = true,
    var hideTypingStatus: Boolean = true,
    var hideOnlineStatus: Boolean = false,
    var ghostStreamMode: Boolean = false
)

data class LiquidGlassConfig(
    var blurRadius: Float = 24.0f,
    var glassOpacity: Float = 0.65f,
    var noiseSheen: Boolean = true,
    var chromaticAberration: Boolean = true,
    var borderGlowIntensity: Float = 0.85f
)

data class ChatConfig(
    var bubbleCornerRadius: Float = 18.0f,
    var showIds: Boolean = true,
    var showExactEditTime: Boolean = true,
    var antiDeleteEnabled: Boolean = true,
    var forwardWithoutQuote: Boolean = true,
    var customAccentSeed: String? = null
)

data class ExteraState(
    var ghost: GhostConfig = GhostConfig(),
    var glass: LiquidGlassConfig = LiquidGlassConfig(),
    var chat: ChatConfig = ChatConfig()
)

object ExteraBridge {
    private const val TAG = "ExteraBridge"
    private var isNativeLoaded = false

    val state = ExteraState()

    fun init() {
        try {
            isNativeLoaded = nativeInitEngine()
            Log.d(TAG, "Rust engine init status: $isNativeLoaded")
        } catch (e: Throwable) {
            Log.e(TAG, "Native init error: ${e.message}")
            isNativeLoaded = false
        }
    }

    fun shouldBlockReadReceipt(chatId: Long, maxId: Int): Boolean {
        if (isNativeLoaded) {
            return nativeShouldBlockRead(chatId, maxId)
        }
        return state.ghost.hideReadReceipts
    }

    fun shouldBlockTyping(chatId: Long, action: String): Boolean {
        if (isNativeLoaded) {
            return nativeShouldBlockTyping(chatId, action)
        }
        return state.ghost.hideTypingStatus
    }

    fun shouldBlockOnline(): Boolean {
        if (isNativeLoaded) {
            return nativeShouldBlockOnline()
        }
        return state.ghost.hideOnlineStatus
    }

    fun recordMessage(
        chatId: Long,
        msgId: Long,
        senderId: Long,
        senderName: String,
        text: String,
        isOutgoing: Boolean
    ) {
        if (isNativeLoaded) {
            nativeRecordMessage(chatId, msgId, senderId, senderName, text, isOutgoing)
        }
    }

    fun markMessageDeleted(chatId: Long, msgId: Long): Boolean {
        if (isNativeLoaded) {
            return nativeMarkMessageDeleted(chatId, msgId)
        }
        return true
    }

    // JNI External Declarations
    private external fun nativeInitEngine(): Boolean
    private external fun nativeGetConfigJson(): String
    private external fun nativeUpdateConfigJson(json: String): Boolean
    private external fun nativeShouldBlockRead(chatId: Long, maxId: Int): Boolean
    private external fun nativeShouldBlockTyping(chatId: Long, action: String): Boolean
    private external fun nativeShouldBlockOnline(): Boolean
    private external fun nativeRecordMessage(
        chatId: Long,
        msgId: Long,
        senderId: Long,
        senderName: String,
        text: String,
        isOutgoing: Boolean
    )
    private external fun nativeMarkMessageDeleted(chatId: Long, msgId: Long): Boolean
    private external fun nativeGetChatMessagesJson(chatId: Long): String
}
