package com.example.vr.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-immersion procedural VR audio synthesizer for high-altitude rooftop experience.
 * Synthesizes dynamic 1,000-ft wind gusts, low-frequency city traffic drone,
 * and soft atmospheric ambient musical chords in real time via an AudioTrack PCM stream,
 * coupled with a full TextToSpeech tour narration guide.
 */
class VrAudioEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var synthesisThread: Thread? = null

    // Volume sliders (0.0f to 1.0f)
    var masterVolume: Float = 0.85f
    var windVolume: Float = 0.70f
    var trafficVolume: Float = 0.55f
    var ambientMusicVolume: Float = 0.45f
    var narrationVolume: Float = 1.0f

    // Android TTS for voice tour guide
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VrAudioEngine", "TTS init error: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let {
                val result = it.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    it.setSpeechRate(0.95f)
                    it.setPitch(1.0f)
                    ttsReady = true
                }
            }
        }
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            synthesisThread = Thread {
                runProceduralAudioLoop(bufferSize)
            }.apply {
                name = "VrAudioSynthThread"
                priority = Thread.NORM_PRIORITY
                start()
            }
        } catch (e: Exception) {
            Log.e("VrAudioEngine", "Failed to start audio track: ${e.message}")
        }
    }

    private fun runProceduralAudioLoop(bufferSize: Int) {
        val pcmBuffer = ShortArray(bufferSize)
        val random = Random()

        // Low-pass filter memories for pink wind and traffic sub-bass
        var windFilterL = 0f
        var windFilterR = 0f
        var trafficFilterL = 0f
        var trafficFilterR = 0f

        var gustPhase = 0.0
        var chordPhase1 = 0.0
        var chordPhase2 = 0.0
        var chordPhase3 = 0.0

        // Chord frequencies (soft C minor 9 ambient pad: C3 130.8Hz, G3 196.0Hz, Eb4 311.1Hz)
        val f1 = 130.81
        val f2 = 196.00
        val f3 = 311.13

        val twoPi = 2.0 * PI
        val dt = 1.0 / sampleRate

        while (isPlaying) {
            val samplesToGenerate = bufferSize / 2 // stereo pairs

            val currentMaster = masterVolume
            val currentWind = windVolume * currentMaster
            val currentTraffic = trafficVolume * currentMaster
            val currentMusic = ambientMusicVolume * currentMaster

            for (i in 0 until samplesToGenerate) {
                // 1. Skyscraper Wind Gusts (Dynamic breathing filtered noise)
                gustPhase += dt * 0.18 // slow gust cycle (~5.5 seconds)
                if (gustPhase > twoPi) gustPhase -= twoPi
                val gustStrength = 0.45f + 0.35f * (sin(gustPhase).toFloat() * sin(gustPhase * 1.7).toFloat())

                val rawNoiseL = (random.nextFloat() * 2f - 1f)
                val rawNoiseR = (random.nextFloat() * 2f - 1f)

                // Gentle low-pass filtering for smooth aerodynamic rooftop wind
                windFilterL += 0.06f * (rawNoiseL - windFilterL)
                windFilterR += 0.06f * (rawNoiseR - windFilterR)
                val windOutL = windFilterL * gustStrength * currentWind
                val windOutR = windFilterR * gustStrength * currentWind

                // 2. Distant City Traffic Drone (Sub-bass rumble 50Hz)
                val rawTrafficL = (random.nextFloat() * 2f - 1f)
                val rawTrafficR = (random.nextFloat() * 2f - 1f)
                trafficFilterL += 0.012f * (rawTrafficL - trafficFilterL)
                trafficFilterR += 0.012f * (rawTrafficR - trafficFilterR)
                val trafficOutL = trafficFilterL * 1.8f * currentTraffic
                val trafficOutR = trafficFilterR * 1.8f * currentTraffic

                // 3. Atmospheric Calming Synth Chords
                chordPhase1 += twoPi * f1 * dt
                chordPhase2 += twoPi * f2 * dt
                chordPhase3 += twoPi * f3 * dt
                if (chordPhase1 > twoPi) chordPhase1 -= twoPi
                if (chordPhase2 > twoPi) chordPhase2 -= twoPi
                if (chordPhase3 > twoPi) chordPhase3 -= twoPi

                val padSample = (sin(chordPhase1) * 0.45 + sin(chordPhase2) * 0.35 + sin(chordPhase3) * 0.25).toFloat()
                val musicOut = padSample * 0.25f * currentMusic

                // Mix left and right
                val mixL = (windOutL + trafficOutL + musicOut).coerceIn(-1f, 1f)
                val mixR = (windOutR + trafficOutR + musicOut).coerceIn(-1f, 1f)

                // Write into 16-bit PCM buffer
                pcmBuffer[i * 2] = (mixL * 32767f).toInt().toShort()
                pcmBuffer[i * 2 + 1] = (mixR * 32767f).toInt().toShort()
            }

            audioTrack?.write(pcmBuffer, 0, bufferSize)
        }
    }

    fun speakNarration(text: String) {
        if (!ttsReady || tts == null) return
        try {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TourGuideNarration")
        } catch (e: Exception) {
            Log.e("VrAudioEngine", "Narration error: ${e.message}")
        }
    }

    fun stopNarration() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun stop() {
        isPlaying = false
        synthesisThread?.interrupt()
        synthesisThread = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignored
        }
        audioTrack = null
    }

    fun release() {
        stop()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignored
        }
        tts = null
    }
}
