package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.DeviceTelemetry
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAlert
import com.example.ui.theme.StarkGold
import com.example.ui.theme.StarkSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisCoreStatus

@Composable
fun JarvisTopBar(
    status: JarvisCoreStatus,
    telemetry: DeviceTelemetry,
    isVoiceMuted: Boolean,
    onToggleMute: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title and Mark
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator pulse dot
                val dotColor = when (status) {
                    JarvisCoreStatus.ONLINE_IDLE -> StarkSuccess
                    JarvisCoreStatus.LISTENING -> StarkGold
                    JarvisCoreStatus.THINKING -> ArcCyan
                    JarvisCoreStatus.SPEAKING -> ArcCyan
                    JarvisCoreStatus.EXECUTING_PROTOCOL -> StarkAlert
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.dp, dotColor.copy(alpha = 0.5f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "J.A.R.V.I.S.",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "CORE • MARK LXXXV • GEMINI FLASH",
                color = ArcCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Telemetry pill and control buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Battery & Network telemetry badge
            Box(
                modifier = Modifier
                    .background(ObsidianCard, RoundedCornerShape(12.dp))
                    .border(1.dp, ObsidianCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Network",
                        tint = if (telemetry.isConnected) ArcCyan else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        color = if (telemetry.isCharging) StarkSuccess else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Audio Mute/Unmute Toggle
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier.testTag("voice_mute_button")
            ) {
                Icon(
                    imageVector = if (isVoiceMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = if (isVoiceMuted) "Unmute Jarvis" else "Mute Jarvis",
                    tint = if (isVoiceMuted) TextSecondary else ArcCyan
                )
            }

            // Clear Conversation Memory
            IconButton(
                onClick = onClearHistory,
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear History",
                    tint = TextSecondary
                )
            }
        }
    }
}
