package com.altron.voice

import android.app.Activity
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import android.speech.tts.TextToSpeech
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var speech: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(25, 30, 25, 30)
        layout.setBackgroundColor(Color.BLACK)

        val title = TextView(this)
        title.text = "A L T R O N"
        title.textSize = 32f
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.RED)

        status = TextView(this)
        status.text = "SYSTEM STANDBY"
        status.textSize = 20f
        status.gravity = Gravity.CENTER
        status.setTextColor(Color.WHITE)

        val button = Button(this)
        button.text = "ACTIVATE VOICE"
        button.setOnClickListener { startVoice() }

        layout.addView(title)
        layout.addView(status)
        layout.addView(button)
        setContentView(layout)

        speech = SpeechRecognizer.createSpeechRecognizer(this)
    }

    private fun startVoice() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speech?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                status.text = "LISTENING..."
            }
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )?.firstOrNull()?.lowercase() ?: ""

                status.text = words
                command(words)
            }
            override fun onError(error: Int) {
                status.text = "TRY AGAIN"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speech?.startListening(intent)
    }

    private fun command(words: String) {
        when {
            words.contains("youtube") -> {
                speak("Opening YouTube")
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://youtube.com")))
            }
            words.contains("google") -> {
                speak("Opening Google")
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://google.com")))
            }
            words.contains("hello") || words.contains("salam") -> {
                speak("Hello. ALTRON is online.")
            }
            else -> speak("Command received.")
        }
    }

    private fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "altron")
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        speech?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}
