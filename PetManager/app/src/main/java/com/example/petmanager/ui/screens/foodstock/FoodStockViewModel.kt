package com.example.petmanager.ui.screens.foodstock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.FoodItem
import com.example.petmanager.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FoodItemDisplay(
    val id: Long,
    val name: String,
    val brand: String?,
    val type: String,
    val currentQuantity: Double,
    val initialQuantity: Double,
    val quantityUnit: String,
    val photoUri: String?,
    val lowStock: Boolean,
    val stockProgress: Float
)

data class FoodStockUiState(
    val isLoading: Boolean = true,
    val foodItems: List<FoodItemDisplay> = emptyList(),
    val filterQuery: String = "", // For future use
    val errorMessage: String? = null
)

@HiltViewModel
class FoodStockViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodStockUiState())
    val uiState: StateFlow<FoodStockUiState> = _uiState.asStateFlow()

    init {
        loadFoodItems()
    }

    private fun loadFoodItems() {
        petRepository.getAllFoodItems()
            .onEach { foodItemList ->
                val displayItems = foodItemList.map { mapToFoodItemDisplay(it) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        foodItems = displayItems,
                        errorMessage = null
                    )
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur lors du chargement des réserves: ${e.localizedMessage}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun mapToFoodItemDisplay(foodItem: FoodItem): FoodItemDisplay {
        val stockProgress = if (foodItem.initialQuantity > 0) {
            (foodItem.currentQuantity / foodItem.initialQuantity).toFloat()
        } else {
            0f
        }
        val lowStock = foodItem.lowStockThreshold != null && foodItem.currentQuantity <= foodItem.lowStockThreshold

        return FoodItemDisplay(
            id = foodItem.foodItemId,
            name = foodItem.name,
            brand = foodItem.brand,
            type = foodItem.type,
            currentQuantity = foodItem.currentQuantity,
            initialQuantity = foodItem.initialQuantity,
            quantityUnit = foodItem.quantityUnit,
            photoUri = foodItem.photoUri,
            lowStock = lowStock,
            stockProgress = stockProgress.coerceIn(0f, 1f) // Ensure progress is between 0 and 1
        )
    }

    // onAddFoodItemClick and onFoodItemClick are handled by navigation in the UI
    // No specific ViewModel logic needed for them for V0 other than potentially logging.
    fun onFoodItemClicked(foodItemId: Long) {
        // Placeholder for any future ViewModel logic related to item click
        println("Food item clicked: $foodItemId")
    }
}
