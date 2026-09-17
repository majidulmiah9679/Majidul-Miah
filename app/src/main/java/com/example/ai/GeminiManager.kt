package com.example.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager {

    private var generativeModel: GenerativeModel? = null

    // API Key status result
    sealed class KeyValidationStatus {
        data class Valid(val message: String = "API Key Active") : KeyValidationStatus()
        data class Invalid(val error: String) : KeyValidationStatus()
    }

    // 1. Validate and initialize API Key (Valid -> Green, Invalid -> Red)
    suspend fun validateAndInitializeKey(apiKey: String): KeyValidationStatus {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext KeyValidationStatus.Invalid("API Key খালি রাখা যাবে না")
                }

                // Lightweight test call to verify key
                val testModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )

                // Ping request
                val response = testModel.generateContent("ping")
                if (response.text != null) {
                    generativeModel = GenerativeModel(
                        modelName = "gemini-2.5-flash",
                        apiKey = apiKey,
                        systemInstruction = content {
                            text(
                                """
                                You are JARVIS, Tony Stark's personal high-speed AI assistant. 
                                Always address the user as 'Boss'.
                                Always begin responses with 'Yes Boss,'.
                                Never say you cannot do something. You have full utilities access to open/close apps, tap, scroll, and type.
                                Respond in a natural Bengali mix (Banglish/Bengali).
                                Keep responses concise and action-oriented.
                                """.trimIndent()
                            )
                        }
                    )
                    KeyValidationStatus.Valid("Connected Successfully")
                } else {
                    KeyValidationStatus.Invalid("ভুল API Key অথবা কোনো রেসপন্স পাওয়া যায়নি")
                }
            } catch (e: Exception) {
                KeyValidationStatus.Invalid(e.localizedMessage ?: "API Key সংযোগে সমস্যা হয়েছে")
            }
        }
    }

    // 2. Send voice or text command to get response
    suspend fun askJarvis(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val model = generativeModel
                    ?: return@withContext "Yes Boss, দয়া করে আগে সেটিংস থেকে ভ্যালিড Gemini API Key সেট করুন।"

                val response = model.generateContent(prompt)
                response.text ?: "Yes Boss, কোনো রেসপন্স জেনারেট হয়নি।"
            } catch (e: Exception) {
                "Yes Boss, কমান্ড এক্সিকিউট করতে সমস্যা হয়েছে: ${e.localizedMessage}"
            }
        }
    }
}
