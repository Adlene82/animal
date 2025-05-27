package com.example.petmanager.ui.screens.addeditevent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime // For Time Picker
import androidx.compose.material.icons.filled.CalendarToday // For Date Picker
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    navController: NavController,
    viewModel: AddEditEventViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

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
                        if (uiState.isEditing) stringResource(R.string.title_edit_event)
                        else stringResource(R.string.title_add_event)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveEvent,
                        enabled = uiState.saveButtonEnabled
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && !uiState.isEditing) { // Show loader only for initial animal/contact list loading
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AddEditEventForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                uiState = uiState,
                eventTypes = viewModel.eventTypes,
                onViewModelEvent = viewModel // Pass ViewModel directly for multiple callbacks
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventForm(
    modifier: Modifier = Modifier,
    uiState: AddEditEventUiState,
    eventTypes: List<String>,
    onViewModelEvent: AddEditEventViewModel // Simplified parameter passing
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (uiState.isLoading && uiState.isEditing) { // Show loader when editing and loading event data
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onViewModelEvent::onTitleChanged,
            label = { Text(stringResource(R.string.label_title) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.titleError != null,
            supportingText = { uiState.titleError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onViewModelEvent::onDescriptionChanged,
            label = { Text(stringResource(R.string.label_description)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            maxLines = 3
        )

        OutlinedTextField(
            value = uiState.location,
            onValueChange = onViewModelEvent::onLocationChanged,
            label = { Text(stringResource(R.string.label_location)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true
        )

        // Date Picker
        OutlinedTextField(
            value = uiState.eventDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.label_date) + "*") },
            modifier = Modifier.fillMaxWidth().clickable { showDatePickerDialog = true },
            readOnly = true,
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            isError = uiState.dateTimeError != null && uiState.eventDate == null
        )

        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.eventDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            onViewModelEvent.onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                        }
                    }) { Text(stringResource(R.string.action_ok)) }
                },
                dismissButton = { TextButton(onClick = { showDatePickerDialog = false }) { Text(stringResource(R.string.action_cancel)) } }
            ) { DatePicker(state = datePickerState) }
        }

        // Time Picker
        OutlinedTextField(
            value = uiState.eventTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.label_time) + "*") },
            modifier = Modifier.fillMaxWidth().clickable { showTimePickerDialog = true },
            readOnly = true,
            trailingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
            isError = uiState.dateTimeError != null && uiState.eventTime == null
        )

        if (showTimePickerDialog) {
            val timePickerState = rememberTimePickerState(
                initialHour = uiState.eventTime?.hour ?: LocalTime.now().hour,
                initialMinute = uiState.eventTime?.minute ?: LocalTime.now().minute,
                is24Hour = true // Or based on locale/preference
            )
            TimePickerDialog( // Custom Dialog for TimePicker as M3 TimePickerDialog is not standard
                onDismissRequest = { showTimePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showTimePickerDialog = false
                        onViewModelEvent.onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    }) { Text(stringResource(R.string.action_ok)) }
                },
                dismissButton = { TextButton(onClick = { showTimePickerDialog = false }) { Text(stringResource(R.string.action_cancel)) } }
            ) {
                TimePicker(state = timePickerState, modifier = Modifier.padding(16.dp))
            }
        }

        if (uiState.dateTimeError != null) {
            Text(uiState.dateTimeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Event Type Dropdown
        DropdownField(
            label = stringResource(R.string.label_event_type) + "*",
            options = eventTypes,
            selectedOption = uiState.selectedEventType,
            onOptionSelected = onViewModelEvent::onEventTypeSelected,
            error = uiState.eventTypeError
        )

        // Animal Dropdown
        DropdownField(
            label = stringResource(R.string.label_associated_animal),
            options = listOf(stringResource(R.string.option_none)) + uiState.availableAnimals.map { it.name }, // Add "None" option
            selectedOption = uiState.availableAnimals.find { it.animalId == uiState.selectedAnimalId }?.name ?: stringResource(R.string.option_none),
            onOptionSelected = { selectedName ->
                val selectedAnimal = uiState.availableAnimals.find { it.name == selectedName }
                onViewModelEvent.onAnimalSelected(selectedAnimal?.animalId?.toString())
            }
        )

        // Contact Dropdown
        DropdownField(
            label = stringResource(R.string.label_associated_contact),
            options = listOf(stringResource(R.string.option_none)) + uiState.availableContacts.map { it.name },
            selectedOption = uiState.availableContacts.find { it.contactId == uiState.selectedContactId }?.name ?: stringResource(R.string.option_none),
            onOptionSelected = { selectedName ->
                val selectedContact = uiState.availableContacts.find { it.name == selectedName }
                onViewModelEvent.onContactSelected(selectedContact?.contactId?.toString())
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.label_recurring_event), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = uiState.isRecurring, onCheckedChange = onViewModelEvent::onIsRecurringChanged)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption.ifEmpty { options.firstOrNull() ?: "" }, // Show first option if current selection is empty
            onValueChange = {}, // Read-only
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            isError = error != null,
            supportingText = { error?.let { Text(it) } }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Custom TimePickerDialog Composable
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}


@Preview(showBackground = true, name = "Add Event Screen")
@Composable
fun AddEditEventScreenPreview_Add() {
    PetManagerTheme {
        val animals = listOf(Animal(1, "Buddy", "Chien", "Retriever", LocalDate.now(), "Mâle", null, null, null, null, null))
        val contacts = listOf(Contact(1, "Dr. Vet", "Vétérinaire", null, null, null, null, null, false, null))
        AddEditEventForm(
            uiState = AddEditEventUiState(
                isEditing = false,
                saveButtonEnabled = true,
                availableAnimals = animals,
                availableContacts = contacts,
                selectedEventType = "VACCIN"
            ),
            eventTypes = listOf("VACCIN", "TRAITEMENT", "RDV_VETO", "ANNIVERSAIRE", "GROOMING", "AUTRE"),
            onViewModelEvent = hiltViewModel() // This won't work well in preview for callbacks
        )
    }
}

// String resources for preview and actual use:
// <string name="title_edit_event">Modifier Événement</string>
// <string name="title_add_event">Ajouter Événement</string>
// <string name="label_title">Titre</string>
// <string name="label_description">Description</string>
// <string name="label_location">Lieu</string>
// <string name="label_date">Date</string>
// <string name="label_time">Heure</string>
// <string name="label_event_type">Type d\'événement</string>
// <string name="label_associated_animal">Animal Associé (optionnel)</string>
// <string name="label_associated_contact">Contact Associé (optionnel)</string>
// <string name="label_recurring_event">Répéter l\'événement</string>
// <string name="option_none">Aucun</string>
// (Other common strings like "action_save", "action_back", "action_ok", "action_cancel" are assumed)
