package com.example.petmanager.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake // Anniversaire
import androidx.compose.material.icons.filled.Event // Autre
import androidx.compose.material.icons.filled.LocalHospital // RDV_VETO
import androidx.compose.material.icons.filled.Medication // TRAITEMENT
import androidx.compose.material.icons.filled.Vaccines // VACCIN
import androidx.compose.material.icons.filled.ContentCut // Grooming
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petmanager.R // Assuming R class is generated
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItemCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventTypeDetails = getEventTypeDetails(eventType = event.eventType)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize(), // Ensure minimum touch target
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Subtle elevation
        shape = MaterialTheme.shapes.medium, // Consistent M3 shape
        colors = CardDefaults.cardColors(
            containerColor = eventTypeDetails.backgroundColor.copy(alpha = 0.15f) // Slightly more pronounced tint
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = eventTypeDetails.icon,
                // TODO: Use string resource R.string.event_type_icon_description
                contentDescription = stringResource(id = R.string.event_type_icon_description_placeholder, eventTypeDetails.displayName),
                tint = eventTypeDetails.iconColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium, // Adjusted typography
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Explicit color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    // TODO: Use string resource R.string.event_time_label_prefix
                    text = stringResource(id = R.string.event_time_label_prefix_placeholder) + " ${event.dateTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium, // Adjusted typography
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color
                )
                if (!event.description.isNullOrBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

data class EventTypeVisuals(
    val displayName: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
fun getEventTypeDetails(eventType: String): EventTypeVisuals {
    val currentScheme = MaterialTheme.colorScheme
    return when (eventType.uppercase()) {
        "VACCIN" -> EventTypeVisuals("Vaccination", Icons.Filled.Vaccines, currentScheme.primary, currentScheme.primaryContainer)
        "TRAITEMENT" -> EventTypeVisuals("Traitement", Icons.Filled.Medication, currentScheme.secondary, currentScheme.secondaryContainer)
        "RDV_VETO" -> EventTypeVisuals("Rdv Vétérinaire", Icons.Filled.LocalHospital, currentScheme.error, currentScheme.errorContainer)
        "ANNIVERSAIRE" -> EventTypeVisuals("Anniversaire", Icons.Filled.Cake, Color(0xFF7B1FA2), Color(0xFFE1BEE7)) // Custom Purple
        "GROOMING" -> EventTypeVisuals("Toilettage", Icons.Filled.ContentCut, Color(0xFF0288D1), Color(0xFFB3E5FC)) // Custom Blue
        else -> EventTypeVisuals(eventType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, Icons.Filled.Event, currentScheme.tertiary, currentScheme.tertiaryContainer)
    }
}

@Preview(showBackground = true, name = "Event Item Card - Vaccin")
@Composable
fun EventItemCardPreview_Vaccin() {
    PetManagerTheme {
        val sampleEvent = Event(
            eventId = 1,
            animalId = 1,
            title = "Rappel Vaccin CHP",
            dateTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(30),
            eventType = "VACCIN",
            description = "Chez Dr. Leblanc. Ne pas oublier carnet de santé.",
            contactId = null
        )
        EventItemCard(event = sampleEvent, onClick = {})
    }
}

@Preview(showBackground = true, name = "Event Item Card - Anniversaire Dark")
@Composable
fun EventItemCardPreview_BirthdayDark() {
    PetManagerTheme(darkTheme = true) {
        val sampleEvent = Event(
            eventId = 2,
            animalId = 2,
            title = "Anniversaire de Mistigri !",
            dateTime = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0),
            eventType = "ANNIVERSAIRE",
            description = "Préparer le gâteau au thon.",
            contactId = null
        )
        EventItemCard(event = sampleEvent, onClick = {})
    }
}

@Preview(showBackground = true, name = "Event Item Card - Grooming Type")
@Composable
fun EventItemCardPreview_Grooming() {
    PetManagerTheme {
        val sampleEvent = Event(
            eventId = 3,
            animalId = 1,
            title = "Toilettage",
            dateTime = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0),
            eventType = "GROOMING", // Custom type
            description = "Coupe d'été.",
            contactId = null
        )
        EventItemCard(event = sampleEvent, onClick = {})
    }
}

// Placeholder string resource IDs used:
// R.string.event_type_icon_description_placeholder
// R.string.event_time_label_prefix_placeholder
