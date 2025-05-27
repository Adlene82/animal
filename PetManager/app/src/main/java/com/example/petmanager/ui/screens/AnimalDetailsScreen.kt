package com.example.petmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // Assuming R class is generated and ic_placeholder_pet exists
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.animaldetails.AnimalDetailsUiState
import com.example.petmanager.ui.screens.animaldetails.AnimalDetailsViewModel
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Internal helper composables for AnimalDetailsScreen
@Composable
internal fun DetailItem(label: String, value: String?, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(modifier = modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null, // Decorative, as label describes the item
                modifier = Modifier.size(20.dp).padding(end = 12.dp, top = 3.dp), // Adjusted padding
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall, // Refined typography
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value ?: stringResource(id = R.string.not_specified_placeholder), // TODO: Add R.string.not_specified_placeholder
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun EventListItem(event: Event, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    val cardModifier = if (onClick != null) modifier.clickable(onClick = onClick).fillMaxWidth() else modifier.fillMaxWidth()

    Card(
        modifier = cardModifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium // Consistent M3 shape
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) // Refined typography
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                // TODO: Use string resource R.string.event_date_time_label_prefix
                stringResource(id = R.string.event_date_time_label_prefix_placeholder) + " ${event.dateTime.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium // Refined typography
            )
            event.location?.takeIf { it.isNotBlank() }?.let {
                 Text(
                    // TODO: Use string resource R.string.event_location_label_prefix
                    stringResource(id = R.string.event_location_label_prefix_placeholder) + " $it",
                    style = MaterialTheme.typography.bodySmall, // Refined typography
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            event.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall, // Refined typography
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Divider(modifier = Modifier.padding(vertical = 16.dp)) // Consistent spacing for dividers
}

// Main Screen Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailsScreen(
    navController: NavController,
    viewModel: AnimalDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                // TODO: Use string resource R.string.animal_details_title_default
                title = { Text(uiState.animal?.name ?: stringResource(id = R.string.animal_details_title_default_placeholder), style = MaterialTheme.typography.titleLarge) },
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
                    uiState.animal?.let { animal ->
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.AddEditAnimal.createRoute(animal.animalId))
                            },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp) // Ensure touch target
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                // TODO: Use string resource R.string.action_edit
                                contentDescription = stringResource(id = R.string.action_edit_placeholder)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant // M3 style
                )
            )
        }
    ) { innerPadding ->
        AnimalDetailsContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onEventClick = { eventId ->
                 navController.navigate(Screen.AddEditEvent.createRoute(eventId = eventId))
            }
        )
    }
}

