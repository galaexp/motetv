package com.example.tvremote.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.MacroStep
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaPower
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMacroDialog(
    onSave: (name: String, desc: String, icon: String, steps: List<MacroStep>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val steps = remember {
        mutableStateListOf(
            MacroStep("WAKE_UP", 600L),
            MacroStep("HOME", 400L)
        )
    }

    val availableCommands = listOf(
        "SKIP_YOUTUBE_AD" to "⚡ Skip YouTube Ad",
        "WAKE_UP" to "Wake Up TV / Stick",
        "HOME" to "Go to Home Screen",
        "DPAD_CENTER" to "Press OK / Select",
        "VOLUME_UP" to "Volume Up",
        "VOLUME_DOWN" to "Volume Down",
        "VOLUME_MUTE" to "Mute Audio",
        "LAUNCH_PATCHWALL" to "Launch PatchWall (Xiaomi)",
        "SLEEP" to "Put TV to Sleep",
        "CHANNEL_UP" to "Channel Up",
        "CHANNEL_DOWN" to "Channel Down"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Custom Macro", color = NovaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = NovaTextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Macro Name", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Cinema Night") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaAccent,
                        unfocusedBorderColor = NovaBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_macro_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Wakes TV and starts video") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaAccent,
                        unfocusedBorderColor = NovaBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_macro_desc")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sequence (${steps.size} steps)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaAccent)
                    TextButton(
                        onClick = { steps.add(MacroStep("DPAD_CENTER", 400L)) },
                        modifier = Modifier.testTag("btn_add_macro_step")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = NovaAccent)
                        Text("+ Step", fontSize = 11.sp, color = NovaAccent)
                    }
                }

                LazyColumn(modifier = Modifier.height(180.dp)) {
                    itemsIndexed(steps) { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${index + 1}.", color = NovaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                            ) {
                                OutlinedTextField(
                                    value = availableCommands.firstOrNull { it.first == step.commandKey }?.second ?: step.commandKey,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NovaAccent,
                                        unfocusedBorderColor = NovaBorder,
                                        focusedTextColor = NovaTextPrimary,
                                        unfocusedTextColor = NovaTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(NovaSurfaceElevated)
                                ) {
                                    availableCommands.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, color = NovaTextPrimary, fontSize = 12.sp) },
                                            onClick = {
                                                steps[index] = step.copy(commandKey = key)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (steps.size > 1) {
                                IconButton(
                                    onClick = { steps.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Step", tint = NovaPower, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.ifBlank { "Custom Macro" }, desc.ifBlank { "User custom macro" }, "play", steps.toList())
                },
                enabled = steps.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = NovaAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_save_macro")
            ) {
                Text("Save Macro", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NovaTextMuted)
            }
        },
        containerColor = NovaSurface
    )
}
