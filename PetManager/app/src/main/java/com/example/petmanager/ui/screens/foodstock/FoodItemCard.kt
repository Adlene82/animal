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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // Assuming R class and ic_placeholder_food exist
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
    val df = DecimalFormat("#.#")

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Consistent elevation
        border = BorderStroke(1.dp, cardBorderColor),
        shape = MaterialTheme.shapes.medium // Consistent M3 shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Consistent padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium) // Consistent shape
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Neutral placeholder background
                contentAlignment = Alignment.Center
            ) {
                if (foodItem.photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(foodItem.photoUri)
                            .crossfade(true)
                            .build(),
                        // TODO: Use string resource R.string.food_item_photo_description
                        contentDescription = stringResource(id = R.string.food_item_photo_description_placeholder, foodItem.name),
                        placeholder = painterResource(id = R.drawable.ic_placeholder_food),
                        error = painterResource(id = R.drawable.ic_placeholder_food),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        // TODO: Use string resource R.string.food_item_photo_placeholder
                        contentDescription = stringResource(id = R.string.food_item_photo_placeholder_placeholder, foodItem.name),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Themed color
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp)) // Consistent spacing

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Consistent internal spacing
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleMedium, // M3 Typography
                        fontWeight = FontWeight.Bold, // Keep bold for emphasis
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.onSurface // Explicit color
                    )
                    if (foodItem.lowStock) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Warning,
                            // TODO: Use string resource R.string.low_stock_alert_icon_description
                            contentDescription = stringResource(id = R.string.low_stock_alert_icon_description_placeholder),
                            tint = MaterialTheme.colorScheme.error, // Consistent error color
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                foodItem.brand?.takeIf { it.isNotBlank() }?.let { // Show brand only if present
                    Text(
                        // TODO: Use string resource R.string.food_item_brand_label
                        text = stringResource(id = R.string.food_item_brand_label_placeholder, it),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color
                    )
                }
                Text(
                    // TODO: Use string resource R.string.food_item_type_label
                    text = stringResource(id = R.string.food_item_type_label_placeholder, foodItem.type),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    // TODO: Use string resource R.string.food_item_stock_label
                    text = stringResource(
                        id = R.string.food_item_stock_label_placeholder,
                        df.format(foodItem.currentQuantity),
                        df.format(foodItem.initialQuantity),
                        foodItem.quantityUnit
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Primary info color
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { foodItem.stockProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (foodItem.lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, // Consistent color logic
                    trackColor = MaterialTheme.colorScheme.surfaceVariant // M3 track color
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

// Placeholder string resource IDs used:
// R.string.food_item_photo_description_placeholder
// R.string.food_item_photo_placeholder_placeholder
// R.string.low_stock_alert_icon_description_placeholder
// R.string.food_item_brand_label_placeholder
// R.string.food_item_type_label_placeholder
// R.string.food_item_stock_label_placeholder
