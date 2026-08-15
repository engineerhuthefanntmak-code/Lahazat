package com.floating.stopwatch.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val MAIN_SIZE = floatPreferencesKey("main_size")
        val FLOATING_SIZE = floatPreferencesKey("floating_size")
        val FLOATING_WIDTH = floatPreferencesKey("floating_width")
        val FLOATING_HEIGHT = floatPreferencesKey("floating_height")
        val SHOW_CENTISECONDS_MAIN = booleanPreferencesKey("show_centiseconds_main")
        val SHOW_CENTISECONDS_FLOATING = booleanPreferencesKey("show_centiseconds_floating")
        val STYLE_PRESET = stringPreferencesKey("style_preset")
        val COLOR_PRESET = stringPreferencesKey("color_preset")
        val CUSTOM_COLOR_HEX = stringPreferencesKey("custom_color_hex")
        val FLOATING_X = floatPreferencesKey("floating_x")
        val FLOATING_Y = floatPreferencesKey("floating_y")
        val EXPERIENCE_LEVEL = stringPreferencesKey("experience_level")
        val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val SHAPE_PRESET = stringPreferencesKey("shape_preset")
        val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        val GRADIENT_ENABLED = booleanPreferencesKey("gradient_enabled")
        val MESH_GRADIENT_ENABLED = booleanPreferencesKey("mesh_gradient_enabled")
        val ENERGY_AURA_ENABLED = booleanPreferencesKey("energy_aura_enabled")
        val AURA_EFFECT_TYPE = stringPreferencesKey("aura_effect_type")
        val VOLUME_COUNTER_SCREEN_OFF_ENABLED = booleanPreferencesKey("volume_counter_screen_off_enabled")
        val LAYOUT_ORIENTATION = stringPreferencesKey("layout_orientation")
        val ACTIVE_WIDGETS_COUNT = intPreferencesKey("active_widgets_count")
        val FLOATING_PADDING = floatPreferencesKey("floating_padding")
        val FLOATING_OPACITY = floatPreferencesKey("floating_opacity")
        val GLOWING_BORDER = booleanPreferencesKey("glowing_border")
        val SHOW_BATTERY_INDICATOR = booleanPreferencesKey("show_battery_indicator")
        val STATUS_INDICATOR_MODE = stringPreferencesKey("status_indicator_mode")
        val UNIFIED_SIZE_ENABLED = booleanPreferencesKey("unified_size_enabled")
        val UNIFIED_WIDTH = floatPreferencesKey("unified_width")
        val UNIFIED_HEIGHT = floatPreferencesKey("unified_height")
    }

    val unifiedSizeEnabled: Flow<Boolean> = context.dataStore.data.map { it[UNIFIED_SIZE_ENABLED] ?: false }
    val unifiedWidth: Flow<Float> = context.dataStore.data.map { it[UNIFIED_WIDTH] ?: 170.0f }
    val unifiedHeight: Flow<Float> = context.dataStore.data.map { it[UNIFIED_HEIGHT] ?: 56.0f }

    suspend fun setUnifiedSizeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[UNIFIED_SIZE_ENABLED] = enabled }
    }

    suspend fun setUnifiedWidth(width: Float) {
        context.dataStore.edit {
            it[UNIFIED_WIDTH] = width
            if (it[UNIFIED_SIZE_ENABLED] == true) {
                for (i in 0..4) {
                    it[floatPreferencesKey("widget_${i}_width")] = width
                }
            }
        }
    }

    suspend fun setUnifiedHeight(height: Float) {
        context.dataStore.edit {
            it[UNIFIED_HEIGHT] = height
            if (it[UNIFIED_SIZE_ENABLED] == true) {
                for (i in 0..4) {
                    it[floatPreferencesKey("widget_${i}_height")] = height
                }
            }
        }
    }

    // Dynamic Indexed Preferences for Multi-Widget (up to 5 concurrent widgets)
    fun isWidgetActive(index: Int): Flow<Boolean> = context.dataStore.data.map {
        it[booleanPreferencesKey("widget_${index}_active")] ?: (index == 0) // widget 0 is active by default
    }

    fun getWidgetType(index: Int): Flow<String> = context.dataStore.data.map {
        it[stringPreferencesKey("widget_${index}_type")] ?: when (index) {
            1 -> "countdown"
            2 -> "counter"
            else -> "stopwatch"
        }
    }

    fun getWidgetX(index: Int): Flow<Float> = context.dataStore.data.map {
        it[floatPreferencesKey("widget_${index}_x")] ?: (100f + index * 40f)
    }

    fun getWidgetY(index: Int): Flow<Float> = context.dataStore.data.map {
        it[floatPreferencesKey("widget_${index}_y")] ?: (200f + index * 80f)
    }

    fun getWidgetValue(index: Int): Flow<Long> = context.dataStore.data.map {
        it[longPreferencesKey("widget_${index}_value")] ?: 0L
    }

    fun isWidgetRunning(index: Int): Flow<Boolean> = context.dataStore.data.map {
        it[booleanPreferencesKey("widget_${index}_running")] ?: false
    }

    fun getWidgetWidth(index: Int): Flow<Float> = context.dataStore.data.map {
        it[floatPreferencesKey("widget_${index}_width")] ?: (it[FLOATING_WIDTH] ?: 170.0f)
    }

    fun getWidgetHeight(index: Int): Flow<Float> = context.dataStore.data.map {
        it[floatPreferencesKey("widget_${index}_height")] ?: (it[FLOATING_HEIGHT] ?: 56.0f)
    }

    fun getWidgetCountdownDuration(index: Int): Flow<Int> = context.dataStore.data.map {
        it[intPreferencesKey("widget_${index}_countdown_duration")] ?: 300 // 5 minutes default
    }

    suspend fun setWidgetActive(index: Int, active: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey("widget_${index}_active")] = active }
    }

    suspend fun setWidgetType(index: Int, type: String) {
        context.dataStore.edit { it[stringPreferencesKey("widget_${index}_type")] = type }
    }

    suspend fun setWidgetPosition(index: Int, x: Float, y: Float) {
        context.dataStore.edit {
            it[floatPreferencesKey("widget_${index}_x")] = x
            it[floatPreferencesKey("widget_${index}_y")] = y
        }
    }

    suspend fun setWidgetValue(index: Int, value: Long) {
        context.dataStore.edit { it[longPreferencesKey("widget_${index}_value")] = value }
    }

    suspend fun setWidgetRunning(index: Int, running: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey("widget_${index}_running")] = running }
    }

    suspend fun setWidgetWidth(index: Int, width: Float) {
        context.dataStore.edit { it[floatPreferencesKey("widget_${index}_width")] = width }
    }

    suspend fun setWidgetHeight(index: Int, height: Float) {
        context.dataStore.edit { it[floatPreferencesKey("widget_${index}_height")] = height }
    }

    suspend fun setWidgetCountdownDuration(index: Int, seconds: Int) {
        context.dataStore.edit { it[intPreferencesKey("widget_${index}_countdown_duration")] = seconds }
    }

    val biometricLock: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_LOCK] ?: false }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "Midnight" }

    val mainSize: Flow<Float> = context.dataStore.data.map { it[MAIN_SIZE] ?: 1.0f }
    val floatingSize: Flow<Float> = context.dataStore.data.map { it[FLOATING_SIZE] ?: 0.5f }
    val floatingWidth: Flow<Float> = context.dataStore.data.map { it[FLOATING_WIDTH] ?: 170.0f }
    val floatingHeight: Flow<Float> = context.dataStore.data.map { it[FLOATING_HEIGHT] ?: 56.0f }

    val shapePreset: Flow<String> = context.dataStore.data.map { it[SHAPE_PRESET] ?: "rounded" }
    val fontSizeScale: Flow<Float> = context.dataStore.data.map { it[FONT_SIZE_SCALE] ?: 1.0f }
    val gradientEnabled: Flow<Boolean> = context.dataStore.data.map { it[GRADIENT_ENABLED] ?: false }
    val meshGradientEnabled: Flow<Boolean> = context.dataStore.data.map { it[MESH_GRADIENT_ENABLED] ?: true }
    val energyAuraEnabled: Flow<Boolean> = context.dataStore.data.map { it[ENERGY_AURA_ENABLED] ?: true }
    val auraEffectType: Flow<String> = context.dataStore.data.map { it[AURA_EFFECT_TYPE] ?: "Ribbons & Sparks" }
    val volumeCounterScreenOffEnabled: Flow<Boolean> = context.dataStore.data.map { it[VOLUME_COUNTER_SCREEN_OFF_ENABLED] ?: false }
    val layoutOrientation: Flow<String> = context.dataStore.data.map { it[LAYOUT_ORIENTATION] ?: "horizontal" }
    val activeWidgetsCount: Flow<Int> = context.dataStore.data.map { it[ACTIVE_WIDGETS_COUNT] ?: 1 }

    val floatingPadding: Flow<Float> = context.dataStore.data.map { it[FLOATING_PADDING] ?: 6.0f }
    val floatingOpacity: Flow<Float> = context.dataStore.data.map { it[FLOATING_OPACITY] ?: 0.85f }
    val glowingBorder: Flow<Boolean> = context.dataStore.data.map { it[GLOWING_BORDER] ?: false }
    val showBatteryIndicator: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BATTERY_INDICATOR] ?: true }
    val statusIndicatorMode: Flow<String> = context.dataStore.data.map { it[STATUS_INDICATOR_MODE] ?: "battery" }

    val showCentisecondsMain: Flow<Boolean> = context.dataStore.data.map { it[SHOW_CENTISECONDS_MAIN] ?: true }
    val showCentisecondsFloating: Flow<Boolean> = context.dataStore.data.map { it[SHOW_CENTISECONDS_FLOATING] ?: true }

    val stylePreset: Flow<String> = context.dataStore.data.map { it[STYLE_PRESET] ?: "Glass" }
    val colorPreset: Flow<String> = context.dataStore.data.map { it[COLOR_PRESET] ?: "Gold" }
    val customColorHex: Flow<String> = context.dataStore.data.map { it[CUSTOM_COLOR_HEX] ?: "#C9A66B" }

    val floatingX: Flow<Float> = context.dataStore.data.map { it[FLOATING_X] ?: -1.0f }
    val floatingY: Flow<Float> = context.dataStore.data.map { it[FLOATING_Y] ?: -1.0f }

    val experienceLevel: Flow<String> = context.dataStore.data.map { it[EXPERIENCE_LEVEL] ?: "Premium" }
    val hapticIntensity: Flow<String> = context.dataStore.data.map { it[HAPTIC_INTENSITY] ?: "Medium" }

    suspend fun setMainSize(size: Float) {
        context.dataStore.edit { it[MAIN_SIZE] = size }
    }

    suspend fun setFloatingSize(size: Float) {
        context.dataStore.edit { it[FLOATING_SIZE] = size }
    }

    suspend fun setFloatingWidth(width: Float) {
        context.dataStore.edit {
            it[FLOATING_WIDTH] = width
            for (i in 0..4) {
                it[floatPreferencesKey("widget_${i}_width")] = width
            }
        }
    }

    suspend fun setFloatingHeight(height: Float) {
        context.dataStore.edit {
            it[FLOATING_HEIGHT] = height
            for (i in 0..4) {
                it[floatPreferencesKey("widget_${i}_height")] = height
            }
        }
    }

    suspend fun setShowCentisecondsMain(show: Boolean) {
        context.dataStore.edit { it[SHOW_CENTISECONDS_MAIN] = show }
    }

    suspend fun setShowCentisecondsFloating(show: Boolean) {
        context.dataStore.edit { it[SHOW_CENTISECONDS_FLOATING] = show }
    }

    suspend fun setStylePreset(preset: String) {
        context.dataStore.edit { it[STYLE_PRESET] = preset }
    }

    suspend fun setColorPreset(preset: String) {
        context.dataStore.edit { it[COLOR_PRESET] = preset }
    }

    suspend fun setCustomColorHex(hex: String) {
        context.dataStore.edit { it[CUSTOM_COLOR_HEX] = hex }
    }

    suspend fun setFloatingPosition(x: Float, y: Float) {
        context.dataStore.edit {
            it[FLOATING_X] = x
            it[FLOATING_Y] = y
        }
    }

    suspend fun setExperienceLevel(level: String) {
        context.dataStore.edit { it[EXPERIENCE_LEVEL] = level }
    }

    suspend fun setHapticIntensity(intensity: String) {
        context.dataStore.edit { it[HAPTIC_INTENSITY] = intensity }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_LOCK] = enabled }
    }

    suspend fun setShapePreset(preset: String) {
        context.dataStore.edit { it[SHAPE_PRESET] = preset }
    }

    suspend fun setFontSizeScale(scale: Float) {
        context.dataStore.edit { it[FONT_SIZE_SCALE] = scale }
    }

    suspend fun setGradientEnabled(enabled: Boolean) {
        context.dataStore.edit { it[GRADIENT_ENABLED] = enabled }
    }

    suspend fun setMeshGradientEnabled(enabled: Boolean) {
        context.dataStore.edit { it[MESH_GRADIENT_ENABLED] = enabled }
    }

    suspend fun setEnergyAuraEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ENERGY_AURA_ENABLED] = enabled }
    }

    suspend fun setAuraEffectType(type: String) {
        context.dataStore.edit { it[AURA_EFFECT_TYPE] = type }
    }

    suspend fun setVolumeCounterScreenOffEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOLUME_COUNTER_SCREEN_OFF_ENABLED] = enabled }
    }

    suspend fun setLayoutOrientation(orientation: String) {
        context.dataStore.edit { it[LAYOUT_ORIENTATION] = orientation }
    }

    suspend fun setFloatingPadding(padding: Float) {
        context.dataStore.edit { it[FLOATING_PADDING] = padding }
    }

    suspend fun setFloatingOpacity(opacity: Float) {
        context.dataStore.edit { it[FLOATING_OPACITY] = opacity }
    }

    suspend fun setGlowingBorder(enabled: Boolean) {
        context.dataStore.edit { it[GLOWING_BORDER] = enabled }
    }

    suspend fun setShowBatteryIndicator(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_BATTERY_INDICATOR] = enabled }
    }

    suspend fun setStatusIndicatorMode(mode: String) {
        context.dataStore.edit { it[STATUS_INDICATOR_MODE] = mode }
    }

    suspend fun setActiveWidgetsCount(count: Int) {
        context.dataStore.edit { it[ACTIVE_WIDGETS_COUNT] = count }
    }
}
