package com.example.petmanager.ui.screens.addeditanimal

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
                        // TODO: Use string resources R.string.title_edit_animal, R.string.title_add_animal
                        if (uiState.isEditing) stringResource(id = R.string.title_edit_animal_placeholder)
                        else stringResource(id = R.string.title_add_animal_placeholder)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            // TODO: Use string resource R.string.action_back
                            contentDescription = stringResource(id = R.string.action_back_placeholder)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveAnimal() },
                        enabled = uiState.saveButtonEnabled,
                        modifier = Modifier.minimumInteractiveComponentSize() // Ensure touch target
                    ) {
                        // TODO: Use string resource R.string.action_save
                        Text(stringResource(id = R.string.action_save_placeholder))
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
            AddEditAnimalForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(all = 16.dp) // Consistent padding
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
                    // TODO: Use string resource R.string.animal_photo_current_description
                    contentDescription = stringResource(id = R.string.animal_photo_description_current_placeholder),
                    placeholder = painterResource(R.drawable.ic_placeholder_pet),
                    error = painterResource(R.drawable.ic_placeholder_pet),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Pets,
                        // TODO: Use string resource R.string.choose_photo_icon_description
                        contentDescription = stringResource(id = R.string.choose_photo_icon_description_placeholder),
                        modifier = Modifier.size(48.dp)
                    )
                    // TODO: Use string resource R.string.action_choose_photo
                    Text(stringResource(id = R.string.action_choose_photo_placeholder), style = MaterialTheme.typography.bodyMedium) // Improved style
                }
            }
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            // TODO: Use string resource R.string.label_name_required
            label = { Text(stringResource(id = R.string.label_name_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = { uiState.nameError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium // M3 shape
        )

        OutlinedTextField(
            value = uiState.species,
            onValueChange = onSpeciesChanged,
            // TODO: Use string resource R.string.label_species_required
            label = { Text(stringResource(id = R.string.label_species_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.speciesError != null,
            supportingText = { uiState.speciesError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.breed,
            onValueChange = onBreedChanged,
            // TODO: Use string resource R.string.label_breed
            label = { Text(stringResource(id = R.string.label_breed_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.birthDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
            onValueChange = { /* Read-only */ },
            // TODO: Use string resource R.string.label_birthdate
            label = { Text(stringResource(id = R.string.label_birthdate_placeholder)) },
            modifier = Modifier.fillMaxWidth().clickable { showDatePickerDialog = true },
            readOnly = true,
            // TODO: Use string resource R.string.calendar_icon_description
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(id = R.string.calendar_icon_description_placeholder)) },
            isError = uiState.birthDateError != null,
            supportingText = { uiState.birthDateError?.let { Text(it) } },
            shape = MaterialTheme.shapes.medium
        )

        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.birthDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                    ?: System.currentTimeMillis(), // Default to today
                yearRange = (LocalDate.now().year - 100)..LocalDate.now().year // Expanded range
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePickerDialog = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                onBirthDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) { Text(stringResource(id = R.string.action_ok_placeholder)) } // TODO: Use string resource
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePickerDialog = false },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) { Text(stringResource(id = R.string.action_cancel_placeholder)) } // TODO: Use string resource
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        var sexExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = sexExpanded,
            onExpandedChange = { sexExpanded = !sexExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.sex,
                onValueChange = { /* Read-only */ },
                // TODO: Use string resource R.string.label_sex
                label = { Text(stringResource(id = R.string.label_sex_placeholder)) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(), // Important for dropdown behavior
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(expanded = sexExpanded, onDismissRequest = { sexExpanded = false }) {
                sexOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onSexChanged(selectionOption)
                            sexExpanded = false
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.chipId,
            onValueChange = onChipIdChanged,
            // TODO: Use string resource R.string.label_chip_id
            label = { Text(stringResource(id = R.string.label_chip_id_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), // Changed for alphanumeric
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.diet,
            onValueChange = onDietChanged,
            // TODO: Use string resource R.string.label_diet
            label = { Text(stringResource(id = R.string.label_diet_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            maxLines = 3,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.allergies,
            onValueChange = onAllergiesChanged,
            // TODO: Use string resource R.string.label_allergies
            label = { Text(stringResource(id = R.string.label_allergies_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            maxLines = 3,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.generalNotes,
            onValueChange = onGeneralNotesChanged,
            // TODO: Use string resource R.string.label_general_notes
            label = { Text(stringResource(id = R.string.label_general_notes_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            maxLines = 5,
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Preview(showBackground = true, name = "Add Animal Screen")
@Composable
fun AddEditAnimalScreenPreview_Add() {
    PetManagerTheme {
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

// Placeholder string resource IDs used in TODOs:
// R.string.title_edit_animal_placeholder
// R.string.title_add_animal_placeholder
// R.string.action_back_placeholder
// R.string.action_save_placeholder
// R.string.animal_photo_description_current_placeholder
// R.string.choose_photo_icon_description_placeholder
// R.string.action_choose_photo_placeholder
// R.string.label_name_placeholder
// R.string.label_species_placeholder
// R.string.label_breed_placeholder
// R.string.label_birthdate_placeholder
// R.string.calendar_icon_description_placeholder
// R.string.action_ok_placeholder
// R.string.action_cancel_placeholder
// R.string.label_sex_placeholder
// R.string.label_chip_id_placeholder
// R.string.label_diet_placeholder
// R.string.label_allergies_placeholder
// R.string.label_general_notes_placeholder
