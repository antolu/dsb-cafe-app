package com.lua.dsbcafe.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lua.dsbcafe.ui.components.AdminMenu
import com.lua.dsbcafe.ui.components.PersonItem
import com.lua.dsbcafe.ui.components.dialogs.DeleteUserDialog
import com.lua.dsbcafe.ui.components.dialogs.DoubleDialog
import com.lua.dsbcafe.ui.components.dialogs.NameInputDialog
import com.lua.dsbcafe.viewmodel.DialogState
import com.lua.dsbcafe.viewmodel.MainViewModel
import com.lua.dsbcafe.viewmodel.UiMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val persons by viewModel.persons.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var adminExpanded by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(message) {
        message?.let {
            val text = when (it) {
                is UiMessage.Info -> it.text
                is UiMessage.Error -> it.text
            }
            snackbarHostState.showSnackbar(text)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(5_000)
            tapCount = 0
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DSB Café",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Coffee counter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Total count — long-press to unlock admin menu
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 24.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    tapCount++
                                    if (tapCount >= 5) {
                                        adminExpanded = !adminExpanded
                                        tapCount = 0
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = totalCount.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = "Coffee addiction breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                HorizontalDivider()

                if (isLoading && persons.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (persons.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No coffees yet. Tap your badge!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = persons,
                            key = { _, person -> person.badgeId },
                        ) { index, person ->
                            PersonItem(person = person, rank = index + 1)
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Admin speed-dial anchored to bottom-end
            if (adminExpanded) {
                AdminMenu(
                    isExpanded = adminExpanded,
                    onToggle = { adminExpanded = !adminExpanded },
                    onReset = {
                        adminExpanded = false
                        viewModel.resetCounts()
                    },
                    onEmail = {
                        adminExpanded = false
                        viewModel.sendStatisticsEmail(context)
                    },
                    onDelete = {
                        adminExpanded = false
                        viewModel.showDeleteUserDialog()
                    },
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }

    when (val state = dialogState) {
        is DialogState.DoubleShot -> DoubleDialog(
            person = state.person,
            onConfirm = { viewModel.confirmDouble(state.badgeId, state.person) },
            onDismiss = { viewModel.dismissDialog() },
        )
        is DialogState.NameInput -> NameInputDialog(
            onConfirm = { name -> viewModel.registerNewPerson(state.badgeId, name) },
            onDismiss = { viewModel.dismissDialog() },
        )
        is DialogState.DeleteUser -> DeleteUserDialog(
            persons = persons,
            onConfirm = { name -> viewModel.deleteUser(name) },
            onDismiss = { viewModel.dismissDialog() },
        )
        DialogState.None -> Unit
    }
}
