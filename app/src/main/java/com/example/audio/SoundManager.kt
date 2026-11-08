package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

enum class SoundEffect {
    ORB_PICKUP,
    GOLDEN_ORB,
    TOXIC_HIT,
    POWER_UP,
    EVOLUTION_UP,
    COMBO_MILESTONE,
    BOSS_HIT,
    BOSS_DEFEATED,
    GAME_OVER,
    BUTTON_CLICK,
    ACHIEVEMENT_UNLOCK
}

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var musicJob: Job? = null

    @Volatile
    var musicVolume: Float = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (field <= 0.01f) stopMusic()
        }

    @Volatile
    var sfxVolume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    @Volatile
    var vibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    fun playSfx(effect: SoundEffect) {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            try {
                when (effect) {
                    SoundEffect.ORB_PICKUP -> playToneSequence(
                        freqs = floatArrayOf(587.33f, 880f),
                        durationsMs = intArrayOf(45, 65),
                        volume = sfxVolume * 0.45f
                    )
                    SoundEffect.GOLDEN_ORB -> playToneSequence(
                        freqs = floatArrayOf(659.25f, 880f, 1174.66f),
                        durationsMs = intArrayOf(45, 50, 90),
                        volume = sfxVolume * 0.55f
                    )
                    SoundEffect.TOXIC_HIT -> playToneSequence(
                        freqs = floatArrayOf(220f, 155f),
                        durationsMs = intArrayOf(75, 110),
                        volume = sfxVolume * 0.55f
                    )
                    SoundEffect.POWER_UP -> playToneSequence(
                        freqs = floatArrayOf(440f, 554.37f, 659.25f, 880f),
                        durationsMs = intArrayOf(40, 40, 45, 95),
                        volume = sfxVolume * 0.55f
                    )
                    SoundEffect.EVOLUTION_UP -> playToneSequence(
                        freqs = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f),
                        durationsMs = intArrayOf(65, 65, 70, 160),
                        volume = sfxVolume * 0.65f
                    )
                    SoundEffect.COMBO_MILESTONE -> playToneSequence(
                        freqs = floatArrayOf(783.99f, 987.77f, 1174.66f),
                        durationsMs = intArrayOf(40, 45, 85),
                        volume = sfxVolume * 0.5f
                    )
                    SoundEffect.BOSS_HIT -> playToneSequence(
                        freqs = floatArrayOf(311.13f, 622.25f),
                        durationsMs = intArrayOf(50, 90),
                        volume = sfxVolume * 0.6f
                    )
                    SoundEffect.BOSS_DEFEATED -> playToneSequence(
                        freqs = floatArrayOf(440f, 554.37f, 659.25f, 880f, 1108.73f, 1318.5f),
                        durationsMs = intArrayOf(60, 60, 60, 70, 80, 190),
                        volume = sfxVolume * 0.7f
                    )
                    SoundEffect.GAME_OVER -> playToneSequence(
                        freqs = floatArrayOf(392f, 349.23f, 311.13f, 261.63f),
                        durationsMs = intArrayOf(85, 85, 95, 190),
                        volume = sfxVolume * 0.6f
                    )
                    SoundEffect.BUTTON_CLICK -> playToneSequence(
                        freqs = floatArrayOf(740f),
                        durationsMs = intArrayOf(28),
                        volume = sfxVolume * 0.35f
                    )
                    SoundEffect.ACHIEVEMENT_UNLOCK -> playToneSequence(
                        freqs = floatArrayOf(659.25f, 783.99f, 1046.5f, 1318.5f),
                        durationsMs = intArrayOf(55, 55, 65, 150),
                        volume = sfxVolume * 0.6f
                    )
                }
            } catch (_: Exception) {
                // Safe ignore on JVM unit test / no-audio devices
            }
        }
    }

    fun vibrate(durationMs: Long = 25L, heavy: Boolean = false) {
        if (!vibrationEnabled) return
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (heavy) 210 else 110
                v.vibrate(VibrationEffect.createOneShot(durationMs, amp))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    fun startMusic() {
        if (musicVolume <= 0.01f) return
        if (musicJob?.isActive == true) return

        musicJob = scope.launch {
            // Cyberpunk D-minor arpeggio bassline loop
            val notes = floatArrayOf(
                146.83f, 174.61f, 220.00f, 174.61f,
                130.81f, 164.81f, 196.00f, 164.81f,
                116.54f, 146.83f, 174.61f, 146.83f,
                130.81f, 164.81f, 220.00f, 261.63f
            )
            var step = 0
            while (isActive) {
                val vol = musicVolume
                if (vol > 0.01f) {
                    val freq = notes[step % notes.size]
                    try {
                        playToneSequence(
                            freqs = floatArrayOf(freq),
                            durationsMs = intArrayOf(170),
                            volume = vol * 0.16f
                        )
                    } catch (_: Exception) {
                    }
                    step++
                }
                delay(240L)
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopMusic()
        scope.cancel()
    }

    private fun playToneSequence(freqs: FloatArray, durationsMs: IntArray, volume: Float) {
        val sampleRate = 22050
        val totalMs = durationsMs.sum()
        val totalSamples = (sampleRate * totalMs) / 1000
        if (totalSamples <= 0) return

        val pcm = ShortArray(totalSamples)
        var sampleIndex = 0

        for (i in freqs.indices) {
            val freq = freqs[i]
            val count = (sampleRate * durationsMs[i]) / 1000
            for (s in 0 until count) {
                if (sampleIndex >= totalSamples) break
                val t = s.toDouble() / sampleRate
                // Smooth envelope to eliminate clicks
                val attack = (s.toFloat() / (count * 0.12f)).coerceAtMost(1f)
                val release = ((count - s).toFloat() / (count * 0.25f)).coerceAtMost(1f)
                val env = attack * release
                // Blend fundamental sine + soft octave harmonic for neon synth feel
                val wave = (sin(2.0 * PI * freq * t) * 0.75 + sin(4.0 * PI * freq * t) * 0.25)
                val sampleVal = (wave * env * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                pcm[sampleIndex++] = sampleVal.toShort()
            }
        }

        val bufferBytes = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferBytes.coerceAtLeast(2048))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep(totalMs.toLong() + 15L)
        track.stop()
        track.release()
    }
}
