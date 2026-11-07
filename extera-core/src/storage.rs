use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::RwLock;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct StoredMessage {
    pub id: i64,
    pub chat_id: i64,
    pub sender_id: i64,
    pub sender_name: String,
    pub text: String,
    pub timestamp: i64,
    pub is_deleted_by_remote: bool,
    pub deleted_timestamp: Option<i64>,
    pub edit_timestamp: Option<i64>,
    pub is_outgoing: bool,
}

pub static MESSAGE_STORAGE: once_cell::sync::Lazy<RwLock<HashMap<i64, Vec<StoredMessage>>>> =
    once_cell::sync::Lazy::new(|| RwLock::new(HashMap::new()));

pub struct StorageEngine;

impl StorageEngine {
    pub fn save_message(msg: StoredMessage) {
        let mut store = MESSAGE_STORAGE.write().unwrap();
        let list = store.entry(msg.chat_id).or_insert_with(Vec::new);
        if let Some(pos) = list.iter().position(|m| m.id == msg.id) {
            list[pos] = msg;
        } else {
            list.push(msg);
        }
    }

    pub fn mark_deleted(chat_id: i64, msg_id: i64, current_time: i64) -> bool {
        let mut store = MESSAGE_STORAGE.write().unwrap();
        if let Some(list) = store.get_mut(&chat_id) {
            if let Some(m) = list.iter_mut().find(|m| m.id == msg_id) {
                m.is_deleted_by_remote = true;
                m.deleted_timestamp = Some(current_time);
                return true;
            }
        }
        false
    }

    pub fn get_chat_messages(chat_id: i64) -> Vec<StoredMessage> {
        let store = MESSAGE_STORAGE.read().unwrap();
        store.get(&chat_id).cloned().unwrap_or_default()
    }
}
