package com.example.petmanager.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ContactsUiState(
    val isLoading: Boolean = true,
    val contacts: List<Contact> = emptyList(),
    // filterQuery and sortOrder can be added later
    val errorMessage: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        petRepository.getAllContacts()
            // The DAO already sorts by name ASC, so no extra .map { it.sortedBy... } needed here
            // If DAO didn't sort, it would be: .map { contacts -> contacts.sortedBy { it.name } }
            .onEach { contactList ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        contacts = contactList,
                        errorMessage = null
                    )
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur lors du chargement des contacts: ${e.localizedMessage}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // onAddContactClick and onContactClick are handled by navigation in the UI
    // No specific ViewModel logic needed for them for V0 other than potentially logging.
    fun onContactClicked(contactId: Long) {
        // Placeholder for any future ViewModel logic related to item click
        println("Contact clicked: $contactId")
    }
}
