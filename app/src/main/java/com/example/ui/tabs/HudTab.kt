package com.example.ui.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArcReactor
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAlert
import com.example.ui.theme.StarkGold
import com.example.ui.theme.StarkSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.JarvisCoreStatus
import com.example.viewmodel.JarvisViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudTab(
    viewModel: JarvisViewModel,
    status: JarvisCoreStatus,
    lastReply: String,
    onStartVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Central Holographic Arc Reactor
        Spacer(modifier = Modifier.height(8.dp))
        ArcReactor(
            status = status,
            size = 210.dp,
            onClick = onStartVoice
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status description badge
        val statusText = when (status) {
            JarvisCoreStatus.ONLINE_IDLE -> "SYSTEM READY • TAP REACTOR TO SPEAK"
            JarvisCoreStatus.LISTENING -> "LISTENING TO INCOMING AUDIO STREAM..."
            JarvisCoreStatus.THINKING -> "PROCESSING QUERY VIA GEMINI FLASH..."
            JarvisCoreStatus.SPEAKING -> "TRANSMITTING VOCAL SYNTHESIS..."
            JarvisCoreStatus.EXECUTING_PROTOCOL -> "EXECUTING EXTERNAL PROTOCOL / WEBHOOK..."
        }
        val statusColor = when (status) {
            JarvisCoreStatus.ONLINE_IDLE -> ArcCyan
            JarvisCoreStatus.LISTENING -> StarkGold
            JarvisCoreStatus.THINKING -> ArcCyanLight
            JarvisCoreStatus.SPEAKING -> ArcCyan
            JarvisCoreStatus.EXECUTING_PROTOCOL -> StarkAlert
        }

        Box(
            modifier = Modifier
                .background(ObsidianCard, RoundedCornerShape(20.dp))
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Voice waveform animation bars
        if (status == JarvisCoreStatus.SPEAKING || status == JarvisCoreStatus.LISTENING) {
            Spacer(modifier = Modifier.height(10.dp))
            WaveformVisualizer(activeColor = statusColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Primary Voice Trigger Button
        Button(
            onClick = onStartVoice,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("voice_command_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (status == JarvisCoreStatus.LISTENING) StarkAlert else ArcCyan,
                contentColor = Color(0xFF070B14)
            )
        ) {
            Icon(
                imageVector = if (status == JarvisCoreStatus.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Input",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (status == JarvisCoreStatus.LISTENING) "STOP LISTENING" else "INITIALIZE VOICE INPUT",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Jarvis Assistant Intelligence Output Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCard)
                .border(1.dp, ObsidianCardBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ArcCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INTELLIGENCE BRIEF",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                viewModel.voiceEngine.speak(lastReply)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("replay_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Replay Speech",
                                tint = ArcCyanLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Jarvis", lastReply)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to tactical clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("copy_reply_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lastReply,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Quick Protocol Triggers Section
        Text(
            text = "TACTICAL PROTOCOL PRESETS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProtocolChip(
                label = "Diagnostic Scan",
                icon = Icons.Default.Speed,
                onClick = { viewModel.runDiagnosticScan() }
            )
            ProtocolChip(
                label = "Morning Briefing",
                icon = Icons.Default.WbSunny,
                onClick = { viewModel.runMorningBriefing() }
            )
            ProtocolChip(
                label = "House Party",
                icon = Icons.Default.Security,
                onClick = { viewModel.runHousePartyProtocol() }
            )
            ProtocolChip(
                label = "Send Webhook Alert",
                icon = Icons.Default.Send,
                onClick = {
                    val url = "https://httpbin.org/post"
                    val payload = """{"event":"JARVIS_SECURITY_ALERT","status":"NOMINAL","source":"STARK_SUIT_MK85"}"""
                    viewModel.executeWebhookTrigger(url, "POST", payload)
                }
            )
            ProtocolChip(
                label = "Who Are You?",
                icon = Icons.Default.Refresh,
                onClick = { viewModel.sendPrompt("Who are you, Jarvis?") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProtocolChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ArcCyan,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = ObsidianCard
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = ObsidianCardBorder
        ),
        modifier = modifier.testTag("protocol_chip_${label.lowercase().replace(" ", "_")}")
    )
}

@Composable
fun WaveformVisualizer(activeColor: Color) {
    val transition = rememberInfiniteTransition(label = "waveform")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 12
        for (i in 0 until barCount) {
            val duration = 300 + (i * 45)
            val heightAnim by transition.animateFloat(
                initialValue = 4f,
                targetValue = 24f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightAnim.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(activeColor.copy(alpha = 0.85f))
            )
        }
    }
}
