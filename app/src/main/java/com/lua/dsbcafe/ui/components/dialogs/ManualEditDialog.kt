package com.lua.dsbcafe.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lua.dsbcafe.data.model.Person

@Composable
fun manualEditDialog(
    persons: List<Person>,
    isExpertMode: Boolean,
    onToggleExpertMode: () -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Manual Edit")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Expert",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Switch(
                        checked = isExpertMode,
                        onCheckedChange = { onToggleExpertMode() },
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) {
                items(persons, key = { it.badgeId }) { person ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp,
                                    ),
                            ) {
                                val errorContainer =
                                    MaterialTheme.colorScheme.errorContainer
                                val onErrorContainer =
                                    MaterialTheme.colorScheme.onErrorContainer
                                val primaryContainer =
                                    MaterialTheme.colorScheme.primaryContainer
                                val onPrimaryContainer =
                                    MaterialTheme.colorScheme.onPrimaryContainer

                                val errColors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = errorContainer,
                                        contentColor = onErrorContainer,
                                    )
                                val primColors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = primaryContainer,
                                        contentColor = onPrimaryContainer,
                                    )
                                if (isExpertMode) {
                                    IconButton(
                                        onClick = {
                                            onDecrement(
                                                person.badgeId,
                                            )
                                        },
                                        colors = errColors,
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrement",
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }

                                Text(
                                    text = person.coffeeCount.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 4.dp,
                                        ),
                                )

                                IconButton(
                                    onClick = { onIncrement(person.badgeId) },
                                    colors = primColors,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increment",
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}
