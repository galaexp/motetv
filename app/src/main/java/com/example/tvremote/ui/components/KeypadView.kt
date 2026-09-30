package com.example.tvremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.RemoteCommand
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun KeypadView(
    onCommand: (RemoteCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var channelBuffer by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // iOS Frosted Tuner Window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0x10000000))
                .clip(RoundedCornerShape(18.dp))
                .background(IosGlassElevated)
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TUNER CHANNEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextTertiary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (channelBuffer.isEmpty()) "— — —" else channelBuffer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (channelBuffer.isEmpty()) IosTextMuted else IosSystemBlue,
                        letterSpacing = 2.sp
                    )
                }

                if (channelBuffer.isNotEmpty()) {
                    Button(
                        onClick = {
                            onCommand(RemoteCommand.KeyEnter)
                            channelBuffer = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_tune_confirm")
                    ) {
                        Text("TUNE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Channel Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("CH 1", "CH 4", "CH 7", "CH 12").forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IosGlassCard)
                        .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(12.dp))
                        .clickable {
                            val digits = preset.replace("CH ", "")
                            channelBuffer = digits
                            digits.forEach { d ->
                                d.digitToIntOrNull()?.let { onCommand(RemoteCommand.Number(it)) }
                            }
                            onCommand(RemoteCommand.KeyEnter)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(preset, fontSize = 12.sp, color = IosTextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3x4 iOS Telephone Glass Dialer Grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("CLR", "0", "ENT")
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { keyLabel ->
                        DialKeyButton(
                            label = keyLabel,
                            onClick = {
                                when (keyLabel) {
                                    "CLR" -> {
                                        if (channelBuffer.isNotEmpty()) {
                                            channelBuffer = channelBuffer.dropLast(1)
                                        }
                                        onCommand(RemoteCommand.KeyClear)
                                    }
                                    "ENT" -> {
                                        onCommand(RemoteCommand.KeyEnter)
                                        channelBuffer = ""
                                    }
                                    else -> {
                                        val digit = keyLabel.toIntOrNull()
                                        if (digit != null) {
                                            channelBuffer = (channelBuffer + digit).take(4)
                                            onCommand(RemoteCommand.Number(digit))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Channel Steppers
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onCommand(RemoteCommand.ChannelDown) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = IosTextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
                    .testTag("keypad_btn_ch_down")
            ) {
                Text("CH −", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = { onCommand(RemoteCommand.ChannelUp) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = IosTextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
                    .testTag("keypad_btn_ch_up")
            ) {
                Text("CH +", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DialKeyButton(
    label: String,
    onClick: () -> Unit
) {
    val isAction = label == "CLR" || label == "ENT"
    val textColor = when (label) {
        "ENT" -> IosSystemBlue
        "CLR" -> IosSystemRed
        else -> IosTextPrimary
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(6.dp, CircleShape, spotColor = Color(0x10000000))
            .clip(CircleShape)
            .background(if (isAction) IosSystemGray5 else Color.White)
            .border(1.dp, IosGlassBorderHighlight, CircleShape)
            .clickable { onClick() }
            .testTag("dial_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (isAction) 15.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
