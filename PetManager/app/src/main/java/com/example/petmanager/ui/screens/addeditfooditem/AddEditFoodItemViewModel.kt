package com.example.petmanager.ui.screens.addeditfooditem

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.FoodItem
import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.ui.navigation.Screen // For argument name
import com.example.petmanager.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.DecimalFormatSymbols
import java.util.Locale

data class AddEditFoodItemUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val brand: String = "",
    val type: String = "",
    val targetSpecies: String = "",
    val initialQuantity: String = "",
    val currentQuantity: String = "",
    val quantityUnit: String = "",
    val lowStockThreshold: String = "",
    val photoUri: String? = null,

    val nameError: String? = null,
    val initialQuantityError: String? = null,
    val currentQuantityError: String? = null,
    val quantityUnitError: String? = null,
    val lowStockThresholdError: String? = null,

    val saveButtonEnabled: Boolean = false,

    val navigateBackEvent: Event<Unit>? = null,
    val snackbarMessage: Event<String>? = null
)

@HiltViewModel
class AddEditFoodItemViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddEditFoodItemUiState())
        private set

    private val foodItemId: Long? = savedStateHandle[Screen.AddEditFoodItem.argumentName]
    private val decimalSeparator = DecimalFormatSymbols(Locale.getDefault()).decimalSeparator

    init {
        if (foodItemId != null && foodItemId != -1L) { // -1L is the default from nav arg
            uiState = uiState.copy(isEditing = true, isLoading = true)
            loadFoodItemData(foodItemId)
        } else {
            validateFields() // Initial validation
        }
    }

    private fun loadFoodItemData(id: Long) {
        viewModelScope.launch {
            val foodItem = petRepository.getFoodItemById(id).firstOrNull()
            if (foodItem != null) {
                uiState = uiState.copy(
                    isLoading = false,
                    name = foodItem.name,
                    brand = foodItem.brand ?: "",
                    type = foodItem.type,
                    targetSpecies = foodItem.targetSpecies ?: "",
                    initialQuantity = formatDouble(foodItem.initialQuantity),
                    currentQuantity = formatDouble(foodItem.currentQuantity),
                    quantityUnit = foodItem.quantityUnit,
                    lowStockThreshold = foodItem.lowStockThreshold?.let { formatDouble(it) } ?: "",
                    photoUri = foodItem.photoUri
                )
                validateFields()
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    snackbarMessage = Event("Article non trouvé.")
                )
            }
        }
    }

    private fun formatDouble(value: Double): String {
        return value.toString().replace('.', decimalSeparator)
    }

    private fun parseDouble(value: String): Double? {
        return value.replace(decimalSeparator, '.').toDoubleOrNull()
    }

    fun onNameChanged(value: String) {
        uiState = uiState.copy(name = value, nameError = null)
        validateFields()
    }
    fun onBrandChanged(value: String) { uiState = uiState.copy(brand = value); validateFields() }
    fun onTypeChanged(value: String) { uiState = uiState.copy(type = value); validateFields() }
    fun onTargetSpeciesChanged(value: String) { uiState = uiState.copy(targetSpecies = value); validateFields() }

    fun onInitialQuantityChanged(value: String) {
        uiState = uiState.copy(initialQuantity = value, initialQuantityError = null)
        validateFields()
    }
    fun onCurrentQuantityChanged(value: String) {
        uiState = uiState.copy(currentQuantity = value, currentQuantityError = null)
        validateFields()
    }
    fun onQuantityUnitChanged(value: String) {
        uiState = uiState.copy(quantityUnit = value, quantityUnitError = null)
        validateFields()
    }
    fun onLowStockThresholdChanged(value: String) {
        uiState = uiState.copy(lowStockThreshold = value, lowStockThresholdError = null)
        validateFields() // Re-validate as it affects save button, even if not strictly required for itself
    }
    fun onPhotoSelected(uri: Uri?) { uiState = uiState.copy(photoUri = uri?.toString()) }


    private fun validateFields(): Boolean {
        var isValid = true
        uiState = uiState.copy(
            nameError = if (uiState.name.isBlank()) { isValid = false; "Le nom est requis." } else null,
            quantityUnitError = if (uiState.quantityUnit.isBlank()) { isValid = false; "L'unité est requise." } else null
        )

        val initialQty = parseDouble(uiState.initialQuantity)
        uiState = uiState.copy(initialQuantityError = if (uiState.initialQuantity.isBlank()) {
            isValid = false; "Quantité initiale requise."
        } else if (initialQty == null) {
            isValid = false; "Quantité initiale invalide."
        } else if (initialQty <= 0) {
            isValid = false; "Doit être > 0."
        } else null)

        val currentQty = parseDouble(uiState.currentQuantity)
        uiState = uiState.copy(currentQuantityError = if (uiState.currentQuantity.isBlank()) {
            isValid = false; "Quantité actuelle requise."
        } else if (currentQty == null) {
            isValid = false; "Quantité actuelle invalide."
        } else if (currentQty < 0) {
            isValid = false; "Ne peut être < 0."
        } else if (initialQty != null && currentQty > initialQty) {
            isValid = false; "Ne peut excéder Q. Initiale"
        }
        else null)

        val lowStockThresholdVal = uiState.lowStockThreshold.takeIf { it.isNotBlank() }?.let { parseDouble(it) }
        uiState = uiState.copy(lowStockThresholdError = if (uiState.lowStockThreshold.isNotBlank() && lowStockThresholdVal == null) {
             // Only error if not blank and invalid. Blank is acceptable.
            isValid = false; "Seuil de stock bas invalide."
        } else if (lowStockThresholdVal != null && lowStockThresholdVal < 0) {
            isValid = false; "Ne peut être < 0."
        }
        else null)


        uiState = uiState.copy(saveButtonEnabled = isValid)
        return isValid
    }

    fun saveFoodItem() {
        if (!validateFields()) {
            uiState = uiState.copy(snackbarMessage = Event("Veuillez corriger les erreurs."))
            return
        }

        val initialQuantityValue = parseDouble(uiState.initialQuantity)!! // Validation ensures not null
        val currentQuantityValue = parseDouble(uiState.currentQuantity)!! // Validation ensures not null
        val lowStockThresholdValue = uiState.lowStockThreshold.takeIf { it.isNotBlank() }?.let { parseDouble(it) }


        viewModelScope.launch {
            val foodItemToSave = FoodItem(
                foodItemId = if (uiState.isEditing) foodItemId!! else 0,
                name = uiState.name.trim(),
                brand = uiState.brand.trim().takeIf { it.isNotBlank() },
                type = uiState.type.trim(),
                targetSpecies = uiState.targetSpecies.trim().takeIf { it.isNotBlank() },
                initialQuantity = initialQuantityValue,
                currentQuantity = currentQuantityValue,
                quantityUnit = uiState.quantityUnit.trim(),
                lowStockThreshold = lowStockThresholdValue,
                photoUri = uiState.photoUri
            )

            try {
                if (uiState.isEditing) {
                    petRepository.updateFoodItem(foodItemToSave)
                } else {
                    petRepository.insertFoodItem(foodItemToSave)
                }
                uiState = uiState.copy(
                    snackbarMessage = Event("Article sauvegardé."),
                    navigateBackEvent = Event(Unit)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    snackbarMessage = Event("Erreur sauvegarde: ${e.localizedMessage}")
                )
            }
        }
    }
    fun consumeSnackbarMessage() { uiState = uiState.copy(snackbarMessage = null) }
    fun consumeNavigateBackEvent() { uiState = uiState.copy(navigateBackEvent = null) }
}
