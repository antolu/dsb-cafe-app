package com.lua.dsbcafe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun AdminMenu(
    isExpanded: Boolean,
    isExpertMode: Boolean,
    onToggle: () -> Unit,
    onToggleExpertMode: () -> Unit,
    onReset: () -> Unit,
    onEmail: () -> Unit,
    onDelete: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                AdminAction(label = "Delete user", onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete user")
                }
                AdminAction(
                    label = if (isExpertMode) "Expert mode: ON" else "Expert mode: OFF",
                    onClick = onToggleExpertMode
                ) {
                    Icon(
                        imageVector = if (isExpertMode) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle expert mode"
                    )
                }
                AdminAction(label = "Manual edit", onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Manual edit")
                }
                AdminAction(label = "Send statistics", onClick = onEmail) {
                    Icon(Icons.Default.Email, contentDescription = "Send statistics email")
                }
                AdminAction(label = "Reset counts", onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset counts")
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(56.dp)
                .zIndex(1f),
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = if (isExpanded) "Close admin menu" else "Open admin menu",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun AdminAction(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            icon()
        }
    }
}
