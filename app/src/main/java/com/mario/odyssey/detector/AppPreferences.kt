package com.mario.odyssey.detector

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object AppPreferences {
    
    fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun setConfidenceThreshold(context: Context, value: Int) {
        getPreferences(context).edit().putInt("confidence_threshold", value).apply()
    }

    fun getConfidenceThreshold(context: Context): Float {
        return getPreferences(context).getInt("confidence_threshold", 30).toFloat() / 100f
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("vibration_enabled", true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("sound_enabled", true)
    }

    fun setRecordingEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("recording_enabled", enabled).apply()
    }

    fun isRecordingEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("recording_enabled", false)
    }

    fun setAutoScreenshot(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("auto_screenshot", enabled).apply()
    }

    fun isAutoScreenshot(context: Context): Boolean {
        return getPreferences(context).getBoolean("auto_screenshot", false)
    }
}
