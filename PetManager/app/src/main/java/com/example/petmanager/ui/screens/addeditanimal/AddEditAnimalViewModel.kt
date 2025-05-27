package com.example.petmanager.ui.screens.addeditanimal

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.ui.navigation.Screen // For argument name
import com.example.petmanager.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditAnimalUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val species: String = "",
    val breed: String = "", // Optional, so default empty
    val chipId: String = "", // Optional
    val sex: String = "", // Default to first option or empty
    val birthDate: LocalDate? = null,
    val photoUri: String? = null,
    val diet: String = "", // Optional
    val allergies: String = "", // Optional
    val generalNotes: String = "", // Optional

    val nameError: String? = null,
    val speciesError: String? = null,
    val birthDateError: String? = null, // Optional: for future validation if needed

    val saveButtonEnabled: Boolean = false,

    val navigateBackEvent: Event<Unit>? = null,
    val snackbarMessage: Event<String>? = null
)

@HiltViewModel
class AddEditAnimalViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddEditAnimalUiState())
        private set

    private val animalId: Long? = savedStateHandle[Screen.AddEditAnimal.argumentName] // Corrected argument name

    val sexOptions = listOf("Mâle", "Femelle", "Inconnu")

    init {
        if (animalId != null && animalId != -1L) { // -1L is the default from nav arg
            uiState = uiState.copy(isEditing = true, isLoading = true)
            loadAnimalData(animalId)
        } else {
            // New animal, set default sex if options available
            uiState = uiState.copy(sex = sexOptions.firstOrNull() ?: "")
            validateFields() // Initial validation for enabling save button
        }
    }

    private fun loadAnimalData(id: Long) {
        viewModelScope.launch {
            val animal = petRepository.getAnimalById(id).firstOrNull()
            if (animal != null) {
                uiState = uiState.copy(
                    isLoading = false,
                    name = animal.name,
                    species = animal.species,
                    breed = animal.breed ?: "",
                    chipId = animal.chipId ?: "",
                    sex = animal.sex.ifEmpty { sexOptions.firstOrNull() ?: "" }, // Ensure valid option or default
                    birthDate = animal.birthDate,
                    photoUri = animal.photoUri,
                    diet = animal.diet ?: "",
                    allergies = animal.allergies ?: "",
                    generalNotes = animal.generalNotes ?: ""
                )
                validateFields() // Validate after loading
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    snackbarMessage = Event("Animal non trouvé.")
                )
            }
        }
    }

    fun onNameChanged(newName: String) {
        uiState = uiState.copy(name = newName)
        validateFields()
    }

    fun onSpeciesChanged(newSpecies: String) {
        uiState = uiState.copy(species = newSpecies)
        validateFields()
    }

    fun onBreedChanged(newBreed: String) {
        uiState = uiState.copy(breed = newBreed)
        // No specific validation for breed, but save button might depend on overall valid state
    }

    fun onChipIdChanged(newChipId: String) {
        uiState = uiState.copy(chipId = newChipId)
    }

    fun onSexChanged(newSex: String) {
        uiState = uiState.copy(sex = newSex)
    }

    fun onBirthDateSelected(date: LocalDate) {
        uiState = uiState.copy(birthDate = date)
        // Potentially validate date (e.g., not in future) if needed
         uiState = uiState.copy(birthDateError = null) // Clear error if any
        validateFields()
    }

    fun onPhotoSelected(uri: Uri?) {
        uiState = uiState.copy(photoUri = uri?.toString())
    }

    fun onDietChanged(newDiet: String) {
        uiState = uiState.copy(diet = newDiet)
    }

    fun onAllergiesChanged(newAllergies: String) {
        uiState = uiState.copy(allergies = newAllergies)
    }

    fun onGeneralNotesChanged(newNotes: String) {
        uiState = uiState.copy(generalNotes = newNotes)
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val nameError = if (uiState.name.isBlank()) {
            isValid = false
            "Le nom ne peut pas être vide."
        } else null
        val speciesError = if (uiState.species.isBlank()) {
            isValid = false
            "L'espèce ne peut pas être vide."
        } else null

        // Example: Birth date validation (optional, can be expanded)
        // val birthDateError = if (uiState.birthDate == null) {
        //     isValid = false
        //    "La date de naissance est requise."
        // } else if (uiState.birthDate!!.isAfter(LocalDate.now())) {
        //     isValid = false
        //     "La date de naissance ne peut pas être dans le futur."
        // } else null


        uiState = uiState.copy(
            nameError = nameError,
            speciesError = speciesError,
            // birthDateError = birthDateError,
            saveButtonEnabled = isValid
        )
        return isValid
    }

    fun saveAnimal() {
        if (!validateFields()) {
            //This case should ideally not happen if saveButtonEnabled is correctly managed
            uiState = uiState.copy(snackbarMessage = Event("Veuillez corriger les erreurs."))
            return
        }

        viewModelScope.launch {
            val animalToSave = Animal(
                animalId = if (uiState.isEditing) animalId!! else 0, // animalId is non-null if isEditing
                name = uiState.name.trim(),
                species = uiState.species.trim(),
                breed = uiState.breed.trim().takeIf { it.isNotBlank() },
                birthDate = uiState.birthDate ?: LocalDate.now(), // Should be validated to not be null if required
                sex = uiState.sex,
                photoUri = uiState.photoUri,
                chipId = uiState.chipId.trim().takeIf { it.isNotBlank() },
                diet = uiState.diet.trim().takeIf { it.isNotBlank() },
                allergies = uiState.allergies.trim().takeIf { it.isNotBlank() },
                generalNotes = uiState.generalNotes.trim().takeIf { it.isNotBlank() }
            )

            try {
                if (uiState.isEditing) {
                    petRepository.updateAnimal(animalToSave)
                } else {
                    petRepository.insertAnimal(animalToSave)
                }
                uiState = uiState.copy(
                    snackbarMessage = Event("Animal sauvegardé avec succès."),
                    navigateBackEvent = Event(Unit)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    snackbarMessage = Event("Erreur lors de la sauvegarde: ${e.localizedMessage}")
                )
            }
        }
    }

    fun consumeSnackbarMessage() {
        uiState = uiState.copy(snackbarMessage = null)
    }

    fun consumeNavigateBackEvent() {
        uiState = uiState.copy(navigateBackEvent = null)
    }
}
