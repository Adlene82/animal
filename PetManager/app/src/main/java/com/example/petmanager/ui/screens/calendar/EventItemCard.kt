package com.example.petmanager.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake // Anniversaire
import androidx.compose.material.icons.filled.Event // Autre
import androidx.compose.material.icons.filled.LocalHospital // RDV_VETO
import androidx.compose.material.icons.filled.Medication // TRAITEMENT
import androidx.compose.material.icons.filled.Vaccines // VACCIN
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val eventTypeDetails = getEventTypeDetails(event.eventType)
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = eventTypeDetails.backgroundColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = eventTypeDetails.icon,
                contentDescription = "Event type: ${event.eventType}",
                tint = eventTypeDetails.iconColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.dateTime.format(formatter)} - ${eventTypeDetails.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!event.description.isNullOrBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val backgroundColor: Color // For card background tint or indicator
)

@Composable
fun getEventTypeDetails(eventType: String): EventTypeVisuals {
    return when (eventType.uppercase()) {
        "VACCIN" -> EventTypeVisuals("Vaccination", Icons.Filled.Vaccines, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        "TRAITEMENT" -> EventTypeVisuals("Traitement", Icons.Filled.Medication, Color(0xFFFFA000), Color(0xFFFFE0B2)) // Orange
        "RDV_VETO" -> EventTypeVisuals("Rdv Vétérinaire", Icons.Filled.LocalHospital, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
        "ANNIVERSAIRE" -> EventTypeVisuals("Anniversaire", Icons.Filled.Cake, Color(0xFF7B1FA2), Color(0xFFE1BEE7)) // Purple
        else -> EventTypeVisuals(eventType.replaceFirstChar { it.titlecase() }, Icons.Filled.Event, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
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

@Preview(showBackground = true, name = "Event Item Card - Default Type")
@Composable
fun EventItemCardPreview_Default() {
    PetManagerTheme {
        val sampleEvent = Event(
            eventId = 3,
            animalId = 1,
            title = "Toilettage",
            dateTime = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0),
            eventType = "Toilettage", // Custom type
            description = "Coupe d'été.",
            contactId = null
        )
        EventItemCard(event = sampleEvent, onClick = {})
    }
}
