package com.schulte.grid.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * 音效管理器 —— 使用 AudioTrack 实时合成音效（零音频文件）
 * 等效于 Web 版的 Web Audio API 方案
 */
class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** 播放正确点击音效（短促高音） */
    fun playCorrect() {
        scope.launch { playTone(880f, 0.06f, 0.09f) }
    }

    /** 播放错误点击音效（低沉方波） */
    fun playWrong() {
        scope.launch { playTone(180f, 0.18f, 0.07f, waveType = "square") }
    }

    /** 播放完成音效（三连音） */
    fun playComplete() {
        scope.launch {
            playTone(523f, 0.15f, 0.1f)
            delay(80)
            playTone(659f, 0.15f, 0.1f)
            delay(80)
            playTone(784f, 0.15f, 0.1f)
        }
    }

    /** 播放倒计时提示音 */
    fun playCountdown() {
        scope.launch { playTone(660f, 0.08f, 0.06f) }
    }

    private fun playTone(
        freq: Float,
        durationSec: Float,
        volume: Float,
        waveType: String = "sine",
        sampleRate: Int = 44100,
    ) {
        val numSamples = (sampleRate * durationSec).toInt()
        if (numSamples <= 0) return

        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val angle = 2.0f * PI.toFloat() * freq * t
            val sample = when (waveType) {
                "square" -> if (sin(angle.toDouble()) >= 0) 1.0f else -1.0f
                else -> sin(angle.toDouble()).toFloat()
            }
            // 应用淡出
            val envelope = if (t > durationSec * 0.7f) {
                1.0f - (t - durationSec * 0.7f) / (durationSec * 0.3f)
            } else 1.0f
            buffer[i] = (sample * volume * envelope * Short.MAX_VALUE).toInt().toShort()
        }

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
            .setBufferSizeInBytes(numSamples * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(buffer, 0, numSamples)
            track.play()
            // 不等待播放完成，避免阻塞
        } catch (_: Exception) {
            track.release()
        }
    }

    /** 释放资源 */
    fun release() {
        scope.cancel()
    }
}
