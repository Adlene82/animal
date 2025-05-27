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
import com.example.petmanager.R // Assuming R class is generated
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
                        // TODO: Use string resources R.string.title_edit_event, R.string.title_add_event
                        if (uiState.isEditing) stringResource(id = R.string.title_edit_event_placeholder)
                        else stringResource(id = R.string.title_add_event_placeholder),
                        style = MaterialTheme.typography.titleLarge // Consistent typography
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
                        onClick = viewModel::saveEvent,
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
        // Show loader only for initial animal/contact list loading when creating new event
        // or when loading existing event data.
        if (uiState.isLoading && (uiState.isEditing || (uiState.availableAnimals.isEmpty() && uiState.availableContacts.isEmpty()))) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AddEditEventForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(all = 16.dp) // Consistent padding
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) { // Consistent spacing
        // Show loader if editing and event data is still loading (after animal/contact lists are loaded)
        if (uiState.isLoading && uiState.isEditing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onViewModelEvent::onTitleChanged,
            // TODO: Use string resource R.string.label_title_required
            label = { Text(stringResource(id = R.string.label_title_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.titleError != null,
            supportingText = { uiState.titleError?.let { Text(it, style = MaterialTheme.typography.bodySmall) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium // M3 shape
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onViewModelEvent::onDescriptionChanged,
            // TODO: Use string resource R.string.label_description
            label = { Text(stringResource(id = R.string.label_description_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            maxLines = 3,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.location,
            onValueChange = onViewModelEvent::onLocationChanged,
            // TODO: Use string resource R.string.label_location
            label = { Text(stringResource(id = R.string.label_location_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // Date Picker
        OutlinedTextField(
            value = uiState.eventDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
            onValueChange = {},
            // TODO: Use string resource R.string.label_date_required
            label = { Text(stringResource(id = R.string.label_date_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth().clickable { showDatePickerDialog = true }.minimumInteractiveComponentSize(),
            readOnly = true,
            // TODO: Use string resource R.string.calendar_icon_description
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(id = R.string.calendar_icon_description_placeholder)) },
            isError = uiState.dateTimeError != null && uiState.eventDate == null,
            shape = MaterialTheme.shapes.medium
        )

        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.eventDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePickerDialog = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                onViewModelEvent.onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
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
            ) { DatePicker(state = datePickerState) }
        }

        // Time Picker
        OutlinedTextField(
            value = uiState.eventTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: "",
            onValueChange = {},
            // TODO: Use string resource R.string.label_time_required
            label = { Text(stringResource(id = R.string.label_time_placeholder) + "*") },
            modifier = Modifier.fillMaxWidth().clickable { showTimePickerDialog = true }.minimumInteractiveComponentSize(),
            readOnly = true,
            // TODO: Use string resource R.string.time_icon_description
            trailingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = stringResource(id = R.string.time_icon_description_placeholder)) },
            isError = uiState.dateTimeError != null && uiState.eventTime == null,
            shape = MaterialTheme.shapes.medium
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
                    TextButton(
                        onClick = {
                            showTimePickerDialog = false
                            onViewModelEvent.onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) { Text(stringResource(id = R.string.action_ok_placeholder)) } // TODO: Use string resource
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePickerDialog = false },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) { Text(stringResource(id = R.string.action_cancel_placeholder)) } // TODO: Use string resource
                }
            ) {
                TimePicker(state = timePickerState, modifier = Modifier.padding(16.dp).fillMaxWidth()) // Ensure TimePicker fills width
            }
        }

        if (uiState.dateTimeError != null) {
            Text(uiState.dateTimeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Event Type Dropdown
        DropdownField(
            // TODO: Use string resource R.string.label_event_type_required
            label = stringResource(id = R.string.label_event_type_placeholder) + "*",
            options = eventTypes,
            selectedOption = uiState.selectedEventType.ifEmpty { eventTypes.firstOrNull() ?: "" },
            onOptionSelected = onViewModelEvent::onEventTypeSelected,
            error = uiState.eventTypeError
        )

        // Animal Dropdown
        DropdownField(
            // TODO: Use string resource R.string.label_associated_animal
            label = stringResource(id = R.string.label_associated_animal_placeholder),
            // TODO: Use string resource R.string.option_none
            options = listOf(stringResource(id = R.string.option_none_placeholder)) + uiState.availableAnimals.map { it.name },
            selectedOption = uiState.availableAnimals.find { it.animalId == uiState.selectedAnimalId }?.name ?: stringResource(id = R.string.option_none_placeholder),
            onOptionSelected = { selectedName ->
                val selectedAnimal = uiState.availableAnimals.find { it.name == selectedName }
                onViewModelEvent.onAnimalSelected(selectedAnimal?.animalId?.toString())
            }
        )

        // Contact Dropdown
        DropdownField(
            // TODO: Use string resource R.string.label_associated_contact
            label = stringResource(id = R.string.label_associated_contact_placeholder),
            // TODO: Use string resource R.string.option_none
            options = listOf(stringResource(id = R.string.option_none_placeholder)) + uiState.availableContacts.map { it.name },
            selectedOption = uiState.availableContacts.find { it.contactId == uiState.selectedContactId }?.name ?: stringResource(id = R.string.option_none_placeholder),
            onOptionSelected = { selectedName ->
                val selectedContact = uiState.availableContacts.find { it.name == selectedName }
                onViewModelEvent.onContactSelected(selectedContact?.contactId?.toString())
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(), // Ensure row is easily tappable for Switch
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // TODO: Use string resource R.string.label_recurring_event
            Text(stringResource(id = R.string.label_recurring_event_placeholder), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = uiState.isRecurring,
                onCheckedChange = onViewModelEvent::onIsRecurringChanged,
                modifier = Modifier.minimumInteractiveComponentSize() // Ensure switch itself is large enough
            )
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
    error: String? = null,
    enabled: Boolean = true // Added enabled parameter
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }, // Only change if enabled
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {}, // Read-only
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().minimumInteractiveComponentSize(), // Ensure touch target
            isError = error != null,
            supportingText = { error?.let { Text(it, style = MaterialTheme.typography.bodySmall) } },
            shape = MaterialTheme.shapes.medium, // M3 shape
            enabled = enabled // Pass enabled to OutlinedTextField
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled, // Only show if enabled
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    modifier = Modifier.minimumInteractiveComponentSize() // Ensure touch target
                )
            }
        }
    }
}

// Custom TimePickerDialog Composable
@Composable
fun TimePickerDialog(
    title: String = "Select Time", // TODO: Use string resource
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) }, // Consistent title typography
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = MaterialTheme.shapes.extraLarge // M3 Dialog shape
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

// Placeholder string resource IDs used:
// R.string.title_edit_event_placeholder
// R.string.title_add_event_placeholder
// R.string.action_back_placeholder
// R.string.action_save_placeholder
// R.string.label_title_placeholder
// R.string.label_description_placeholder
// R.string.label_location_placeholder
// R.string.label_date_placeholder
// R.string.calendar_icon_description_placeholder
// R.string.action_ok_placeholder
// R.string.action_cancel_placeholder
// R.string.label_time_placeholder
// R.string.time_icon_description_placeholder
// R.string.label_event_type_placeholder
// R.string.label_associated_animal_placeholder
// R.string.option_none_placeholder
// R.string.label_associated_contact_placeholder
// R.string.label_recurring_event_placeholder
