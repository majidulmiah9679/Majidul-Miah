package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisBrain
import com.example.audio.JarvisVoiceEngine
import com.example.data.ChatMessage
import com.example.data.JarvisDatabase
import com.example.data.JarvisRepository
import com.example.data.WebhookLog
import com.example.network.WebhookDispatcher
import com.example.network.WebhookResult
import com.example.system.DeviceTelemetry
import com.example.system.JarvisDiagnostics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JarvisCoreStatus {
    ONLINE_IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING_PROTOCOL
}

enum class JarvisTab {
    HUD,
    TERMINAL,
    WEBHOOKS,
    DIAGNOSTICS
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository
    val voiceEngine = JarvisVoiceEngine(application)
    private val brain = JarvisBrain()
    private val webhookDispatcher = WebhookDispatcher()

    init {
        val db = JarvisDatabase.getDatabase(application)
        repository = JarvisRepository(db.jarvisDao())
    }

    val chatMessages: StateFlow<List<ChatMessage>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webhookLogs: StateFlow<List<WebhookLog>> = repository.webhookLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _coreStatus = MutableStateFlow(JarvisCoreStatus.ONLINE_IDLE)
    val coreStatus: StateFlow<JarvisCoreStatus> = _coreStatus.asStateFlow()

    private val _telemetry = MutableStateFlow(JarvisDiagnostics.getTelemetry(application))
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    private val _activeTab = MutableStateFlow(JarvisTab.HUD)
    val activeTab: StateFlow<JarvisTab> = _activeTab.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _lastAssistantReply = MutableStateFlow(
        "Good day, Sir. J.A.R.V.I.S. core is operational. Gemini Flash neural relays and webhook dispatch array are ready."
    )
    val lastAssistantReply: StateFlow<String> = _lastAssistantReply.asStateFlow()

    private val _activeWebhookResult = MutableStateFlow<WebhookResult?>(null)
    val activeWebhookResult: StateFlow<WebhookResult?> = _activeWebhookResult.asStateFlow()

    private val _isWebhookRunning = MutableStateFlow(false)
    val isWebhookRunning: StateFlow<Boolean> = _isWebhookRunning.asStateFlow()

    init {
        // Collect voice engine speech state
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { speaking ->
                if (speaking && _coreStatus.value != JarvisCoreStatus.THINKING) {
                    _coreStatus.value = JarvisCoreStatus.SPEAKING
                } else if (!speaking && _coreStatus.value == JarvisCoreStatus.SPEAKING) {
                    _coreStatus.value = JarvisCoreStatus.ONLINE_IDLE
                }
            }
        }
        viewModelScope.launch {
            voiceEngine.isListening.collect { listening ->
                if (listening) {
                    _coreStatus.value = JarvisCoreStatus.LISTENING
                } else if (_coreStatus.value == JarvisCoreStatus.LISTENING) {
                    _coreStatus.value = JarvisCoreStatus.ONLINE_IDLE
                }
            }
        }

