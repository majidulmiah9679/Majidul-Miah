package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class WebhookResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val responseBody: String,
    val latencyMs: Long,
    val errorMessage: String? = null
)

class WebhookDispatcher {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun executeWebhook(
        url: String,
        method: String = "POST",
        jsonPayload: String = "{}"
    ): WebhookResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

            val requestBuilder = Request.Builder().url(validUrl)

            if (method.equals("POST", ignoreCase = true)) {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonPayload.toRequestBody(mediaType)
                requestBuilder.post(body)
            } else {
                requestBuilder.get()
            }

            requestBuilder.header("User-Agent", "JARVIS-Assistant-Engine/1.0")
            requestBuilder.header("Content-Type", "application/json")

            client.newCall(requestBuilder.build()).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyString = response.body?.string() ?: ""
                WebhookResult(
                    isSuccess = response.isSuccessful,
                    statusCode = response.code,
                    responseBody = bodyString,
                    latencyMs = latency,
                    errorMessage = if (!response.isSuccessful) "HTTP ${response.code}: ${response.message}" else null
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            WebhookResult(
                isSuccess = false,
                statusCode = 0,
                responseBody = "",
                latencyMs = latency,
                errorMessage = e.localizedMessage ?: e.javaClass.simpleName
            )
        }
    }
}
