package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisTab

data class NavItemData(
    val tab: JarvisTab,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun JarvisBottomNav(
    activeTab: JarvisTab,
    onTabSelected: (JarvisTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemData(JarvisTab.HUD, "CORE HUD", Icons.Default.GraphicEq, "nav_hud"),
        NavItemData(JarvisTab.TERMINAL, "DIRECTIVES", Icons.Default.Chat, "nav_terminal"),
        NavItemData(JarvisTab.WEBHOOKS, "WEBHOOKS", Icons.Default.Sensors, "nav_webhooks"),
        NavItemData(JarvisTab.DIAGNOSTICS, "SYSTEM", Icons.Default.Speed, "nav_diagnostics")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .border(
                width = 1.dp,
                color = ObsidianCardBorder.copy(alpha = 0.6f)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val isSelected = activeTab == item.tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(item.tab) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) ArcCyan else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) ArcCyan else TextSecondary
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ArcCyanDark.copy(alpha = 0.2f),
                        selectedIconColor = ArcCyan,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = ArcCyan,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}
