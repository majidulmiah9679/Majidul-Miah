package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.StarkAlert
import com.example.ui.theme.StarkGold
import com.example.viewmodel.JarvisCoreStatus
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactor(
    status: JarvisCoreStatus,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_anim")

    // Dynamic rotation speeds based on Jarvis activity
    val rotationSpeed = when (status) {
        JarvisCoreStatus.THINKING -> 3000
        JarvisCoreStatus.EXECUTING_PROTOCOL -> 2500
        JarvisCoreStatus.LISTENING -> 4000
        JarvisCoreStatus.SPEAKING -> 5000
        JarvisCoreStatus.ONLINE_IDLE -> 12000
    }

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (rotationSpeed * 1.5).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Pulse animation for core breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (status == JarvisCoreStatus.LISTENING || status == JarvisCoreStatus.SPEAKING) 600 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = when (status) {
        JarvisCoreStatus.EXECUTING_PROTOCOL -> StarkGold
        JarvisCoreStatus.LISTENING -> StarkGold
        JarvisCoreStatus.SPEAKING -> ArcCyanLight
        JarvisCoreStatus.THINKING -> ArcCyanDark
        JarvisCoreStatus.ONLINE_IDLE -> ArcCyan
    }

    val secondaryColor = when (status) {
        JarvisCoreStatus.EXECUTING_PROTOCOL -> StarkAlert
        JarvisCoreStatus.LISTENING -> ArcCyan
        else -> ArcCyanDark
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_core")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2 - 8.dp.toPx()

            // 1. Ambient Radial Glow Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f * pulseScale),
                        primaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f,
                center = center
            )

            // 2. Outermost Static Reference Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Rotating Outer Segmented Arcs
            val outerArcRadius = radius * 0.92f
            for (i in 0 until 4) {
                val startAngle = rotationAngle + (i * 90f) + 10f
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerArcRadius, center.y - outerArcRadius),
                    size = Size(outerArcRadius * 2, outerArcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Counter-rotating Radial Tick Nodes (12 energy nodes)
            val tickRadiusInner = radius * 0.74f
            val tickRadiusOuter = radius * 0.84f
            for (i in 0 until 12) {
                val angleDeg = counterRotation + (i * 30f)
                val angleRad = angleDeg * (PI.toFloat() / 180f)
                val start = Offset(
                    center.x + tickRadiusInner * cos(angleRad),
                    center.y + tickRadiusInner * sin(angleRad)
                )
                val end = Offset(
                    center.x + tickRadiusOuter * cos(angleRad),
                    center.y + tickRadiusOuter * sin(angleRad)
                )
                val isAccent = i % 3 == 0
                drawLine(
                    color = if (isAccent) secondaryColor else primaryColor.copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = if (isAccent) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 5. Middle Energy Ring
            val middleRadius = radius * 0.65f
            drawCircle(
                color = secondaryColor.copy(alpha = 0.4f),
                radius = middleRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 6. Inner Fast Rotating Segment Ring (3 Arc segments)
            val innerArcRadius = radius * 0.54f
            for (i in 0 until 3) {
                val startAngle = (rotationAngle * 1.8f) + (i * 120f)
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerArcRadius, center.y - innerArcRadius),
                    size = Size(innerArcRadius * 2, innerArcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 7. Core Reactor Energy Center with Pulsing Glow
            val coreRadius = (radius * 0.36f) * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        primaryColor.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 8. Core Center Triangular Reactor Lattice
            val triRadius = coreRadius * 0.65f
            for (i in 0 until 3) {
                val a1 = counterRotation + (i * 120f)
                val a2 = counterRotation + ((i + 1) * 120f)
                val r1 = a1 * (PI.toFloat() / 180f)
                val r2 = a2 * (PI.toFloat() / 180f)
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(center.x + triRadius * cos(r1), center.y + triRadius * sin(r1)),
                    end = Offset(center.x + triRadius * cos(r2), center.y + triRadius * sin(r2)),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
