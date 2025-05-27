package com.example.petmanager.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business // For category/address
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonPin // Placeholder for contact image or avatar
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star // For favorite
import androidx.compose.material.icons.filled.Work // For specialty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItemCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PersonPin, // Replace with actual image/avatar if available
                contentDescription = "Contact Icon",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (contact.isFavorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Favorite Contact",
                            tint = MaterialTheme.colorScheme.tertiary, // Or a gold/yellow color
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = contact.category,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                contact.specialty?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(icon = Icons.Filled.Work, text = it)
                }
                contact.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(icon = Icons.Filled.Phone, text = it)
                }
                contact.emailAddress?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(icon = Icons.Filled.Email, text = it)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Contact Item Card - Favorite")
@Composable
fun ContactItemCardPreview_Favorite() {
    PetManagerTheme {
        val sampleContact = Contact(
            contactId = 1,
            name = "Dr. Alice Vétérinaire Très Long Nom Pour Tester Ellipsis",
            category = "Vétérinaire",
            specialty = "Chirurgie et Dentisterie Canine et Féline",
            phoneNumber = "01 23 45 67 89",
            emailAddress = "alice.veterinaire.longue.adresse.email@example.com",
            address = "123 Rue des Animaux Heureux, 75000 Paris",
            website = "www.alicevet.com",
            isFavorite = true,
            notes = "Très douce avec les animaux."
        )
        ContactItemCard(contact = sampleContact, onClick = {})
    }
}

@Preview(showBackground = true, name = "Contact Item Card - Not Favorite")
@Composable
fun ContactItemCardPreview_NotFavorite() {
    PetManagerTheme {
        val sampleContact = Contact(
            contactId = 2,
            name = "Bob le Toiletteur",
            category = "Toiletteur",
            specialty = "Toutes races, chiens et chats",
            phoneNumber = "09 87 65 43 21",
            emailAddress = null,
            address = "456 Avenue du Poil Soyeux",
            website = null,
            isFavorite = false,
            notes = null
        )
        ContactItemCard(contact = sampleContact, onClick = {})
    }
}

@Preview(showBackground = true, name = "Contact Item Card - Dark Theme")
@Composable
fun ContactItemCardPreview_Dark() {
    PetManagerTheme(darkTheme = true) {
        val sampleContact = Contact(
            contactId = 1,
            name = "Clinique SombreVet",
            category = "Vétérinaire",
            specialty = "Urgences Nocturnes",
            phoneNumber = "112",
            emailAddress = "urgence@sombrevet.com",
            address = "Quelque part dans la nuit",
            website = null,
            isFavorite = true,
            notes = "Ouvert 24/7"
        )
        ContactItemCard(contact = sampleContact, onClick = {})
    }
}
