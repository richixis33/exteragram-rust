use serde::{Deserialize, Serialize};
use std::sync::RwLock;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GhostConfig {
    pub hide_read_receipts: bool,
    pub hide_typing_status: bool,
    pub hide_online_status: bool,
    pub ghost_stream_mode: bool,
}

impl Default for GhostConfig {
    fn default() -> Self {
        Self {
            hide_read_receipts: true,
            hide_typing_status: true,
            hide_online_status: false,
            ghost_stream_mode: false,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LiquidGlassConfig {
    pub blur_radius: f32,          // e.g. 24.0 dp
    pub glass_opacity: f32,        // e.g. 0.65
    pub noise_sheen: bool,         // specular sheen effect
    pub chromatic_aberration: bool,
    pub border_glow_intensity: f32,// 0.0 - 1.0
}

impl Default for LiquidGlassConfig {
    fn default() -> Self {
        Self {
            blur_radius: 26.0,
            glass_opacity: 0.68,
            noise_sheen: true,
            chromatic_aberration: true,
            border_glow_intensity: 0.85,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ChatCustomizationConfig {
    pub bubble_corner_radius: f32, // 18.0 dp
    pub show_ids: bool,            // Show User/Chat ID in header & messages
    pub show_exact_edit_time: bool,
    pub anti_delete_enabled: bool, // Store deleted messages locally
    pub forward_without_quote: bool,
    pub custom_accent_seed: Option<String>, // Hex color or system Monet
}

impl Default for ChatCustomizationConfig {
    fn default() -> Self {
        Self {
            bubble_corner_radius: 18.0,
            show_ids: true,
            show_exact_edit_time: true,
            anti_delete_enabled: true,
            forward_without_quote: true,
            custom_accent_seed: None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct ExteraConfig {
    pub ghost: GhostConfig,
    pub liquid_glass: LiquidGlassConfig,
    pub chat: ChatCustomizationConfig,
}

pub static GLOBAL_CONFIG: once_cell::sync::Lazy<RwLock<ExteraConfig>> =
    once_cell::sync::Lazy::new(|| RwLock::new(ExteraConfig::default()));

pub fn get_config() -> ExteraConfig {
    GLOBAL_CONFIG.read().unwrap().clone()
}

pub fn update_config_from_json(json_str: &str) -> Result<ExteraConfig, serde_json::Error> {
    let new_cfg: ExteraConfig = serde_json::from_str(json_str)?;
    let mut lock = GLOBAL_CONFIG.write().unwrap();
    *lock = new_cfg.clone();
    Ok(new_cfg)
}
