use crate::config::get_config;
use log::info;

pub enum TelegramAction {
    SendReadReceipt { chat_id: i64, max_id: i32 },
    SetTyping { chat_id: i64, action: String },
    UpdateStatus { is_online: bool },
    DeleteMessage { chat_id: i64, msg_id: i32 },
}

#[derive(Debug, PartialEq, Eq)]
pub enum InterceptResult {
    Pass,
    Block,
    Modify(String),
}

pub struct GhostEngine;

impl GhostEngine {
    /// Evaluates whether an outgoing packet should reach the Telegram network
    pub fn intercept_action(action: &TelegramAction) -> InterceptResult {
        let cfg = get_config().ghost;

        match action {
            TelegramAction::SendReadReceipt { chat_id, max_id } => {
                if cfg.hide_read_receipts {
                    info!("[GhostEngine] Blocked read receipt for chat {}, max_id {}", chat_id, max_id);
                    InterceptResult::Block
                } else {
                    InterceptResult::Pass
                }
            }
            TelegramAction::SetTyping { chat_id, action } => {
                if cfg.hide_typing_status {
                    info!("[GhostEngine] Suppressed typing indicator ({}) in chat {}", action, chat_id);
                    InterceptResult::Block
                } else {
                    InterceptResult::Pass
                }
            }
            TelegramAction::UpdateStatus { is_online } => {
                if *is_online && cfg.hide_online_status {
                    info!("[GhostEngine] Masked online status update");
                    InterceptResult::Block
                } else {
                    InterceptResult::Pass
                }
            }
            TelegramAction::DeleteMessage { .. } => InterceptResult::Pass,
        }
    }
}
