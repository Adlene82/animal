package com.example.petmanager.ui.screens.addeditfooditem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fastfood // Placeholder for food image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // For placeholder drawable and string resources
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodItemScreen(
    navController: NavController,
    viewModel: AddEditFoodItemViewModel = hiltViewModel()
    // foodItemId is handled by ViewModel's SavedStateHandle
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle Snackbar messages
    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.getContentIfNotHandled()?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.consumeSnackbarMessage()
        }
    }

    // Handle navigation
    LaunchedEffect(key1 = uiState.navigateBackEvent) {
        uiState.navigateBackEvent?.getContentIfNotHandled()?.let {
            navController.popBackStack()
            viewModel.consumeNavigateBackEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) stringResource(R.string.title_edit_food_item)
                        else stringResource(R.string.title_add_food_item)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveFoodItem() },
                        enabled = uiState.saveButtonEnabled
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AddEditFoodItemForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                uiState = uiState,
                onNameChanged = viewModel::onNameChanged,
                onBrandChanged = viewModel::onBrandChanged,
                onTypeChanged = viewModel::onTypeChanged,
                onTargetSpeciesChanged = viewModel::onTargetSpeciesChanged,
                onInitialQuantityChanged = viewModel::onInitialQuantityChanged,
                onCurrentQuantityChanged = viewModel::onCurrentQuantityChanged,
                onQuantityUnitChanged = viewModel::onQuantityUnitChanged,
                onLowStockThresholdChanged = viewModel::onLowStockThresholdChanged,
                onPhotoSelected = viewModel::onPhotoSelected
            )
        }
    }
}

@Composable
fun AddEditFoodItemForm(
    modifier: Modifier = Modifier,
    uiState: AddEditFoodItemUiState,
    onNameChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
    onTargetSpeciesChanged: (String) -> Unit,
    onInitialQuantityChanged: (String) -> Unit,
    onCurrentQuantityChanged: (String) -> Unit,
    onQuantityUnitChanged: (String) -> Unit,
    onLowStockThresholdChanged: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onPhotoSelected(uri)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Photo Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uiState.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.food_item_photo_description_current),
                    placeholder = painterResource(R.drawable.ic_placeholder_food),
                    error = painterResource(R.drawable.ic_placeholder_food),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Fastfood, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text(stringResource(R.string.action_choose_photo), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.label_name) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = { uiState.nameError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.brand,
            onValueChange = onBrandChanged,
            label = { Text(stringResource(R.string.label_brand)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.type,
            onValueChange = onTypeChanged,
            label = { Text(stringResource(R.string.label_type)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.targetSpecies,
            onValueChange = onTargetSpeciesChanged,
            label = { Text(stringResource(R.string.label_target_species)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.initialQuantity,
                onValueChange = onInitialQuantityChanged,
                label = { Text(stringResource(R.string.label_initial_quantity) + "*") },
                modifier = Modifier.weight(1f),
                isError = uiState.initialQuantityError != null,
                supportingText = { uiState.initialQuantityError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.currentQuantity,
                onValueChange = onCurrentQuantityChanged,
                label = { Text(stringResource(R.string.label_current_quantity) + "*") },
                modifier = Modifier.weight(1f),
                isError = uiState.currentQuantityError != null,
                supportingText = { uiState.currentQuantityError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = uiState.quantityUnit,
            onValueChange = onQuantityUnitChanged,
            label = { Text(stringResource(R.string.label_quantity_unit) + "*") },
            placeholder = { Text("kg, g, pcs...")},
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.quantityUnitError != null,
            supportingText = { uiState.quantityUnitError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.lowStockThreshold,
            onValueChange = onLowStockThresholdChanged,
            label = { Text(stringResource(R.string.label_low_stock_threshold)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.lowStockThresholdError != null,
            supportingText = { uiState.lowStockThresholdError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true
        )
    }
}

@Preview(showBackground = true, name = "Add Food Item Screen")
@Composable
fun AddEditFoodItemScreenPreview_Add() {
    PetManagerTheme {
        AddEditFoodItemForm(
            uiState = AddEditFoodItemUiState(isEditing = false, saveButtonEnabled = true),
            onNameChanged = {}, onBrandChanged = {}, onTypeChanged = {}, onTargetSpeciesChanged = {},
            onInitialQuantityChanged = {}, onCurrentQuantityChanged = {}, onQuantityUnitChanged = {},
            onLowStockThresholdChanged = {}, onPhotoSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Food Item Screen with Errors")
@Composable
fun AddEditFoodItemScreenPreview_EditError() {
    PetManagerTheme {
        AddEditFoodItemForm(
            uiState = AddEditFoodItemUiState(
                isEditing = true, name = "Croquettes", initialQuantity = "10a", currentQuantity = "-5", quantityUnit = "",
                nameError = "Le nom est requis.", initialQuantityError = "Quantité initiale invalide.",
                currentQuantityError = "Ne peut être < 0.", quantityUnitError = "L'unité est requise.",
                saveButtonEnabled = false
            ),
            onNameChanged = {}, onBrandChanged = {}, onTypeChanged = {}, onTargetSpeciesChanged = {},
            onInitialQuantityChanged = {}, onCurrentQuantityChanged = {}, onQuantityUnitChanged = {},
            onLowStockThresholdChanged = {}, onPhotoSelected = {}
        )
    }
}

// Needed String resources for preview and actual use:
// <string name="title_edit_food_item">Modifier Article</string>
// <string name="title_add_food_item">Ajouter Article</string>
// <string name="food_item_photo_description_current">Photo actuelle de l\'article</string>
// <string name="label_brand">Marque</string>
// <string name="label_type">Type</string>
// <string name="label_target_species">Espèce Cible</string>
// <string name="label_initial_quantity">Quantité Initiale</string>
// <string name="label_current_quantity">Quantité Actuelle</string>
// <string name="label_quantity_unit">Unité (ex: kg, g)</string>
// <string name="label_low_stock_threshold">Seuil de Stock Bas (optionnel)</string>
// (Other common strings like "action_save", "action_back", "action_choose_photo", "label_name" are assumed)
