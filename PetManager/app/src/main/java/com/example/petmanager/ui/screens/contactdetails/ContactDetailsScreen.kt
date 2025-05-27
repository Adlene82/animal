package com.example.petmanager.ui.screens.contactdetails

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.DetailItem // Assuming DetailItem is accessible
import com.example.petmanager.ui.screens.addeditcontact.AddEditContactUiState
import com.example.petmanager.ui.screens.addeditcontact.AddEditContactViewModel
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(
    navController: NavController,
    // contactId is passed via SavedStateHandle to ViewModel
    viewModel: AddEditContactViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(key1 = uiState.launchIntentEvent) {
        uiState.launchIntentEvent?.getContentIfNotHandled()?.let { intent ->
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Show a snackbar or toast if no app can handle the intent
                snackbarHostState.showSnackbar(
                    message = "Aucune application trouvée pour cette action.",
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.consumeLaunchIntentEvent()
        }
    }
    LaunchedEffect(key1 = uiState.snackbarMessage) {
        uiState.snackbarMessage?.getContentIfNotHandled()?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.consumeSnackbarMessage()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.name.ifEmpty { stringResource(R.string.title_contact_details) }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (!uiState.isEditing) { // Show Edit button only if not already editing (e.g. after save)
                        IconButton(onClick = {
                            // Option 1: Navigate to AddEditContactScreen with ID
                            // navController.navigate(Screen.AddEditContact.createRoute(uiState.contactId))

                            // Option 2: Enable edit mode in the current ViewModel instance
                            // This might be preferred if ContactDetailsScreen and AddEditContactScreen are merged
                            // or if AddEditContactScreen is designed to take over the current view.
                            // For now, let's assume AddEditContactScreen is a separate form screen.
                            uiState.contactId?.let {
                                navController.navigate(Screen.AddEditContact.createRoute(it))
                            }

                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.contactId == null) { // Or a specific error state for not found
             Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.contact_not_found))
            }
        }
        else {
            ContactDetailsContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                viewModel = viewModel, // Pass viewModel for actions
                onPlanAppointmentClick = {
                    uiState.contactId?.let { contactId ->
                        navController.navigate(Screen.AddEditEvent.createRoute(null, contactId = contactId))
                    }
                }
            )
        }
    }
}

@Composable
fun ContactDetailsContent(
    modifier: Modifier = Modifier,
    uiState: AddEditContactUiState,
    viewModel: AddEditContactViewModel,
    onPlanAppointmentClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = stringResource(if (uiState.isFavorite) R.string.label_unfavorite else R.string.label_favorite),
                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { /* No direct edit here, handled in edit screen */ }
            )
        }
        Divider()

        DetailItem(label = stringResource(R.string.label_category), value = uiState.category)
        if (uiState.specialty.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_specialty), value = uiState.specialty)
        }
        if (uiState.phoneNumber.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_phone_number), value = uiState.phoneNumber)
        }
        if (uiState.emailAddress.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_email_address), value = uiState.emailAddress)
        }
        if (uiState.address.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_address), value = uiState.address)
        }
        if (uiState.website.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_website), value = uiState.website)
        }
        if (uiState.notes.isNotBlank()) {
            DetailItem(label = stringResource(R.string.label_notes), value = uiState.notes)
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                text = stringResource(R.string.action_call),
                icon = Icons.Filled.Phone,
                onClick = viewModel::prepareDialIntent,
                enabled = uiState.phoneNumber.isNotBlank()
            )
            ActionButton(
                text = stringResource(R.string.action_email),
                icon = Icons.Filled.Email,
                onClick = viewModel::prepareSendToEmailIntent,
                enabled = uiState.emailAddress.isNotBlank()
            )
            ActionButton(
                text = stringResource(R.string.action_view_website),
                icon = Icons.Filled.Language,
                onClick = viewModel::prepareViewWebsiteIntent,
                enabled = uiState.website.isNotBlank()
            )
            ActionButton(
                text = stringResource(R.string.action_view_address),
                icon = Icons.Filled.LocationOn,
                onClick = viewModel::prepareViewAddressIntent,
                enabled = uiState.address.isNotBlank()
            )
            Spacer(Modifier.height(8.dp)) // Visual separation
            Button(
                onClick = onPlanAppointmentClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.action_plan_appointment))
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(text)
    }
}

@Preview(showBackground = true, name = "Contact Details Screen - Loaded")
@Composable
fun ContactDetailsScreenPreview_Loaded() {
    PetManagerTheme {
        // ViewModel interaction is complex for preview.
        // Directly previewing Content with sample state.
        ContactDetailsContent(
            uiState = AddEditContactUiState(
                isLoading = false,
                name = "Dr. Alice Vétérinaire",
                category = "Vétérinaire",
                specialty = "Chirurgie",
                phoneNumber = "123-456-7890",
                emailAddress = "alice@vet.com",
                address = "123 Rue des Animaux",
                website = "www.alicevet.com",
                isFavorite = true,
                notes = "Très compétente et douce."
            ),
            viewModel = hiltViewModel(), // This won't fully work in preview
            onPlanAppointmentClick = {}
        )
    }
}

// String resources:
// <string name="title_contact_details">Détails du Contact</string>
// <string name="contact_not_found">Contact non trouvé.</string>
// <string name="label_category">Catégorie</string>
// <string name="label_specialty">Spécialité</string>
// <string name="label_phone_number">Numéro de téléphone</string>
// <string name="label_email_address">Adresse e-mail</string>
// <string name="label_address">Adresse</string>
// <string name="label_website">Site Web</string>
// <string name="label_notes">Notes</string>
// <string name="label_favorite">Marquer comme favori</string>
// <string name="label_unfavorite">Retirer des favoris</string>
// <string name="action_call">Appeler</string>
// <string name="action_email">Envoyer un e-mail</string>
// <string name="action_view_website">Visiter le site Web</string>
// <string name="action_view_address">Voir sur la carte</string>
// <string name="action_plan_appointment">Planifier un RDV</string>
// (action_edit, action_back are assumed from other screens)
