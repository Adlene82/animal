package com.example.petmanager.ui.screens.foodstock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood // Placeholder for food
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // For placeholder drawable
import com.example.petmanager.ui.theme.PetManagerTheme
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemCard(
    foodItem: FoodItemDisplay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorderColor = if (foodItem.lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
    val df = DecimalFormat("#.#") // To format quantity nicely

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (foodItem.photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(foodItem.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo of ${foodItem.name}",
                        placeholder = painterResource(id = R.drawable.ic_placeholder_food),
                        error = painterResource(id = R.drawable.ic_placeholder_food),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        contentDescription = "No photo available for ${foodItem.name}",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f), // Take remaining space
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false) // Prevent text from pushing icon
                    )
                    if (foodItem.lowStock) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Low stock alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (foodItem.brand != null) {
                    Text(
                        text = "Marque: ${foodItem.brand}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Type: ${foodItem.type}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stock: ${df.format(foodItem.currentQuantity)} / ${df.format(foodItem.initialQuantity)} ${foodItem.quantityUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { foodItem.stockProgress }, // State-based progress
                    modifier = Modifier.fillMaxWidth(),
                    color = if (foodItem.lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Food Item Card - Normal Stock")
@Composable
fun FoodItemCardPreview_Normal() {
    PetManagerTheme {
        val sampleFood = FoodItemDisplay(
            id = 1L,
            name = "Croquettes Super Premium Long Nom Qui Dépasse",
            brand = "Royal Canin",
            type = "Chien Adulte",
            currentQuantity = 8.5,
            initialQuantity = 10.0,
            quantityUnit = "kg",
            photoUri = null,
            lowStock = false,
            stockProgress = 0.85f
        )
        FoodItemCard(foodItem = sampleFood, onClick = {})
    }
}

@Preview(showBackground = true, name = "Food Item Card - Low Stock")
@Composable
fun FoodItemCardPreview_LowStock() {
    PetManagerTheme {
        val sampleFoodLow = FoodItemDisplay(
            id = 2L,
            name = "Pâtée Gourmande",
            brand = "Gourmet Gold",
            type = "Chat Stérilisé",
            currentQuantity = 0.3,
            initialQuantity = 1.2,
            quantityUnit = "kg",
            photoUri = null,
            lowStock = true,
            stockProgress = 0.25f
        )
        FoodItemCard(foodItem = sampleFoodLow, onClick = {})
    }
}

@Preview(showBackground = true, name = "Food Item Card - Dark Theme Low Stock")
@Composable
fun FoodItemCardPreview_DarkLowStock() {
    PetManagerTheme(darkTheme = true) {
        val sampleFoodLow = FoodItemDisplay(
            id = 2L,
            name = "Pâtée Gourmande Sombre",
            brand = "Gourmet Gold",
            type = "Chat Stérilisé",
            currentQuantity = 0.3,
            initialQuantity = 1.2,
            quantityUnit = "kg",
            photoUri = null,
            lowStock = true,
            stockProgress = 0.25f
        )
        FoodItemCard(foodItem = sampleFoodLow, onClick = {})
    }
}

// Placeholder for R.drawable.ic_placeholder_food - needs to be created
// Example: res/drawable/ic_placeholder_food.xml
// <vector xmlns:android="http://schemas.android.com/apk/res/android"
//     android:width="24dp"
//     android:height="24dp"
//     android:viewportWidth="24"
//     android:viewportHeight="24"
//     android:tint="?attr/colorControlNormal">
//   <path
//       android:fillColor="@android:color/white"
//       android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM11,17L7,17v-2h4v2zM11,13L7,13v-2h4v2zM11,9L7,9V7h4V9zM17,17h-4v-2h4v2zM17,13h-4v-2h4v2zM17,9h-4V7h4V9z"/>
// </vector>
// For a more relevant icon, consider using one from Material Symbols or a custom SVG.
// For this example, Icons.Filled.Fastfood is used as a fallback in the card.
