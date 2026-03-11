package com.lua.dsbcafe.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.lua.dsbcafe.ui.components.dialogs.ManualEditDialog
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
    val isExpertMode by viewModel.isExpertMode.collectAsState()

    var adminExpanded by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(pageCount = { 2 })

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            IconButton(
                onClick = onSignOut,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign out",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> SummaryPage(
                        totalCount = totalCount,
                        isLoading = isLoading,
                        onTap = {
                            tapCount++
                            if (tapCount >= 5) {
                                adminExpanded = !adminExpanded
                                tapCount = 0
                            }
                        },
                    )
                    1 -> LeaderboardPage(
                        persons = persons,
                        isLoading = isLoading,
                        onDoubleTap = { badgeId -> viewModel.incrementCount(badgeId) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (adminExpanded) 96.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(2) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (selected) 10.dp else 7.dp)
                            .padding(0.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                            label = "dot",
                        ) { isSelected ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isSelected) {
                                            Modifier.size(10.dp)
                                        } else {
                                            Modifier.size(7.dp)
                                        }
                                    ),
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = if (isSelected) {
                                            androidx.compose.ui.graphics.Color(0xFF0033A0)
                                        } else {
                                            androidx.compose.ui.graphics.Color(0x660033A0)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

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
                    onIncrement = {
                        adminExpanded = false
                        viewModel.showManualEditDialog()
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
        is DialogState.ManualEdit -> ManualEditDialog(
            persons = persons,
            isExpertMode = isExpertMode,
            onToggleExpertMode = { viewModel.toggleExpertMode() },
            onIncrement = { badgeId -> viewModel.incrementCount(badgeId) },
            onDecrement = { badgeId -> viewModel.decrementCount(badgeId) },
            onDismiss = { viewModel.dismissDialog() },
        )
        DialogState.None -> Unit
    }
}

@Composable
private fun SummaryPage(
    totalCount: Int,
    isLoading: Boolean,
    onTap: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading && totalCount == 0) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onTap() })
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = totalCount.toString(),
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "coffees drunk",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardPage(
    persons: List<com.lua.dsbcafe.data.model.Person>,
    isLoading: Boolean,
    onDoubleTap: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {

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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    items = persons,
                    key = { it.badgeId },
                ) { person ->
                    PersonItem(
                        person = person,
                        onDoubleTap = { onDoubleTap(person.badgeId) },
                    )
                }
            }
        }
    }
}
