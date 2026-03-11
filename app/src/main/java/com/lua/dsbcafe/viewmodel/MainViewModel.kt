package com.lua.dsbcafe.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lua.dsbcafe.data.model.Person
import com.lua.dsbcafe.data.repository.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface DialogState {
    data object None : DialogState
    data class DoubleShot(val badgeId: String, val person: Person) : DialogState
    data class NameInput(val badgeId: String) : DialogState
    data object DeleteUser : DialogState
    data object ManualEdit : DialogState
}

sealed interface UiMessage {
    data class Info(val text: String) : UiMessage
    data class Error(val text: String) : UiMessage
}

class MainViewModel : ViewModel() {
    private val repository = PersonRepository()

    val persons: StateFlow<List<Person>> = repository.observePersons()
        .map { it.sortedByDescending { p -> p.coffeeCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCount: StateFlow<Int> = persons
        .map { list -> list.sumOf { it.coffeeCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _dialogState = MutableStateFlow<DialogState>(DialogState.None)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isExpertMode = MutableStateFlow(false)
    val isExpertMode: StateFlow<Boolean> = _isExpertMode.asStateFlow()

    fun onNfcTagRead(badgeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = repository.getOrNull(badgeId)
                if (existing != null) {
                    val updated = existing.copy(coffeeCount = existing.coffeeCount + 1)
                    repository.save(updated)
                    _dialogState.value = DialogState.DoubleShot(badgeId, updated)
                } else {
                    _dialogState.value = DialogState.NameInput(badgeId)
                }
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Failed to read badge: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerNewPerson(badgeId: String, name: String) {
        viewModelScope.launch {
            val person = Person(name = name, coffeeCount = 1, badgeId = badgeId)
            try {
                repository.save(person)
                _dialogState.value = DialogState.DoubleShot(badgeId, person)
                _message.value = UiMessage.Info("Welcome, $name!")
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Failed to register: ${e.message}")
                _dialogState.value = DialogState.None
            }
        }
    }

    fun confirmDouble(badgeId: String, person: Person) {
        viewModelScope.launch {
            val updated = person.copy(coffeeCount = person.coffeeCount + 1)
            try {
                repository.save(updated)
                _message.value = UiMessage.Info("Double shot counted for ${person.name}!")
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Failed to update count: ${e.message}")
            } finally {
                _dialogState.value = DialogState.None
            }
        }
    }

    fun dismissDialog() {
        _dialogState.value = DialogState.None
    }

    fun showDeleteUserDialog() {
        _dialogState.value = DialogState.DeleteUser
    }

    fun showManualEditDialog() {
        _dialogState.value = DialogState.ManualEdit
    }

    fun toggleExpertMode() {
        _isExpertMode.value = !_isExpertMode.value
    }

    fun incrementCount(badgeId: String) {
        viewModelScope.launch {
            try {
                val person = persons.value.find { it.badgeId == badgeId } ?: return@launch
                repository.save(person.copy(coffeeCount = person.coffeeCount + 1))
                _message.value = UiMessage.Info("Added a coffee for ${person.name}.")
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Failed to increment: ${e.message}")
            }
        }
    }

    fun decrementCount(badgeId: String) {
        viewModelScope.launch {
            try {
                val person = persons.value.find { it.badgeId == badgeId } ?: return@launch
                if (person.coffeeCount > 0) {
                    repository.save(person.copy(coffeeCount = person.coffeeCount - 1))
                    _message.value = UiMessage.Info("Removed a coffee for ${person.name}.")
                }
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Failed to decrement: ${e.message}")
            }
        }
    }

    fun resetCounts() {
        viewModelScope.launch {
            try {
                repository.resetAllCounts(persons.value)
                _message.value = UiMessage.Info("All counts reset.")
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Reset failed: ${e.message}")
            }
        }
    }

    fun deleteUser(name: String) {
        viewModelScope.launch {
            try {
                repository.deletePerson(name)
                _message.value = UiMessage.Info("$name removed.")
            } catch (e: Exception) {
                _message.value = UiMessage.Error("Delete failed: ${e.message}")
            } finally {
                _dialogState.value = DialogState.None
            }
        }
    }

    fun sendStatisticsEmail(context: Context) {
        val emailBody = persons.value.joinToString("\n") { "${it.name}: ${it.coffeeCount}" }
        val currentMonth = LocalDate.now().month
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Coffee Count for $currentMonth")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    fun clearMessage() {
        _message.value = null
    }
}
