package com.example.petmanager.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake // For birthday
import androidx.compose.material.icons.filled.Pets // Placeholder for animal
import androidx.compose.material.icons.filled.Vaccines // For vaccine status
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCard(
    animalItem: AnimalListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp), // Fixed height for consistency, good for touch target
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // M3 recommends lower elevation
        shape = MaterialTheme.shapes.medium // Consistent shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Consistent padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animal Image
            Box(
                modifier = Modifier
                    .size(104.dp) // Maintains square image, good size
                    .clip(MaterialTheme.shapes.medium) // Consistent shape
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Neutral background for placeholder
                contentAlignment = Alignment.Center
            ) {
                if (animalItem.photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(animalItem.photoUri)
                            .crossfade(true)
                            .build(),
                        // TODO: Use string resource R.string.animal_photo_description
                        contentDescription = stringResource(R.string.animal_photo_description, animalItem.name),
                        placeholder = painterResource(id = R.drawable.ic_placeholder_pet),
                        error = painterResource(id = R.drawable.ic_placeholder_pet),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        // TODO: Use string resource R.string.animal_photo_placeholder_description
                        contentDescription = stringResource(R.string.animal_photo_placeholder_description, animalItem.name),
                        modifier = Modifier.size(48.dp), // Good icon size
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Consistent color
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp)) // Consistent spacing

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Consistent spacing
            ) {
                Text(
                    text = animalItem.name,
                    style = MaterialTheme.typography.titleMedium, // Adjusted from Large for card context
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Explicit color
                )
                Text(
                    text = if (animalItem.breed != null) "${animalItem.species} - ${animalItem.breed}" else animalItem.species,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color for secondary info
                )
                Text(
                    text = animalItem.age,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                animalItem.quickStatus?.let { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp) // Add a bit of space before status
                    ) {
                        val icon: ImageVector = when {
                            status.contains("Anniversaire", ignoreCase = true) -> Icons.Filled.Cake
                            status.contains("vaccin", ignoreCase = true) -> Icons.Filled.Vaccines
                            else -> Icons.Filled.Pets
                        }
                        Icon(
                            imageVector = icon,
                            // TODO: Use string resource R.string.animal_status_icon_description
                            contentDescription = stringResource(R.string.animal_status_icon_description),
                            modifier = Modifier.size(16.dp), // Appropriate size for label icon
                            tint = MaterialTheme.colorScheme.secondary // Use a theme color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Animal Card Preview")
@Composable
fun AnimalCardPreview() {
    PetManagerTheme {
        val sampleAnimal = AnimalListItem(
            id = 1L,
            name = "Biscotte le Magnifique Extrêmement Long Nom",
            species = "Chat",
            breed = "Européen à Poil Court et Doux",
            age = "3 ans et 2 mois",
            photoUri = null, // "https://example.com/biscotte.jpg" - Coil won't load this in preview easily
            quickStatus = "🎂 Anniversaire !"
        )
        AnimalCard(animalItem = sampleAnimal, onClick = {})
    }
}

@Preview(showBackground = true, name = "Animal Card Without Breed/Status Preview")
@Composable
fun AnimalCardNoBreedStatusPreview() {
    PetManagerTheme {
        val sampleAnimal = AnimalListItem(
            id = 2L,
            name = "Rex",
            species = "Chien",
            breed = null,
            age = "1 an",
            photoUri = null,
            quickStatus = null
        )
        AnimalCard(animalItem = sampleAnimal, onClick = {})
    }
}

@Preview(showBackground = true, name = "Animal Card Dark Theme")
@Composable
fun AnimalCardDarkThemePreview() {
    PetManagerTheme(darkTheme = true) {
        val sampleAnimal = AnimalListItem(
            id = 1L,
            name = "Noctua",
            species = "Hibou",
            breed = "Grand Duc",
            age = "10 ans",
            photoUri = null,
            quickStatus = "Prochain vaccin: Demain"
        )
        AnimalCard(animalItem = sampleAnimal, onClick = {})
    }
}
