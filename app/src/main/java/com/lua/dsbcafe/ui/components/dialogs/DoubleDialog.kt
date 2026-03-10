package com.lua.dsbcafe.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lua.dsbcafe.data.model.Person
import kotlinx.coroutines.delay

@Composable
fun DoubleDialog(
    person: Person,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var progress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        val totalMs = 10_000L
        val stepMs = 50L
        val steps = totalMs / stepMs
        repeat(steps.toInt()) {
            delay(stepMs)
            progress = 1f - (it + 1).toFloat() / steps
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make it a double?") },
        text = {
            androidx.compose.foundation.layout.Column {
                Text("Add another coffee for ${person.name}?")
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Make it a double!") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("No thanks") }
        },
    )
}