@Composable
fun AnimalDetailsContent(
    modifier: Modifier = Modifier,
    uiState: AnimalDetailsUiState,
    onEventClick: (Long) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Consistent horizontal padding
        contentAlignment = Alignment.TopStart
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            uiState.animal == null -> {
                Text(
                    // TODO: Use string resource R.string.animal_not_found
                    text = stringResource(id = R.string.animal_not_found_placeholder),
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                val animal = uiState.animal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, bottom = 16.dp) // Consistent vertical padding
                ) {
                    // Animal Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Increased height
                            .clip(MaterialTheme.shapes.large) // M3 shape
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (animal.photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(animal.photoUri)
                                    .crossfade(true)
                                    .build(),
                                // TODO: Use string resource R.string.animal_photo_description
                                contentDescription = stringResource(id = R.string.animal_photo_description_placeholder, animal.name),
                                placeholder = painterResource(id = R.drawable.ic_placeholder_pet),
                                error = painterResource(id = R.drawable.ic_placeholder_pet),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                // TODO: Use string resource R.string.animal_photo_placeholder_description
                                contentDescription = stringResource(id = R.string.animal_photo_placeholder_description_placeholder, animal.name),
                                modifier = Modifier.size(96.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Info Section
                    // TODO: Use string resource R.string.section_title_main_info
                    Text(stringResource(id = R.string.section_title_main_info_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    // TODO: Use string resources for labels
                    DetailItem(label = stringResource(id = R.string.label_name_placeholder), value = animal.name, icon = Icons.Filled.Badge)
                    DetailItem(label = stringResource(id = R.string.label_species_placeholder), value = animal.species, icon = Icons.Filled.Category)
                    DetailItem(label = stringResource(id = R.string.label_breed_placeholder), value = animal.breed, icon = Icons.Filled.Pets)
                    DetailItem(label = stringResource(id = R.string.label_sex_placeholder), value = animal.sex, icon = if (animal.sex.equals("Mâle", true)) Icons.Filled.Male else if (animal.sex.equals("Femelle", true)) Icons.Filled.Female else Icons.Filled.Transgender)
                    DetailItem(
                        label = stringResource(id = R.string.label_age_birthdate_placeholder),
                        // TODO: Use string resource for date format
                        value = "${uiState.age ?: ""} (${animal.birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))})",
                        icon = Icons.Filled.Cake
                    )
                    DetailItem(label = stringResource(id = R.string.label_chip_id_placeholder), value = animal.chipId, icon = Icons.Filled.Memory)

                    SectionDivider()

                    // Weight/Size Section
                    // TODO: Use string resource R.string.section_title_health_info
                    Text(stringResource(id = R.string.section_title_health_info_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    // TODO: Use string resource for "as of date" R.string.as_of_date_placeholder
                    val weightText = uiState.latestWeight?.let { "${it.weightInKg} kg (${stringResource(id = R.string.as_of_date_placeholder, it.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))})" }
                    DetailItem(label = stringResource(id = R.string.label_latest_weight_placeholder), value = weightText, icon = Icons.Filled.MonitorWeight)

                    SectionDivider()

                    // Diet/Allergies Section
                    // TODO: Use string resource R.string.section_title_feeding_info
                    Text(stringResource(id = R.string.section_title_feeding_info_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    DetailItem(label = stringResource(id = R.string.label_diet_placeholder), value = animal.diet, icon = Icons.Filled.Restaurant)
                    DetailItem(label = stringResource(id = R.string.label_allergies_placeholder), value = animal.allergies, icon = Icons.Filled.NoFood)

                    SectionDivider()

                    // Medical History Section
                    // TODO: Use string resource R.string.section_title_medical_history
                    Text(stringResource(id = R.string.section_title_medical_history_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    if (uiState.medicalHistoryEvents.isEmpty()) {
                        // TODO: Use string resource R.string.no_medical_events
                        Text(stringResource(id = R.string.no_medical_events_placeholder), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp))
                    } else {
                        uiState.medicalHistoryEvents.forEach { event ->
                            EventListItem(event = event, onClick = { onEventClick(event.eventId) })
                        }
                    }

                    SectionDivider()

                    // General Notes Section
                    // TODO: Use string resource R.string.section_title_general_notes
                    Text(stringResource(id = R.string.section_title_general_notes_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    DetailItem(label = stringResource(id = R.string.label_general_notes_placeholder), value = animal.generalNotes, icon = Icons.Filled.Notes)

                    SectionDivider()

                    // Other Events Section
                    // TODO: Use string resource R.string.section_title_other_events
                    Text(stringResource(id = R.string.section_title_other_events_placeholder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    if (uiState.otherEvents.isEmpty()) {
                        // TODO: Use string resource R.string.no_other_events
                        Text(stringResource(id = R.string.no_other_events_placeholder), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp))
                    } else {
                        uiState.otherEvents.forEach { event ->
                            EventListItem(event = event, onClick = { onEventClick(event.eventId) })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


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
        AnimalDetailsContent(uiState = sampleState, onEventClick = {})
    }
}

@Preview(showBackground = true, name = "Animal Details Screen - Loading")
@Composable
fun AnimalDetailsScreenPreview_Loading() {
    PetManagerTheme {
        AnimalDetailsContent(uiState = AnimalDetailsUiState(isLoading = true), onEventClick = {})
    }
}

@Preview(showBackground = true, name = "Animal Details Screen - Error")
@Composable
fun AnimalDetailsScreenPreview_Error() {
    PetManagerTheme {
        AnimalDetailsContent(uiState = AnimalDetailsUiState(isLoading = false, errorMessage = "Impossible de charger les données."), onEventClick = {})
    }
}

// Placeholder string resource IDs used in TODOs:
// R.string.not_specified_placeholder
// R.string.event_date_time_label_prefix_placeholder
// R.string.event_location_label_prefix_placeholder
// R.string.animal_details_title_default_placeholder
// R.string.action_back_placeholder
// R.string.action_edit_placeholder
// R.string.animal_not_found_placeholder
// R.string.animal_photo_description_placeholder
// R.string.animal_photo_placeholder_description_placeholder
// R.string.section_title_main_info_placeholder
// R.string.label_name_placeholder
// R.string.label_species_placeholder
// R.string.label_breed_placeholder
// R.string.label_sex_placeholder
// R.string.label_age_birthdate_placeholder
// R.string.label_chip_id_placeholder
// R.string.section_title_health_info_placeholder
// R.string.as_of_date_placeholder
// R.string.label_latest_weight_placeholder
// R.string.section_title_feeding_info_placeholder
// R.string.label_diet_placeholder
// R.string.label_allergies_placeholder
// R.string.section_title_medical_history_placeholder
// R.string.no_medical_events_placeholder
// R.string.section_title_general_notes_placeholder
// R.string.label_general_notes_placeholder
// R.string.section_title_other_events_placeholder
// R.string.no_other_events_placeholder
