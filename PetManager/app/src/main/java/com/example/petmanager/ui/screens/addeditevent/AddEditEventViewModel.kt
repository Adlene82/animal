package com.example.petmanager.ui.screens.addeditevent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.ui.navigation.Screen // For argument name
import com.example.petmanager.util.Event as UtilEvent // Alias to avoid name clash
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditEventUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val eventDate: LocalDate? = null,
    val eventTime: LocalTime? = null,
    val selectedEventType: String = "",
    val availableAnimals: List<Animal> = emptyList(),
    val selectedAnimalId: Long? = null,
    val availableContacts: List<Contact> = emptyList(),
    val selectedContactId: Long? = null,
    val isRecurring: Boolean = false,

    val titleError: String? = null,
    val eventTypeError: String? = null,
    val dateTimeError: String? = null,

    val saveButtonEnabled: Boolean = false,

    val navigateBackEvent: UtilEvent<Unit>? = null,
    val snackbarMessage: UtilEvent<String>? = null
)

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddEditEventUiState())
        private set

    private val eventId: Long? = savedStateHandle[Screen.AddEditEvent.argumentName]
    val eventTypes = listOf("VACCIN", "TRAITEMENT", "RDV_VETO", "ANNIVERSAIRE", "GROOMING", "AUTRE")

    init {
        uiState = uiState.copy(isLoading = true) // Start loading animals and contacts

        viewModelScope.launch {
            val animals = petRepository.getAllAnimals().firstOrNull() ?: emptyList()
            val contacts = petRepository.getAllContacts().firstOrNull() ?: emptyList()

            uiState = uiState.copy(
                availableAnimals = animals,
                availableContacts = contacts,
                isLoading = false // Initial data loaded
            )

            if (eventId != null && eventId != -1L) { // -1L is the default from nav arg
                uiState = uiState.copy(isEditing = true, isLoading = true) // Now load the specific event
                loadEventData(eventId)
            } else {
                // New event, ensure a default event type is set if list is not empty
                if (eventTypes.isNotEmpty()) {
                    uiState = uiState.copy(selectedEventType = eventTypes.first())
                }
                validateFields() // Initial validation
            }
        }
    }

    private fun loadEventData(id: Long) {
        viewModelScope.launch {
            val event = petRepository.getEventById(id).firstOrNull()
            if (event != null) {
                uiState = uiState.copy(
                    isLoading = false,
                    title = event.title,
                    description = event.description ?: "",
                    location = event.location ?: "",
                    eventDate = event.dateTime.toLocalDate(),
                    eventTime = event.dateTime.toLocalTime(),
                    selectedEventType = event.eventType.ifBlank { eventTypes.firstOrNull() ?: "" },
                    selectedAnimalId = event.animalId,
                    selectedContactId = event.contactId,
                    isRecurring = event.isRecurring
                )
                validateFields()
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    snackbarMessage = UtilEvent("Événement non trouvé.")
                )
            }
        }
    }

    // Field change handlers
    fun onTitleChanged(value: String) { uiState = uiState.copy(title = value, titleError = null); validateFields() }
    fun onDescriptionChanged(value: String) { uiState = uiState.copy(description = value); validateFields() }
    fun onLocationChanged(value: String) { uiState = uiState.copy(location = value); validateFields() }
    fun onDateSelected(date: LocalDate) { uiState = uiState.copy(eventDate = date, dateTimeError = null); validateFields() }
    fun onTimeSelected(time: LocalTime) { uiState = uiState.copy(eventTime = time, dateTimeError = null); validateFields() }
    fun onEventTypeSelected(type: String) { uiState = uiState.copy(selectedEventType = type, eventTypeError = null); validateFields() }
    fun onAnimalSelected(animalIdString: String?) { // Dropdown might return string representation of ID or "Aucun"
        val animalId = animalIdString?.toLongOrNull()
        uiState = uiState.copy(selectedAnimalId = animalId); validateFields()
    }
    fun onContactSelected(contactIdString: String?) {
        val contactId = contactIdString?.toLongOrNull()
        uiState = uiState.copy(selectedContactId = contactId); validateFields()
    }
    fun onIsRecurringChanged(isRecurringValue: Boolean) { uiState = uiState.copy(isRecurring = isRecurringValue); validateFields() }


    private fun validateFields(): Boolean {
        var isValid = true
        uiState = uiState.copy(
            titleError = if (uiState.title.isBlank()) { isValid = false; "Le titre est requis." } else null,
            eventTypeError = if (uiState.selectedEventType.isBlank()) { isValid = false; "Le type d'événement est requis." } else null,
            dateTimeError = if (uiState.eventDate == null || uiState.eventTime == null) { isValid = false; "Date et heure complètes requises." } else null
        )
        uiState = uiState.copy(saveButtonEnabled = isValid)
        return isValid
    }

    fun saveEvent() {
        if (!validateFields()) {
            uiState = uiState.copy(snackbarMessage = UtilEvent("Veuillez corriger les erreurs."))
            return
        }

        val eventDateTime = LocalDateTime.of(uiState.eventDate!!, uiState.eventTime!!) // Validation ensures not null

        viewModelScope.launch {
            val eventToSave = Event(
                eventId = if (uiState.isEditing) eventId!! else 0,
                title = uiState.title.trim(),
                description = uiState.description.trim().takeIf { it.isNotBlank() },
                location = uiState.location.trim().takeIf { it.isNotBlank() },
                dateTime = eventDateTime,
                eventType = uiState.selectedEventType,
                animalId = uiState.selectedAnimalId,
                contactId = uiState.selectedContactId,
                isRecurring = uiState.isRecurring
            )

            try {
                if (uiState.isEditing) {
                    petRepository.updateEvent(eventToSave)
                } else {
                    petRepository.insertEvent(eventToSave)
                }
                uiState = uiState.copy(
                    snackbarMessage = UtilEvent("Événement sauvegardé."),
                    navigateBackEvent = UtilEvent(Unit)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    snackbarMessage = UtilEvent("Erreur sauvegarde: ${e.localizedMessage}")
                )
            }
        }
    }
    fun consumeSnackbarMessage() { uiState = uiState.copy(snackbarMessage = null) }
    fun consumeNavigateBackEvent() { uiState = uiState.copy(navigateBackEvent = null) }
}
