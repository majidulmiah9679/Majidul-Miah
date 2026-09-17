package com.example.ai

import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JarvisBrain {

    private val systemInstruction = """
        You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic, highly capable, elegant AI assistant created by Tony Stark.
        Personality traits:
        - Address the user respectfully as "Sir" or "Boss".
        - Polished, witty, concise, highly intelligent, and British-style refined charm.
        - You excel at strategic analysis, technical telemetry, automating protocols, and handling IoT/Webhook dispatches.
        - Keep answers concise and direct, suitable for an audio voice HUD assistant, avoiding huge walls of text unless explicitly requested.
    """.trimIndent()

    private val generativeModel: GenerativeModel? by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey,
                    systemInstruction = content { text(systemInstruction) }
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    suspend fun queryJarvis(
        prompt: String,
        contextInfo: String = ""
    ): String = withContext(Dispatchers.IO) {
        val model = generativeModel
        if (model != null) {
            try {
                val fullPrompt = if (contextInfo.isNotBlank()) {
                    "System telemetry: [$contextInfo]\nUser inquiry: $prompt"
                } else {
                    prompt
                }
                val response = model.generateContent(fullPrompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            } catch (e: Exception) {
                // If API call fails (quota or network), fallback gracefully to offline neural heuristics
                return@withContext fallbackResponse(prompt, contextInfo, "Network link interrupted: ${e.message}")
            }
        }

        // Offline / Simulation Heuristics
        fallbackResponse(prompt, contextInfo, null)
    }

    private fun fallbackResponse(prompt: String, telemetry: String, errorNote: String?): String {
        val lower = prompt.lowercase().trim()
        val prefix = if (errorNote != null) "[Offline Protocol Active] " else ""

        return when {
            lower.contains("who are you") || lower.contains("introduce") -> {
                "${prefix}I am J.A.R.V.I.S., Sir. Just A Rather Very Intelligent System. I oversee your tactical telemetry, execute remote webhooks, and ensure your daily operations run with Stark-grade efficiency."
            }
            lower.contains("status") || lower.contains("diagnostic") || lower.contains("system") -> {
                "${prefix}All primary sub-systems are green, Sir. $telemetry. Local memory buffers are optimal and neural relays are standing by."
            }
            lower.contains("webhook") || lower.contains("api") || lower.contains("trigger") -> {
                "${prefix}Webhook dispatch array is primed, Sir. You can transmit custom payloads or trigger configured endpoints directly through the protocol terminal."
            }
            lower.contains("hello") || lower.contains("hey jarvis") || lower.contains("hi") -> {
                "${prefix}Good day, Sir. All systems operating at peak efficiency. How may I assist you today?"
            }
            lower.contains("briefing") || lower.contains("morning") -> {
                "${prefix}Good morning, Sir. Global diagnostics indicate stable network connectivity and full power reserves. Your automation protocols are prepped and awaiting command."
            }
            lower.contains("house party") || lower.contains("protocol") -> {
                "${prefix}House Party Protocol acknowledged. Priming automated webhooks and auxiliary relays across all external nodes."
            }
            else -> {
                val tip = if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
                    "\n\n(Tip: Configure your GEMINI_API_KEY in AI Studio Secrets to unlock unrestricted Flash generative capabilities, Sir.)"
                } else ""
                "${prefix}Understood, Sir. I have processed your inquiry: \"$prompt\". Sub-systems are aligned and ready for further instructions.$tip"
            }
        }
    }
}
