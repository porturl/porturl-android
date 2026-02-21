package org.friesoft.porturl.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {
    fun vibrate(context: Context, effect: HapticEffect) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        val vibrationEffect = when (effect) {
            HapticEffect.DragStarted -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            HapticEffect.DragCellHover -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            HapticEffect.LongClick -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        }
        
        vibrator.vibrate(vibrationEffect)
    }
}

enum class HapticEffect {
    DragStarted,
    DragCellHover,
    LongClick
}
