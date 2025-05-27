package com.example.petmanager.ui.screens.addeditanimal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Pets // Placeholder for animal image
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // For placeholder drawable and string resources
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnimalScreen(
    navController: NavController,
    viewModel: AddEditAnimalViewModel = hiltViewModel()
    // animalId is handled by ViewModel's SavedStateHandle
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle Snackbar messages
    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.getContentIfNotHandled()?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.consumeSnackbarMessage() // Important to consume the event
        }
    }

    // Handle navigation
    LaunchedEffect(key1 = uiState.navigateBackEvent) {
        uiState.navigateBackEvent?.getContentIfNotHandled()?.let {
            navController.popBackStack()
            viewModel.consumeNavigateBackEvent() // Important to consume the event
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) stringResource(R.string.title_edit_animal)
                        else stringResource(R.string.title_add_animal)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveAnimal() },
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
            AddEditAnimalForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                uiState = uiState,
                sexOptions = viewModel.sexOptions,
                onNameChanged = viewModel::onNameChanged,
                onSpeciesChanged = viewModel::onSpeciesChanged,
                onBreedChanged = viewModel::onBreedChanged,
                onChipIdChanged = viewModel::onChipIdChanged,
                onSexChanged = viewModel::onSexChanged,
                onBirthDateSelected = viewModel::onBirthDateSelected,
                onPhotoSelected = viewModel::onPhotoSelected,
                onDietChanged = viewModel::onDietChanged,
                onAllergiesChanged = viewModel::onAllergiesChanged,
                onGeneralNotesChanged = viewModel::onGeneralNotesChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnimalForm(
    modifier: Modifier = Modifier,
    uiState: AddEditAnimalUiState,
    sexOptions: List<String>,
    onNameChanged: (String) -> Unit,
    onSpeciesChanged: (String) -> Unit,
    onBreedChanged: (String) -> Unit,
    onChipIdChanged: (String) -> Unit,
    onSexChanged: (String) -> Unit,
    onBirthDateSelected: (LocalDate) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onDietChanged: (String) -> Unit,
    onAllergiesChanged: (String) -> Unit,
    onGeneralNotesChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var showDatePickerDialog by remember { mutableStateOf(false) }

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
                    contentDescription = stringResource(R.string.animal_photo_description_current),
                    placeholder = painterResource(R.drawable.ic_placeholder_pet),
                    error = painterResource(R.drawable.ic_placeholder_pet),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(48.dp))
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
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.species,
            onValueChange = onSpeciesChanged,
            label = { Text(stringResource(R.string.label_species) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.speciesError != null,
            supportingText = { uiState.speciesError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.breed,
            onValueChange = onBreedChanged,
            label = { Text(stringResource(R.string.label_breed)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        // Birth Date Picker
        OutlinedTextField(
            value = uiState.birthDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
            onValueChange = { /* Read-only, updated by dialog */ },
            label = { Text(stringResource(R.string.label_birthdate)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePickerDialog = true },
            readOnly = true,
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            isError = uiState.birthDateError != null, // Assuming birthDateError is part of uiState
            supportingText = { uiState.birthDateError?.let { Text(it) } }
        )

        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.birthDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                    ?: LocalDate.now().atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
                yearRange = (LocalDate.now().year - 50)..LocalDate.now().year // Example range
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            onBirthDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                        }
                    }) { Text(stringResource(R.string.action_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) { Text(stringResource(R.string.action_cancel)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }


        // Sex Selection
        var sexExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = sexExpanded,
            onExpandedChange = { sexExpanded = !sexExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.sex,
                onValueChange = { /* Read-only, updated by dropdown */ },
                label = { Text(stringResource(R.string.label_sex)) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth() // Important for dropdown behavior
            )
            ExposedDropdownMenu(
                expanded = sexExpanded,
                onDismissRequest = { sexExpanded = false }
            ) {
                sexOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onSexChanged(selectionOption)
                            sexExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.chipId,
            onValueChange = onChipIdChanged,
            label = { Text(stringResource(R.string.label_chip_id)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.diet,
            onValueChange = onDietChanged,
            label = { Text(stringResource(R.string.label_diet)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            maxLines = 3
        )

        OutlinedTextField(
            value = uiState.allergies,
            onValueChange = onAllergiesChanged,
            label = { Text(stringResource(R.string.label_allergies)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            maxLines = 3
        )

        OutlinedTextField(
            value = uiState.generalNotes,
            onValueChange = onGeneralNotesChanged,
            label = { Text(stringResource(R.string.label_general_notes)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done // Last field
            ),
            maxLines = 5
        )
    }
}

@Preview(showBackground = true, name = "Add Animal Screen")
@Composable
fun AddEditAnimalScreenPreview_Add() {
    PetManagerTheme {
        // Mock NavController, ViewModel is more complex due to Hilt and SavedStateHandle
        // For previewing the form itself, it's easier to call AddEditAnimalForm directly
        AddEditAnimalForm(
            uiState = AddEditAnimalUiState(isEditing = false, sex = "Mâle", saveButtonEnabled = true),
            sexOptions = listOf("Mâle", "Femelle", "Inconnu"),
            onNameChanged = {}, onSpeciesChanged = {}, onBreedChanged = {}, onChipIdChanged = {},
            onSexChanged = {}, onBirthDateSelected = {}, onPhotoSelected = {}, onDietChanged = {},
            onAllergiesChanged = {}, onGeneralNotesChanged = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Animal Screen")
@Composable
fun AddEditAnimalScreenPreview_Edit() {
    PetManagerTheme {
        AddEditAnimalForm(
            uiState = AddEditAnimalUiState(
                isEditing = true, name = "Biscotte", species = "Chat", sex = "Femelle",
                birthDate = LocalDate.now().minusYears(2), saveButtonEnabled = true
            ),
            sexOptions = listOf("Mâle", "Femelle", "Inconnu"),
            onNameChanged = {}, onSpeciesChanged = {}, onBreedChanged = {}, onChipIdChanged = {},
            onSexChanged = {}, onBirthDateSelected = {}, onPhotoSelected = {}, onDietChanged = {},
            onAllergiesChanged = {}, onGeneralNotesChanged = {}
        )
    }
}

// Needed String resources for preview and actual use:
// <string name="title_edit_animal">Modifier Animal</string>
// <string name="title_add_animal">Ajouter Animal</string>
// <string name="action_save">Sauvegarder</string>
// <string name="animal_photo_description_current">Photo actuelle de l\'animal</string>
// <string name="action_choose_photo">Choisir une photo</string>
// <string name="label_birthdate">Date de naissance</string>
// <string name="action_ok">OK</string>
// <string name="action_cancel">Annuler</string>
// <string name="label_sex">Sexe</string>
// (Other labels like name, species, breed, chip_id, diet, allergies, general_notes are assumed from AnimalDetailsScreen or are common)
// (action_back is assumed from AnimalDetailsScreen)
