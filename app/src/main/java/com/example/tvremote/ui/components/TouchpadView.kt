package com.example.tvremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun TouchpadView(
    onCommand: (RemoteCommand) -> Unit,
    onTouchMove: (dx: Float, dy: Float) -> Unit,
    onTouchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var sensitivity by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // iOS Frosted Instruction Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0C000000))
                .clip(RoundedCornerShape(16.dp))
                .background(IosGlassElevated)
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = null,
                    tint = IosSystemBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Swipe to steer cursor • Tap to click • Pinch to zoom",
                    fontSize = 12.sp,
                    color = IosTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Apple Magic Trackpad Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x14000000))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.2.dp, IosGlassBorderHighlight, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTouchClick() },
                        onDoubleTap = {
                            onTouchClick()
                            onTouchClick()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> touchPos = offset },
                        onDragEnd = { touchPos = null },
                        onDragCancel = { touchPos = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            touchPos = change.position
                            onTouchMove(dragAmount.x * sensitivity, dragAmount.y * sensitivity)
                        }
                    )
                }
                .testTag("touchpad_surface"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle watermark
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MAGIC GLASS TRACKPAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = IosTextMuted.copy(alpha = 0.5f)
                )
            }

            // Finger touch ripple indicator
            touchPos?.let { pos ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .padding(start = pos.x.dp / 2.5f, top = pos.y.dp / 2.5f)
                        .clip(CircleShape)
                        .background(IosSystemBlue.copy(alpha = 0.2f))
                        .border(1.dp, IosSystemBlue.copy(alpha = 0.5f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sensitivity Slider Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IosGlassCard)
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SENSITIVITY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = IosSystemBlue,
                        activeTrackColor = IosSystemBlue,
                        inactiveTrackColor = IosSystemGray5
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .testTag("slider_sensitivity")
                )
                Text(
                    text = "${"%.1f".format(sensitivity)}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosSystemBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Navigation Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onCommand(RemoteCommand.Back) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IosTextPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(14.dp))
                    .testTag("touchpad_btn_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Back", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onTouchClick,
                colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("touchpad_btn_click")
            ) {
                Text("Click / Select", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onCommand(RemoteCommand.Home) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IosTextPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(14.dp))
                    .testTag("touchpad_btn_home")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Home", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
