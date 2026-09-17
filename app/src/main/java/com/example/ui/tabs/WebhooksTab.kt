package com.example.ui.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WebhookLog
import com.example.network.WebhookResult
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAlert
import com.example.ui.theme.StarkGold
import com.example.ui.theme.StarkSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WebhooksTab(
    viewModel: JarvisViewModel,
    logs: List<WebhookLog>,
    activeResult: WebhookResult?,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    var urlText by remember { mutableStateOf("https://httpbin.org/post") }
    var method by remember { mutableStateOf("POST") }
    var payloadText by remember {
        mutableStateOf("""{"assistant":"JARVIS","event":"SECURITY_SYNC","status":"OPERATIONAL"}""")
    }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val presets = listOf(
        Triple("HTTPBin POST", "https://httpbin.org/post", "POST"),
        Triple("HTTPBin GET", "https://httpbin.org/get", "GET"),
        Triple("Home Relay", "https://httpbin.org/post", "POST"),
        Triple("Custom Trigger", "", "POST")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WEBHOOK & API DISPATCH ARRAY",
                        color = ArcCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Powered by OkHttp Client Engine",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(ObsidianCard, RoundedCornerShape(8.dp))
                        .border(1.dp, ArcCyanDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OKHTTP 4.12",
                        color = ArcCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Preset Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEachIndexed { index, (name, pUrl, pMethod) ->
                    val isSelected = selectedPresetIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPresetIndex = index
                            if (pUrl.isNotBlank()) urlText = pUrl
                            method = pMethod
                            if (pMethod == "GET") {
                                payloadText = ""
                            } else if (payloadText.isBlank()) {
                                payloadText = """{"assistant":"JARVIS","event":"ALERT"}"""
                            }
                        },
                        label = {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArcCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ObsidianCardBorder,
                            selectedBorderColor = ArcCyan
                        )
                    )
                }
            }
        }

        // Configuration Form Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Method & URL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Method Toggle
                        Button(
                            onClick = {
                                method = if (method == "POST") "GET" else "POST"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (method == "POST") StarkGold else ArcCyanDark,
                                contentColor = Color(0xFF070B14)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = method,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Target URL
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("webhook_url_input"),
                            label = { Text("Target Endpoint URL", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = ObsidianCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // JSON Payload (if POST)
                    if (method == "POST") {
                        OutlinedTextField(
                            value = payloadText,
                            onValueChange = { payloadText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("webhook_payload_input"),
                            label = { Text("JSON Payload Body", fontSize = 11.sp) },
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = ObsidianCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Trigger Execution Button
                    Button(
                        onClick = {
                            if (urlText.isNotBlank()) {
                                viewModel.executeWebhookTrigger(urlText, method, payloadText)
                            }
                        },
                        enabled = !isRunning && urlText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dispatch_webhook_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArcCyan,
                            contentColor = Color(0xFF070B14)
                        )
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF070B14),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRANSMITTING OVER OKHTTP...",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Transmit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRANSMIT WEBHOOK VIA OKHTTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active Result Inspector
        if (activeResult != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(
                            1.dp,
                            if (activeResult.isSuccess) StarkSuccess else StarkAlert,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeResult.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = "Status",
                                    tint = if (activeResult.isSuccess) StarkSuccess else StarkAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeResult.isSuccess) "HTTP ${activeResult.statusCode} OK" else "ERROR ${activeResult.statusCode}",
                                    color = if (activeResult.isSuccess) StarkSuccess else StarkAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Latency",
                                    tint = StarkGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeResult.latencyMs} ms",
                                    color = StarkGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (activeResult.errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeResult.errorMessage,
                                color = StarkAlert,
                                fontSize = 12.sp
                            )
                        }

                        if (activeResult.responseBody.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SERVER RESPONSE PAYLOAD",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Response", activeResult.responseBody))
                                        Toast.makeText(context, "Payload copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF070B14))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = activeResult.responseBody.take(600),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Historic Webhook Logs Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISPATCH AUDIT LOG (${logs.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Historic Webhook Logs List
        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No webhook transmissions recorded yet.",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                WebhookLogItem(log = log)
            }
        }
    }
}

@Composable
fun WebhookLogItem(log: WebhookLog) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(log.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (log.method == "POST") StarkGold.copy(alpha = 0.2f) else ArcCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.method,
                            color = if (log.method == "POST") StarkGold else ArcCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HTTP ${log.statusCode}",
                        color = if (log.isSuccess) StarkSuccess else StarkAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${log.latencyMs}ms",
                        color = StarkGold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.url,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
