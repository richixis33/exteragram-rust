pub mod config;
pub mod ghost;
pub mod storage;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;

use crate::config::{get_config, update_config_from_json};
use crate::ghost::{GhostEngine, TelegramAction, InterceptResult};
use crate::storage::{StorageEngine, StoredMessage};

/// Initialize Extera Rust Core
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeInitEngine(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    1 as jboolean
}

/// Retrieve the active JSON configuration
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeGetConfigJson(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let cfg = get_config();
    let json = serde_json::to_string(&cfg).unwrap_or_else(|_| "{}".to_string());
    env.new_string(json)
        .expect("Failed to create java string")
        .into_raw()
}

/// Update configuration from JSON
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeUpdateConfigJson(
    mut env: JNIEnv,
    _class: JClass,
    json: JString,
) -> jboolean {
    if let Ok(rust_str) = env.get_string(&json) {
        let str_ref = rust_str.to_str().unwrap_or("{}");
        if update_config_from_json(str_ref).is_ok() {
            return 1 as jboolean;
        }
    }
    0 as jboolean
}

/// Ghost Check: Should block sendReadReceipt
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeShouldBlockRead(
    _env: JNIEnv,
    _class: JClass,
    chat_id: jlong,
    max_id: jint,
) -> jboolean {
    let action = TelegramAction::SendReadReceipt {
        chat_id: chat_id as i64,
        max_id: max_id as i32,
    };
    if GhostEngine::intercept_action(&action) == InterceptResult::Block {
        1 as jboolean
    } else {
        0 as jboolean
    }
}

/// Ghost Check: Should block Typing indicator
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeShouldBlockTyping(
    mut env: JNIEnv,
    _class: JClass,
    chat_id: jlong,
    action_str: JString,
) -> jboolean {
    let act = env
        .get_string(&action_str)
        .map(|s| s.to_str().unwrap_or("typing").to_string())
        .unwrap_or_else(|_| "typing".to_string());

    let action = TelegramAction::SetTyping {
        chat_id: chat_id as i64,
        action: act,
    };
    if GhostEngine::intercept_action(&action) == InterceptResult::Block {
        1 as jboolean
    } else {
        0 as jboolean
    }
}

/// Ghost Check: Should block Online status
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeShouldBlockOnline(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let action = TelegramAction::UpdateStatus { is_online: true };
    if GhostEngine::intercept_action(&action) == InterceptResult::Block {
        1 as jboolean
    } else {
        0 as jboolean
    }
}

/// Record message to Rust Anti-Delete storage
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeRecordMessage(
    mut env: JNIEnv,
    _class: JClass,
    chat_id: jlong,
    msg_id: jlong,
    sender_id: jlong,
    sender_name: JString,
    text: JString,
    is_outgoing: jboolean,
) {
    let name_str = env
        .get_string(&sender_name)
        .map(|s| s.to_str().unwrap_or("").to_string())
        .unwrap_or_default();
    let text_str = env
        .get_string(&text)
        .map(|s| s.to_str().unwrap_or("").to_string())
        .unwrap_or_default();

    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as i64;

    StorageEngine::save_message(StoredMessage {
        id: msg_id as i64,
        chat_id: chat_id as i64,
        sender_id: sender_id as i64,
        sender_name: name_str,
        text: text_str,
        timestamp: now,
        is_deleted_by_remote: false,
        deleted_timestamp: None,
        edit_timestamp: None,
        is_outgoing: is_outgoing != 0,
    });
}

/// Mark a message as remotely deleted (preserving it in storage if anti-delete enabled)
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeMarkMessageDeleted(
    _env: JNIEnv,
    _class: JClass,
    chat_id: jlong,
    msg_id: jlong,
) -> jboolean {
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as i64;

    if StorageEngine::mark_deleted(chat_id as i64, msg_id as i64, now) {
        1 as jboolean
    } else {
        0 as jboolean
    }
}

/// Get all messages for chat as JSON
#[no_mangle]
pub extern "system" fn Java_org_exteragram_rust_core_ExteraBridge_nativeGetChatMessagesJson(
    mut env: JNIEnv,
    _class: JClass,
    chat_id: jlong,
) -> jstring {
    let msgs = StorageEngine::get_chat_messages(chat_id as i64);
    let json = serde_json::to_string(&msgs).unwrap_or_else(|_| "[]".to_string());
    env.new_string(json)
        .expect("Failed to create java string")
        .into_raw()
}