        refreshTelemetry()
    }

    fun setTab(tab: JarvisTab) {
        _activeTab.value = tab
    }

    fun toggleVoiceMute() {
        val newMute = !_isVoiceMuted.value
        _isVoiceMuted.value = newMute
        if (newMute) {
            voiceEngine.stopSpeaking()
        }
    }

    fun refreshTelemetry() {
        _telemetry.value = JarvisDiagnostics.getTelemetry(getApplication())
    }

    fun sendPrompt(userText: String, protocol: String = "GENERAL") {
        if (userText.isBlank()) return
        viewModelScope.launch {
            // Save user message
            repository.addMessage(
                ChatMessage(
                    sender = "USER",
                    text = userText.trim(),
                    protocol = protocol
                )
            )

            _coreStatus.value = JarvisCoreStatus.THINKING
            refreshTelemetry()

            // Query Gemini Flash via JarvisBrain
            val responseText = brain.queryJarvis(
                prompt = userText,
                contextInfo = _telemetry.value.summary()
            )

            _lastAssistantReply.value = responseText

            // Save assistant message
            repository.addMessage(
                ChatMessage(
                    sender = "JARVIS",
                    text = responseText,
                    protocol = protocol
                )
            )

            // Speak out if not muted
            if (!_isVoiceMuted.value) {
                _coreStatus.value = JarvisCoreStatus.SPEAKING
                voiceEngine.speak(responseText)
            } else {
                _coreStatus.value = JarvisCoreStatus.ONLINE_IDLE
            }
        }
    }

    fun executeWebhookTrigger(
        url: String,
        method: String = "POST",
        payload: String = "{}"
    ) {
        viewModelScope.launch {
            _isWebhookRunning.value = true
            _coreStatus.value = JarvisCoreStatus.EXECUTING_PROTOCOL

            val result = webhookDispatcher.executeWebhook(url, method, payload)
            _activeWebhookResult.value = result

            // Record to Room Database
            repository.logWebhook(
                WebhookLog(
                    url = url,
                    method = method,
                    payload = payload,
                    statusCode = result.statusCode,
                    responseBody = result.responseBody.take(1000),
                    latencyMs = result.latencyMs,
                    isSuccess = result.isSuccess
                )
            )

            val statusSummary = if (result.isSuccess) {
                "Webhook dispatch successful to $url. HTTP ${result.statusCode} received in ${result.latencyMs}ms."
            } else {
                "Webhook dispatch failure to $url: ${result.errorMessage ?: "Unknown error"}."
            }

            _lastAssistantReply.value = statusSummary

            repository.addMessage(
                ChatMessage(
                    sender = "JARVIS",
                    text = statusSummary,
                    protocol = "WEBHOOK",
                    webhookInfo = "HTTP ${result.statusCode} (${result.latencyMs}ms)"
                )
            )

            if (!_isVoiceMuted.value) {
                voiceEngine.speak(statusSummary)
            } else {
                _coreStatus.value = JarvisCoreStatus.ONLINE_IDLE
            }

            _isWebhookRunning.value = false
        }
    }

    fun runMorningBriefing() {
        viewModelScope.launch {
            refreshTelemetry()
            val prompt = "Provide a classic Tony Stark morning briefing. Address me as Sir. Factor in battery at ${_telemetry.value.batteryPercent}%, network ${_telemetry.value.networkType}, and confirm all sub-systems are online and ready."
            sendPrompt(prompt, protocol = "BRIEFING")
        }
    }

    fun runDiagnosticScan() {
        viewModelScope.launch {
            _coreStatus.value = JarvisCoreStatus.EXECUTING_PROTOCOL
            delay(600)
            refreshTelemetry()
            val t = _telemetry.value
            val scanReport = "Tactical Diagnostic Complete, Sir.\n• Power Core: ${t.batteryPercent}% (${if (t.isCharging) "Charging" else "Discharging"})\n• Comms Array: ${t.networkType}\n• Available Memory: ${t.availableRamMb} MB / ${t.totalRamMb} MB\n• Terminal Unit: ${t.deviceModel} (OS ${t.androidVersion})\nAll sub-systems calibrated."
            
            _lastAssistantReply.value = scanReport
            repository.addMessage(
                ChatMessage(
                    sender = "JARVIS",
                    text = scanReport,
                    protocol = "DIAGNOSTIC"
                )
            )

            if (!_isVoiceMuted.value) {
                voiceEngine.speak("Diagnostic scan complete, Sir. All primary sub-systems are operating within optimal parameters.")
            } else {
                _coreStatus.value = JarvisCoreStatus.ONLINE_IDLE
            }
        }
    }

    fun runHousePartyProtocol() {
        viewModelScope.launch {
            val url = "https://httpbin.org/post"
            val payload = """{"protocol":"HOUSE_PARTY_PROTOCOL","source":"JARVIS_CORE","level":"ALPHA_ALERT","timestamp":${System.currentTimeMillis()}}"""
            executeWebhookTrigger(url, "POST", payload)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearMessages()
            repository.clearLogs()
            _lastAssistantReply.value = "Memory arrays purged, Sir. Ready for new operations."
            if (!_isVoiceMuted.value) {
                voiceEngine.speak("Memory arrays purged, Sir.")
            }
        }
    }

    fun startVoiceInput(onError: (String) -> Unit) {
        voiceEngine.startListening(
            onResult = { spokenText ->
                sendPrompt(spokenText, protocol = "VOICE")
            },
            onError = onError
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
