package com.example.petmanager.ui.screens.addeditcontact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R
import com.example.petmanager.ui.screens.addeditanimal.DropdownField // Reusing DropdownField from add/edit animal
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    navController: NavController,
    // contactId is handled by ViewModel's SavedStateHandle
    viewModel: AddEditContactViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // This screen is always for editing or adding new.
    // If contactId was provided, ViewModel loads it. If not, ViewModel prepares for new.
    // We need to ensure edit mode is active.
    LaunchedEffect(Unit) {
        if (uiState.contactId == null) { // If it's a new contact (ID is null from SavedStateHandle)
            viewModel.prepareNewContact() // This sets isEditing = true
        } else { // If it's an existing contact, explicitly enable edit mode
            viewModel.enableEditMode()
        }
    }

    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.getContentIfNotHandled()?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.consumeSnackbarMessage()
        }
    }

    LaunchedEffect(key1 = uiState.navigateBackEvent) {
        uiState.navigateBackEvent?.getContentIfNotHandled()?.let {
            navController.popBackStack() // Or navigate to details screen if preferred after save
            viewModel.consumeNavigateBackEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.contactId != null) stringResource(R.string.title_edit_contact)
                        else stringResource(R.string.title_add_contact)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveContact,
                        enabled = uiState.saveButtonEnabled && uiState.isEditing
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
            AddEditContactForm(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                uiState = uiState,
                contactCategories = viewModel.contactCategories,
                viewModel = viewModel // Pass full viewModel for callbacks
            )
        }
    }
}

@Composable
fun AddEditContactForm(
    modifier: Modifier = Modifier,
    uiState: AddEditContactUiState,
    contactCategories: List<String>,
    viewModel: AddEditContactViewModel // For direct callback usage
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text(stringResource(R.string.label_name) + "*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = { uiState.nameError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = uiState.isEditing
        )

        DropdownField( // Reusing from AddEditAnimalScreen.kt, ensure it's accessible
            label = stringResource(R.string.label_category) + "*",
            options = contactCategories,
            selectedOption = uiState.category,
            onOptionSelected = viewModel::onCategorySelected,
            error = uiState.categoryError,
            // enabled = uiState.isEditing // DropdownField needs an enabled parameter
        )

        OutlinedTextField(
            value = uiState.specialty,
            onValueChange = viewModel::onSpecialtyChanged,
            label = { Text(stringResource(R.string.label_specialty)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = uiState.isEditing
        )

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = viewModel::onPhoneNumberChanged,
            label = { Text(stringResource(R.string.label_phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = uiState.isEditing
        )

        OutlinedTextField(
            value = uiState.emailAddress,
            onValueChange = viewModel::onEmailAddressChanged,
            label = { Text(stringResource(R.string.label_email_address)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = uiState.isEditing
        )

        OutlinedTextField(
            value = uiState.address,
            onValueChange = viewModel::onAddressChanged,
            label = { Text(stringResource(R.string.label_address)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            maxLines = 3,
            enabled = uiState.isEditing
        )

        OutlinedTextField(
            value = uiState.website,
            onValueChange = viewModel::onWebsiteChanged,
            label = { Text(stringResource(R.string.label_website)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = uiState.isEditing
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.label_favorite), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = uiState.isFavorite,
                onCheckedChange = viewModel::onIsFavoriteChanged,
                enabled = uiState.isEditing
            )
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChanged,
            label = { Text(stringResource(R.string.label_notes)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            maxLines = 5,
            enabled = uiState.isEditing
        )
    }
}

@Preview(showBackground = true, name = "Add Contact Screen")
@Composable
fun AddEditContactScreenPreview_Add() {
    PetManagerTheme {
        // ViewModel is complex to mock directly. Preview AddEditContactForm.
        AddEditContactForm(
            uiState = AddEditContactUiState(isEditing = true, saveButtonEnabled = true, category = "Vétérinaire"),
            contactCategories = listOf("Vétérinaire", "Toiletteur", "Pension"),
            viewModel = hiltViewModel() // Will not work for callbacks in preview
        )
    }
}

@Preview(showBackground = true, name = "Edit Contact Screen with Data")
@Composable
fun AddEditContactScreenPreview_Edit() {
    PetManagerTheme {
        AddEditContactForm(
            uiState = AddEditContactUiState(
                isEditing = true,
                name = "Dr. Alice",
                category = "Vétérinaire",
                specialty = "Canin",
                phoneNumber = "123-456-7890",
                emailAddress = "alice@vet.com",
                saveButtonEnabled = true
            ),
            contactCategories = listOf("Vétérinaire", "Toiletteur", "Pension"),
            viewModel = hiltViewModel()
        )
    }
}

// String resources:
// <string name="title_edit_contact">Modifier Contact</string>
// <string name="title_add_contact">Ajouter Contact</string>
// (Labels are assumed from ContactDetailsScreen or common)
