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
import com.example.petmanager.R // Assuming R class is generated
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodItemScreen(
    navController: NavController,
    viewModel: AddEditFoodItemViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.getContentIfNotHandled()?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.consumeSnackbarMessage()
        }
    }

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
                        // TODO: Use string resources R.string.title_edit_food_item, R.string.title_add_food_item
                        if (uiState.isEditing) stringResource(R.string.title_edit_food_item_placeholder)
                        else stringResource(R.string.title_add_food_item_placeholder),
                        style = MaterialTheme.typography.titleLarge // Consistent typography
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            // TODO: Use string resource R.string.action_back
                            contentDescription = stringResource(R.string.action_back_placeholder)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveFoodItem() },
                        enabled = uiState.saveButtonEnabled,
                        modifier = Modifier.minimumInteractiveComponentSize() // Ensure touch target
                    ) {
                        // TODO: Use string resource R.string.action_save
                        Text(stringResource(R.string.action_save_placeholder))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant // M3 style
                )
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
                    .padding(all = 16.dp) // Consistent padding
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) { // Consistent spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.large) // M3 shape
                .background(MaterialTheme.colorScheme.secondaryContainer) // Theme color
                .clickable { imagePickerLauncher.launch("image/*") }
                .minimumInteractiveComponentSize(), // Ensure touch target
            contentAlignment = Alignment.Center
        ) {
            if (uiState.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uiState.photoUri)
                        .crossfade(true)
                        .build(),
                    // TODO: Use string resource R.string.food_item_photo_current_description
                    contentDescription = stringResource(R.string.food_item_photo_description_current_placeholder),
                    placeholder = painterResource(R.drawable.ic_placeholder_food),
                    error = painterResource(R.drawable.ic_placeholder_food),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Fastfood,
                        // TODO: Use string resource R.string.choose_photo_icon_description
                        contentDescription = stringResource(R.string.choose_photo_icon_description_placeholder),
                        modifier = Modifier.size(48.dp)
                    )
                    // TODO: Use string resource R.string.action_choose_photo
                    Text(stringResource(R.string.action_choose_photo_placeholder), style = MaterialTheme.typography.bodyMedium) // Improved style
                }
            }
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            // TODO: Use string resource R.string.label_name_required
            label = { Text(stringResource(R.string.label_name_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = { uiState.nameError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium // M3 shape
        )

        OutlinedTextField(
            value = uiState.brand,
            onValueChange = onBrandChanged,
            // TODO: Use string resource R.string.label_brand
            label = { Text(stringResource(R.string.label_brand_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.type,
            onValueChange = onTypeChanged,
            // TODO: Use string resource R.string.label_type
            label = { Text(stringResource(R.string.label_type_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.targetSpecies,
            onValueChange = onTargetSpeciesChanged,
            // TODO: Use string resource R.string.label_target_species
            label = { Text(stringResource(R.string.label_target_species_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.initialQuantity,
                onValueChange = onInitialQuantityChanged,
                // TODO: Use string resource R.string.label_initial_quantity_required
                label = { Text(stringResource(R.string.label_initial_quantity_placeholder) + "*") },
                modifier = Modifier.weight(1f),
                isError = uiState.initialQuantityError != null,
                supportingText = { uiState.initialQuantityError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = uiState.currentQuantity,
                onValueChange = onCurrentQuantityChanged,
                // TODO: Use string resource R.string.label_current_quantity_required
                label = { Text(stringResource(R.string.label_current_quantity_placeholder) + "*") },
                modifier = Modifier.weight(1f),
                isError = uiState.currentQuantityError != null,
                supportingText = { uiState.currentQuantityError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        OutlinedTextField(
            value = uiState.quantityUnit,
            onValueChange = onQuantityUnitChanged,
            // TODO: Use string resource R.string.label_quantity_unit_required
            label = { Text(stringResource(R.string.label_quantity_unit_placeholder) + "*") },
             // TODO: Use string resource R.string.placeholder_quantity_unit
            placeholder = { Text(stringResource(R.string.placeholder_quantity_unit_placeholder))},
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.quantityUnitError != null,
            supportingText = { uiState.quantityUnitError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.lowStockThreshold,
            onValueChange = onLowStockThresholdChanged,
            // TODO: Use string resource R.string.label_low_stock_threshold
            label = { Text(stringResource(R.string.label_low_stock_threshold_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.lowStockThresholdError != null,
            supportingText = { uiState.lowStockThresholdError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
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

// Placeholder string resource IDs used:
// R.string.title_edit_food_item_placeholder
// R.string.title_add_food_item_placeholder
// R.string.action_back_placeholder
// R.string.action_save_placeholder
// R.string.food_item_photo_description_current_placeholder
// R.string.choose_photo_icon_description_placeholder
// R.string.action_choose_photo_placeholder
// R.string.label_name_placeholder
// R.string.label_brand_placeholder
// R.string.label_type_placeholder
// R.string.label_target_species_placeholder
// R.string.label_initial_quantity_placeholder
// R.string.label_current_quantity_placeholder
// R.string.label_quantity_unit_placeholder
// R.string.placeholder_quantity_unit_placeholder
// R.string.label_low_stock_threshold_placeholder
