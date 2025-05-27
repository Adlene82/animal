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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // Assuming you have a placeholder drawable
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
            .height(120.dp), // Fixed height for consistency
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animal Image Placeholder
            Box(
                modifier = Modifier
                    .size(104.dp) // Square image
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (animalItem.photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(animalItem.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo of ${animalItem.name}",
                        placeholder = painterResource(id = R.drawable.ic_launcher_background), // Replace with your actual placeholder
                        error = painterResource(id = R.drawable.ic_launcher_background), // Replace with your error placeholder
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = "No photo available for ${animalItem.name}",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f), // Take remaining space
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = animalItem.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (animalItem.breed != null) "${animalItem.species} - ${animalItem.breed}" else animalItem.species,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = animalItem.age,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                animalItem.quickStatus?.let { status ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon: ImageVector = when {
                            status.contains("Anniversaire", ignoreCase = true) -> Icons.Filled.Cake
                            status.contains("vaccin", ignoreCase = true) -> Icons.Filled.Vaccines
                            else -> Icons.Filled.Pets // Default relevant icon
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Status icon",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium, // Changed from Small for better visibility
                            color = MaterialTheme.colorScheme.secondary, // Use a theme color
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
