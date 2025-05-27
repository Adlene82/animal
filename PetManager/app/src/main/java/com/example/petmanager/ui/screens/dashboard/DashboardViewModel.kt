package com.example.petmanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val animals: List<AnimalListItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Debounce search query to avoid too many emissions while typing
        val debouncedSearchQuery = _searchQuery.debounce(300)

        petRepository.getAllAnimals()
            .combine(debouncedSearchQuery) { animals, query ->
                _uiState.update { it.copy(isLoading = true) }
                val filteredAnimals = if (query.isBlank()) {
                    animals
                } else {
                    animals.filter { animal ->
                        animal.name.contains(query, ignoreCase = true) ||
                        animal.species.contains(query, ignoreCase = true) ||
                        (animal.breed?.contains(query, ignoreCase = true) == true)
                    }
                }
                Pair(filteredAnimals, query) // Pass query for state update
            }
            .onEach { (filteredAnimals, query) ->
                val animalListItems = filteredAnimals.map { animal ->
                    mapAnimalToListItem(animal)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        animals = animalListItems,
                        searchQuery = query, // Ensure UI state's query reflects the one used for filtering
                        errorMessage = null
                    )
                }
            }
            .launchIn(viewModelScope) // Use launchIn for flow collection in ViewModel
    }

    private fun mapAnimalToListItem(animal: Animal): AnimalListItem {
        val age = calculateAge(animal.birthDate)
        // Placeholder for quickStatus, can be expanded later
        val quickStatus = if (animal.birthDate.month == LocalDate.now().month &&
            animal.birthDate.dayOfMonth == LocalDate.now().dayOfMonth) {
            "🎂 Anniversaire !"
        } else {
            // Placeholder for other statuses like upcoming vaccinations
            null // "Prochain vaccin: Demain"
        }

        return AnimalListItem(
            id = animal.animalId,
            name = animal.name,
            species = animal.species,
            breed = animal.breed,
            age = age,
            photoUri = animal.photoUri,
            quickStatus = quickStatus
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // No need to update _uiState.searchQuery directly here,
        // it will be updated by the combine operator when new results are emitted.
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    // Navigation related actions are typically handled in the UI layer (Composable)
    // but ViewModel can be aware if needed for logging or other side effects.
    fun onAnimalClick(animalId: Long) {
        // Log or handle any VM-specific logic if needed
        println("Animal clicked: $animalId")
    }

    fun onAddAnimalClick() {
        // Log or handle any VM-specific logic if needed
        println("Add animal clicked")
    }
}

// Utility function - can be moved to a utils package later
fun calculateAge(birthDate: LocalDate, today: LocalDate = LocalDate.now()): String {
    val period = Period.between(birthDate, today)
    val years = period.years
    val months = period.months

    return when {
        years > 0 && months > 0 -> "$years an${if (years > 1) "s" else ""} et $months mois"
        years > 0 && months == 0 -> "$years an${if (years > 1) "s" else ""}"
        years == 0 && months > 0 -> "$months mois"
        years == 0 && months == 0 -> "Moins d'un mois" // Or "Nouveau-né"
        else -> "Date de naissance invalide" // Should not happen with valid data
    }
}
