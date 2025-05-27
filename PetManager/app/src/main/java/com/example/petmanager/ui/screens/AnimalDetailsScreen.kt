package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets // Placeholder for animal image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // For placeholder drawable
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.animaldetails.AnimalDetailsUiState
import com.example.petmanager.ui.screens.animaldetails.AnimalDetailsViewModel
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// DetailItem Composable (defined in this file for simplicity, can be moved)
@Composable
fun DetailItem(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: stringResource(R.string.not_specified), // Assuming R.string.not_specified = "Non renseigné"
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// EventListItem Composable (defined in this file for simplicity)
@Composable
fun EventListItem(event: Event, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(event.dateTime.format(formatter), style = MaterialTheme.typography.bodyMedium)
            event.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailsScreen(
    navController: NavController,
    viewModel: AnimalDetailsViewModel = hiltViewModel()
    // animalId is implicitly handled by ViewModel using SavedStateHandle
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.animal?.name ?: stringResource(R.string.animal_details_title_default)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    uiState.animal?.let { animal ->
                        IconButton(onClick = {
                            navController.navigate(Screen.AddEditAnimal.createRoute(animal.animalId))
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimalDetailsContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState
        )
    }
}

@Composable
fun AnimalDetailsContent(
    modifier: Modifier = Modifier,
    uiState: AnimalDetailsUiState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart // Changed from Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.animal == null -> {
                Text(
                    text = stringResource(R.string.animal_not_found),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                val animal = uiState.animal
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Fill the parent Box
                        .verticalScroll(rememberScrollState()) // Make the Column scrollable
                ) {
                    // Animal Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Fixed height for image
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (animal.photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(animal.photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.animal_photo_description, animal.name),
                                placeholder = painterResource(id = R.drawable.ic_placeholder_pet),
                                error = painterResource(id = R.drawable.ic_placeholder_pet),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                contentDescription = stringResource(R.string.animal_photo_placeholder_description, animal.name),
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Info Section
                    DetailItem(label = stringResource(R.string.label_name), value = animal.name)
                    DetailItem(label = stringResource(R.string.label_species), value = animal.species)
                    DetailItem(label = stringResource(R.string.label_breed), value = animal.breed)
                    DetailItem(label = stringResource(R.string.label_sex), value = animal.sex)
                    DetailItem(
                        label = stringResource(R.string.label_age_birthdate),
                        value = "${uiState.age ?: ""} (${animal.birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))})"
                    )
                    DetailItem(label = stringResource(R.string.label_chip_id), value = animal.chipId)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Weight/Size Section
                    val weightText = uiState.latestWeight?.let { "${it.weightInKg} kg (le ${it.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))})" }
                    DetailItem(label = stringResource(R.string.label_latest_weight), value = weightText)
                    // Optional: TextButton("Voir l'historique") - No action for now

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Diet/Allergies Section
                    DetailItem(label = stringResource(R.string.label_diet), value = animal.diet)
                    DetailItem(label = stringResource(R.string.label_allergies), value = animal.allergies)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Medical History Section
                    Text(stringResource(R.string.section_title_medical_history), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    if (uiState.medicalHistoryEvents.isEmpty()) {
                        Text(stringResource(R.string.no_medical_events), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Using a simple Column here because LazyColumn inside verticalScroll can be problematic
                        // If many events, consider a fixed height LazyColumn or different UI pattern.
                        Column {
                            uiState.medicalHistoryEvents.forEach { event ->
                                EventListItem(event = event)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // General Notes Section
                    DetailItem(label = stringResource(R.string.label_general_notes), value = animal.generalNotes)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Other Events Section
                    Text(stringResource(R.string.section_title_other_events), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    if (uiState.otherEvents.isEmpty()) {
                        Text(stringResource(R.string.no_other_events), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column {
                            uiState.otherEvents.forEach { event ->
                                EventListItem(event = event)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Padding at the end of scroll
                }
            }
        }
    }
}

// Preview for AnimalDetailsScreen
@Preview(showBackground = true, name = "Animal Details Screen - Loaded")
@Composable
fun AnimalDetailsScreenPreview_Loaded() {
    PetManagerTheme {
        val sampleAnimal = Animal(animalId = 1, name = "Biscotte", species = "Chat", breed = "Européen", birthDate = LocalDate.now().minusYears(3), sex = "Femelle", photoUri = null, chipId = "123456789012345", diet = "Croquettes spéciales", allergies = "Pollen", generalNotes = "Aime les câlins, craint l'aspirateur.")
        val sampleState = AnimalDetailsUiState(
            isLoading = false,
            animal = sampleAnimal,
            age = "3 ans",
            latestWeight = null,
            medicalHistoryEvents = emptyList(),
            otherEvents = emptyList(),
            errorMessage = null
        )
        // We don't pass animalId to the screen directly anymore for preview
        // The ViewModel is responsible for loading via SavedStateHandle
        // For previewing, we'd typically mock the ViewModel or pass state directly to Content
        AnimalDetailsContent(uiState = sampleState)
    }
}

@Preview(showBackground = true, name = "Animal Details Screen - Loading")
@Composable
fun AnimalDetailsScreenPreview_Loading() {
    PetManagerTheme {
        AnimalDetailsContent(uiState = AnimalDetailsUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Animal Details Screen - Error")
@Composable
fun AnimalDetailsScreenPreview_Error() {
    PetManagerTheme {
        AnimalDetailsContent(uiState = AnimalDetailsUiState(isLoading = false, errorMessage = "Impossible de charger les données."))
    }
}

// For string resources, add to your strings.xml:
// <string name="not_specified">Non renseigné</string>
// <string name="animal_details_title_default">Détails de l'animal</string>
// <string name="action_back">Retour</string>
// <string name="action_edit">Modifier</string>
// <string name="animal_not_found">Animal non trouvé.</string>
// <string name="animal_photo_description">Photo de %1$s</string>
// <string name="animal_photo_placeholder_description">Aucune photo disponible pour %1$s</string>
// <string name="label_name">Nom</string>
// <string name="label_species">Espèce</string>
// <string name="label_breed">Race</string>
// <string name="label_sex">Sexe</string>
// <string name="label_age_birthdate">Âge / Date de naissance</string>
// <string name="label_chip_id">Numéro d'identification (Puce)</string>
// <string name="label_latest_weight">Dernier poids connu</string>
// <string name="label_diet">Régime alimentaire</string>
// <string name="label_allergies">Allergies</string>
// <string name="section_title_medical_history">Historique Médical</string>
// <string name="no_medical_events">Aucun événement médical enregistré.</string>
// <string name="label_general_notes">Notes Générales</string>
// <string name="section_title_other_events">Autres Événements</string>
// <string name="no_other_events">Aucun autre événement.</string>
