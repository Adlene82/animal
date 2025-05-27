package com.example.petmanager.ui.screens.addeditcontact

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.ui.navigation.Screen // For argument name
import com.example.petmanager.util.Event // Using existing Event wrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditContactUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false, // True if in edit mode or adding new
    val contactId: Long? = null,

    val name: String = "",
    val category: String = "",
    val specialty: String = "",
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val address: String = "",
    val website: String = "",
    val isFavorite: Boolean = false,
    val notes: String = "",

    val nameError: String? = null,
    val categoryError: String? = null,

    val saveButtonEnabled: Boolean = false,

    val navigateBackEvent: Event<Unit>? = null,
    val snackbarMessage: Event<String>? = null,
    val launchIntentEvent: Event<Intent>? = null
)

@HiltViewModel
class AddEditContactViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddEditContactUiState())
        private set

    val contactCategories = listOf("Vétérinaire", "Toiletteur", "Pension", "Éducateur", "Magasin", "Autre")

    init {
        val contactIdFromNav: Long? = savedStateHandle[Screen.AddEditContact.argumentName]
            ?: savedStateHandle[Screen.ContactDetails.argumentName] // Check both if used for details too

        uiState = uiState.copy(contactId = contactIdFromNav)

        if (contactIdFromNav != null && contactIdFromNav != -1L) {
            loadContactData(contactIdFromNav)
        } else {
            // New contact, prepare for editing immediately
            prepareNewContact()
        }
    }

    private fun loadContactData(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val contact = petRepository.getContactById(id).firstOrNull()
            if (contact != null) {
                uiState = uiState.copy(
                    isLoading = false,
                    name = contact.name,
                    category = contact.category.ifBlank { contactCategories.firstOrNull() ?: "" },
                    specialty = contact.specialty ?: "",
                    phoneNumber = contact.phoneNumber ?: "",
                    emailAddress = contact.emailAddress ?: "",
                    address = contact.address ?: "",
                    website = contact.website ?: "",
                    isFavorite = contact.isFavorite,
                    notes = contact.notes ?: "",
                    isEditing = false // Default to view mode for existing contact
                )
                // No validation needed for viewing details initially
                // Save button will be disabled if not in edit mode
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    snackbarMessage = Event("Contact non trouvé.")
                )
            }
        }
    }

    fun prepareNewContact() {
        uiState = uiState.copy(
            isLoading = false,
            isEditing = true, // Ready for input
            contactId = null, // Ensure it's null for a new contact
            name = "", category = contactCategories.firstOrNull() ?: "", specialty = "",
            phoneNumber = "", emailAddress = "", address = "", website = "",
            isFavorite = false, notes = "",
            nameError = null, categoryError = null,
            saveButtonEnabled = false // Initially disabled until valid input
        )
        validateFields() // Call to set initial saveButtonEnabled state correctly
    }

    fun enableEditMode() {
        uiState = uiState.copy(isEditing = true)
        validateFields() // Validate fields when entering edit mode
    }

    // Field change handlers
    fun onNameChanged(value: String) { uiState = uiState.copy(name = value); if(uiState.isEditing) validateFields() }
    fun onCategorySelected(value: String) { uiState = uiState.copy(category = value); if(uiState.isEditing) validateFields() }
    fun onSpecialtyChanged(value: String) { uiState = uiState.copy(specialty = value); if(uiState.isEditing) validateFields() }
    fun onPhoneNumberChanged(value: String) { uiState = uiState.copy(phoneNumber = value); if(uiState.isEditing) validateFields() }
    fun onEmailAddressChanged(value: String) { uiState = uiState.copy(emailAddress = value); if(uiState.isEditing) validateFields() }
    fun onAddressChanged(value: String) { uiState = uiState.copy(address = value); if(uiState.isEditing) validateFields() }
    fun onWebsiteChanged(value: String) { uiState = uiState.copy(website = value); if(uiState.isEditing) validateFields() }
    fun onIsFavoriteChanged(value: Boolean) { uiState = uiState.copy(isFavorite = value); if(uiState.isEditing) validateFields() }
    fun onNotesChanged(value: String) { uiState = uiState.copy(notes = value); if(uiState.isEditing) validateFields() }


    private fun validateFields(): Boolean {
        if (!uiState.isEditing) { // No validation if not in edit mode
            uiState = uiState.copy(saveButtonEnabled = false)
            return false
        }
        var isValid = true
        uiState = uiState.copy(
            nameError = if (uiState.name.isBlank()) { isValid = false; "Le nom est requis." } else null,
            categoryError = if (uiState.category.isBlank()) { isValid = false; "La catégorie est requise." } else null
        )
        uiState = uiState.copy(saveButtonEnabled = isValid)
        return isValid
    }

    fun saveContact() {
        if (!validateFields()) {
            uiState = uiState.copy(snackbarMessage = Event("Veuillez corriger les erreurs."))
            return
        }

        viewModelScope.launch {
            val contactToSave = Contact(
                contactId = uiState.contactId ?: 0,
                name = uiState.name.trim(),
                category = uiState.category,
                specialty = uiState.specialty.trim().takeIf { it.isNotBlank() },
                phoneNumber = uiState.phoneNumber.trim().takeIf { it.isNotBlank() },
                emailAddress = uiState.emailAddress.trim().takeIf { it.isNotBlank() },
                address = uiState.address.trim().takeIf { it.isNotBlank() },
                website = uiState.website.trim().takeIf { it.isNotBlank() },
                isFavorite = uiState.isFavorite,
                notes = uiState.notes.trim().takeIf { it.isNotBlank() }
            )

            try {
                if (uiState.contactId != null && uiState.contactId != 0L) { // Check if it's an existing contact
                    petRepository.updateContact(contactToSave)
                } else {
                    petRepository.insertContact(contactToSave)
                }
                uiState = uiState.copy(
                    snackbarMessage = Event("Contact sauvegardé."),
                    navigateBackEvent = Event(Unit),
                    isEditing = false // Exit edit mode after saving
                )
            } catch (e: Exception) {
                uiState = uiState.copy(snackbarMessage = Event("Erreur sauvegarde: ${e.localizedMessage}"))
            }
        }
    }

    fun prepareDialIntent() {
        if (!uiState.phoneNumber.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${uiState.phoneNumber}"))
            uiState = uiState.copy(launchIntentEvent = Event(intent))
        } else {
            uiState = uiState.copy(snackbarMessage = Event("Numéro de téléphone non disponible."))
        }
    }

    fun prepareSendToEmailIntent() {
        if (!uiState.emailAddress.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(uiState.emailAddress))
            }
            // Check if there's an app to handle this intent before launching
            // This check is typically done in the Composable/Activity.
            // For now, just post the event.
            uiState = uiState.copy(launchIntentEvent = Event(intent))
        } else {
            uiState = uiState.copy(snackbarMessage = Event("Adresse e-mail non disponible."))
        }
    }

    fun prepareViewWebsiteIntent() {
        if (!uiState.website.isNullOrBlank()) {
            var url = uiState.website!!
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            uiState = uiState.copy(launchIntentEvent = Event(intent))
        } else {
            uiState = uiState.copy(snackbarMessage = Event("Site web non disponible."))
        }
    }

    fun prepareViewAddressIntent() {
        if (!uiState.address.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(uiState.address)}"))
            uiState = uiState.copy(launchIntentEvent = Event(intent))
        } else {
            uiState = uiState.copy(snackbarMessage = Event("Adresse non disponible."))
        }
    }


    fun consumeSnackbarMessage() { uiState = uiState.copy(snackbarMessage = null) }
    fun consumeNavigateBackEvent() { uiState = uiState.copy(navigateBackEvent = null) }
    fun consumeLaunchIntentEvent() { uiState = uiState.copy(launchIntentEvent = null) }
}
