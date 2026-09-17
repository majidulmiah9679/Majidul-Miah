package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ai.GeminiManager
import com.example.service.JarvisAccessibilityService
import com.example.system.JarvisDiagnostics
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var etApiKey: EditText
    private lateinit var btnValidateKey: Button
    private lateinit var viewStatusDot: View
    private lateinit var tvStatusMessage: TextView
    private lateinit var tvTerminalOutput: TextView
    private lateinit var tvSystemTelemetry: TextView
    private lateinit var btnVoiceCommand: FloatingActionButton
    private lateinit var btnAccessibility: Button
    private lateinit var btnNotification: Button
    private lateinit var btnOverlayPermission: Button
    private lateinit var btnQuickHome: Button
    private lateinit var btnQuickLock: Button
    private lateinit var btnBatteryOptimization: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val geminiManager = GeminiManager()
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    companion object {
        private const val PREFS_NAME = "jarvis_settings"
        private const val KEY_API = "gemini_api_key"
        private const val REQUEST_RECORD_AUDIO = 101
        private const val REQUEST_OVERLAY_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        textToSpeech = TextToSpeech(this, this)

        // সেভ করা API Key থাকলে স্বয়ংক্রিয়ভাবে ভেরিফাই করা
        val savedKey = sharedPreferences.getString(KEY_API, "") ?: ""
        if (savedKey.isNotEmpty()) {
            etApiKey.setText(savedKey)
            validateKey(savedKey)
        } else {
            setKeyStatus(isValid = false, message = "No API Key configured (Red)")
        }

        setupListeners()
        requestAppPermissions()
        startTelemetryLoop()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionButtonStates()
    }

    private fun initViews() {
        etApiKey = findViewById(R.id.etApiKey)
        btnValidateKey = findViewById(R.id.btnValidateKey)
        viewStatusDot = findViewById(R.id.viewStatusDot)
        tvStatusMessage = findViewById(R.id.tvStatusMessage)
        tvTerminalOutput = findViewById(R.id.tvTerminalOutput)
        tvSystemTelemetry = findViewById(R.id.tvSystemTelemetry)
        btnVoiceCommand = findViewById(R.id.btnVoiceCommand)
        btnAccessibility = findViewById(R.id.btnAccessibility)
        btnNotification = findViewById(R.id.btnNotification)
        btnOverlayPermission = findViewById(R.id.btnOverlayPermission)
        btnQuickHome = findViewById(R.id.btnQuickHome)
        btnQuickLock = findViewById(R.id.btnQuickLock)
        btnBatteryOptimization = findViewById(R.id.btnBatteryOptimization)
    }

    private fun setupListeners() {
        // ১. API Key সেভ এবং লাইভ ভ্যালিডেশন
        btnValidateKey.setOnClickListener {
            val key = etApiKey.text.toString().trim()
            validateKey(key)
        }

        // ২. Accessibility সেটিংসে যাওয়ার বাটন
        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Enable 'Jarvis Assistant' Accessibility Service", Toast.LENGTH_LONG).show()
        }

        // ৩. Notification Listener সেটিংসে যাওয়ার বাটন
        btnNotification.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, "Allow 'Jarvis Assistant' Notification Access", Toast.LENGTH_LONG).show()
        }

        // ৪. Display over other apps (Overlay / HUD) পারমিশন বাটন
        btnOverlayPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                    Toast.makeText(this, "Enable 'Allow display over other apps'", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Overlay permission already granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ৫. ব্যাটারি অপ্টিমাইজেশন ছাড় (Background Keep Alive)
        btnBatteryOptimization.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Battery optimization already ignored (Unrestricted)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ৬. কুইক সিস্টেম হোম অ্যাকশন
        btnQuickHome.setOnClickListener {
            val service = JarvisAccessibilityService.instance
            if (service != null) {
                service.goToHomeScreen()
                speakOut("Navigating Home Boss.")
            } else {
                Toast.makeText(this, "Please enable Accessibility Service first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        // ৭. কুইক স্ক্রিন লক অ্যাকশন
        btnQuickLock.setOnClickListener {
            val service = JarvisAccessibilityService.instance
            if (service != null) {
                service.lockScreen()
                speakOut("Locking screen Boss.")
            } else {
                Toast.makeText(this, "Please enable Accessibility Service first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        // ৮. ভয়েস কমান্ড বাটন
        btnVoiceCommand.setOnClickListener {
            startListening()
        }
    }

    private fun updatePermissionButtonStates() {
        val accessibilityActive = JarvisAccessibilityService.instance != null
        btnAccessibility.text = if (accessibilityActive) "✓ Access Active" else "Accessibility"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val overlayActive = Settings.canDrawOverlays(this)
            btnOverlayPermission.text = if (overlayActive) "✓ HUD Active" else "Overlay (HUD)"
        }
    }

    private fun startTelemetryLoop() {
        lifecycleScope.launch {
            while (isActive) {
                try {
                    val telemetry = JarvisDiagnostics.getTelemetry(this@MainActivity)
                    val chargeBadge = if (telemetry.isCharging) "⚡" else ""
                    tvSystemTelemetry.text = "$chargeBadge${telemetry.batteryPercent}% | ${telemetry.availableRamMb}MB RAM"
                } catch (e: Exception) {
                    // Ignore transient exceptions in telemetry
                }
                delay(3000)
            }
        }
    }

    private fun validateKey(apiKey: String) {
        tvStatusMessage.text = "Testing API Key..."
        viewStatusDot.setBackgroundColor(Color.YELLOW)

        lifecycleScope.launch {
            when (val status = geminiManager.validateAndInitializeKey(apiKey)) {
                is GeminiManager.KeyValidationStatus.Valid -> {
                    sharedPreferences.edit().putString(KEY_API, apiKey).apply()
                    setKeyStatus(isValid = true, message = "API Key Active & Verified (Green)")
                }
                is GeminiManager.KeyValidationStatus.Invalid -> {
                    setKeyStatus(isValid = false, message = "Invalid Key: ${status.error} (Red)")
                }
            }
        }
    }

    private fun setKeyStatus(isValid: Boolean, message: String) {
        tvStatusMessage.text = message
        if (isValid) {
            viewStatusDot.setBackgroundColor(Color.parseColor("#2EA043")) // GREEN
            tvStatusMessage.setTextColor(Color.parseColor("#7EE787"))
        } else {
            viewStatusDot.setBackgroundColor(Color.parseColor("#DA3633")) // RED
            tvStatusMessage.setTextColor(Color.parseColor("#F85149"))
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN") // Bengali & English mixed
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                tvTerminalOutput.text = "Listening to you, Boss..."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val userQuery = matches[0]
                    tvTerminalOutput.text = "Boss: $userQuery\n\nJARVIS is thinking..."
                    processCommand(userQuery)
                }
            }

            override fun onError(error: Int) {
                tvTerminalOutput.text = "Yes Boss, voice input capture failed. Tap to try again."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun processCommand(command: String) {
        val lowerCmd = command.lowercase()

        // সরাসরি ফোন অটোমেশন চেক
        when {
            lowerCmd.contains("lock") || lowerCmd.contains("লক") -> {
                JarvisAccessibilityService.instance?.lockScreen()
                speakOut("Yes Boss, locking the device.")
                tvTerminalOutput.text = "JARVIS: Yes Boss, phone locked."
                return
            }
            lowerCmd.contains("home") || lowerCmd.contains("হোম") -> {
                JarvisAccessibilityService.instance?.goToHomeScreen()
                speakOut("Yes Boss, navigating to home screen.")
                tvTerminalOutput.text = "JARVIS: Navigated Home."
                return
            }
        }

        // Gemini AI-তে পাঠানো
        lifecycleScope.launch {
            val response = geminiManager.askJarvis(command)
            tvTerminalOutput.text = "JARVIS:\n$response"
            speakOut(response)
        }
    }

    private fun speakOut(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale("bn", "IN")
        }
    }

    private fun requestAppPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
        )
        ActivityCompat.requestPermissions(this, permissions, 200)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}
