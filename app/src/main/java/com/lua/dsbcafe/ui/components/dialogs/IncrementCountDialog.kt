package com.lua.dsbcafe.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.lua.dsbcafe.data.model.Person

@Composable
fun IncrementCountDialog(
    persons: List<Person>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a coffee") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                items(persons, key = { it.badgeId }) { person ->
                    ListItem(
                        headlineContent = { Text(person.name) },
                        leadingContent = {
                            RadioButton(
                                selected = selected == person.name,
                                onClick = null,
                            )
                        },
                        modifier = Modifier.selectable(
                            selected = selected == person.name,
                            role = Role.RadioButton,
                            onClick = { selected = person.name },
                        ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null,
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
