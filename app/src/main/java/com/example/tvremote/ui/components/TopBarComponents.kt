package com.example.tvremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.ui.RemoteTab
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosGlassFill
import com.example.ui.theme.IosGlassUltra
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemIndigo
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun RemoteTopBar(
    currentTab: RemoteTab,
    connectionStatus: ConnectionStatus,
    onOpenDevices: () -> Unit,
    onPowerToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Floating Frosted Glass Capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0x14000000))
                    .clip(RoundedCornerShape(22.dp))
                    .background(IosGlassUltra)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Icon & Tab Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(IosSystemBlue.copy(alpha = 0.12f))
                                .border(1.dp, IosSystemBlue.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Nova Remote",
                                tint = IosSystemBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "NOVA REMOTE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = when (currentTab) {
                                    RemoteTab.REMOTE -> "TV Remote"
                                    RemoteTab.TOUCHPAD -> "Gesture Trackpad"
                                    RemoteTab.KEYPAD -> "Numeric Tuner"
                                    RemoteTab.APPS_MACROS -> "Apps & Macros"
                                    RemoteTab.SETTINGS -> "Settings & Devices"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = IosSystemBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Interactive Connection Status Pill
                    DeviceStatusCapsule(
                        status = connectionStatus,
                        onClick = onOpenDevices
                    )

                    // Minimal Right Action: Power Standby Toggle
                    IconButton(
                        onClick = onPowerToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IosSystemRed.copy(alpha = 0.12f))
                            .border(1.dp, IosSystemRed.copy(alpha = 0.25f), CircleShape)
                            .testTag("action_power_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power Standby",
                            tint = IosSystemRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceStatusCapsule(
    status: ConnectionStatus,
    onClick: () -> Unit
) {
    val isConnected = status is ConnectionStatus.Connected
    val isConnecting = status is ConnectionStatus.Connecting || status is ConnectionStatus.PairingRequired

    val dotColor = when {
        isConnected -> IosSystemGreen
        isConnecting -> IosSystemOrange
        else -> IosTextTertiary
    }

    // Gentle pulse animation for active connection
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val labelText = when (status) {
        is ConnectionStatus.Connected -> "${status.device.name} • ${status.latencyMs}ms"
        is ConnectionStatus.Connecting -> "Connecting..."
        is ConnectionStatus.PairingRequired -> "Pairing..."
        is ConnectionStatus.Scanning -> "Searching..."
        is ConnectionStatus.Error -> "Offline"
        ConnectionStatus.Disconnected -> "No TV"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(IosGlassCard)
            .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .testTag("connection_status_capsule"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .scale(if (isConnected) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = labelText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = IosTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
