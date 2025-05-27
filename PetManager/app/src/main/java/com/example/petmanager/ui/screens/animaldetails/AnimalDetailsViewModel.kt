package com.example.petmanager.ui.screens.animaldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.data.local.model.WeightRecord
import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.ui.navigation.Screen // For argument name
import com.example.petmanager.ui.screens.dashboard.calculateAge // Re-use from dashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnimalDetailsUiState(
    val isLoading: Boolean = true,
    val animal: Animal? = null,
    val age: String? = null,
    val latestWeight: WeightRecord? = null,
    val medicalHistoryEvents: List<Event> = emptyList(),
    val otherEvents: List<Event> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AnimalDetailsViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimalDetailsUiState())
    val uiState: StateFlow<AnimalDetailsUiState> = _uiState.asStateFlow()

    private val animalId: Long? = savedStateHandle[Screen.AnimalDetails.argumentName]

    init {
        if (animalId == null || animalId == -1L) { // -1L might be default from nav arg
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "ID de l'animal non valide ou manquant."
                )
            }
        } else {
            loadAnimalDetails(animalId)
        }
    }

    private fun loadAnimalDetails(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val animalFlow = petRepository.getAnimalById(id)
                val animal = animalFlow.firstOrNull() // Collect the first emission

                if (animal == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Animal non trouvé."
                        )
                    }
                    return@launch
                }

                val age = calculateAge(animal.birthDate, LocalDate.now())

                val weightRecordsFlow = petRepository.getWeightRecordsForAnimal(id)
                val latestWeight = weightRecordsFlow.firstOrNull()?.maxByOrNull { it.date }


                val eventsFlow = petRepository.getEventsForAnimal(id)
                val allEvents = eventsFlow.firstOrNull() ?: emptyList()


                val medicalEventTypes = listOf("VACCIN", "TRAITEMENT", "RDV_VETO") // Define medical types
                val medicalHistoryEvents = allEvents.filter { event ->
                    medicalEventTypes.any { type -> event.eventType.equals(type, ignoreCase = true) }
                }.sortedByDescending { it.dateTime }

                val otherEvents = allEvents.filterNot { event ->
                    medicalHistoryEvents.contains(event)
                }.sortedByDescending { it.dateTime }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        animal = animal,
                        age = age,
                        latestWeight = latestWeight,
                        medicalHistoryEvents = medicalHistoryEvents,
                        otherEvents = otherEvents,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur lors du chargement des détails de l'animal: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // onEditClick is handled by UI navigation
}
