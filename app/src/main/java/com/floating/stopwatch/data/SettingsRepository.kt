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
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "Midnight" }

    val mainSize: Flow<Float> = context.dataStore.data.map { it[MAIN_SIZE] ?: 1.0f }
    val floatingSize: Flow<Float> = context.dataStore.data.map { it[FLOATING_SIZE] ?: 0.5f }

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
}
