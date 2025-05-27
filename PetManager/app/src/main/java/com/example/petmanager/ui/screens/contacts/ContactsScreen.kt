package com.example.petmanager.ui.screens.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts // For BottomNavBar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.BottomNavItem // Assuming BottomNavItem is in a shared location
import com.example.petmanager.ui.theme.PetManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // For Bottom Navigation Bar
    val bottomNavItems = listOf(
        BottomNavItem("Accueil", Icons.Filled.Home, Screen.Dashboard),
        BottomNavItem("Calendrier", Icons.Filled.CalendarMonth, Screen.Calendar),
        BottomNavItem("Réserve", Icons.Filled.Inventory, Screen.FoodStock),
        BottomNavItem("Contacts", Icons.Filled.Contacts, Screen.Contacts)
    )
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val selectedBottomNavItem = remember(currentRoute) {
        bottomNavItems.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_contacts)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditContact.createRoute(null))
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add_contact))
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedBottomNavItem == index,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContactsScreenContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onContactClick = { contactId ->
                viewModel.onContactClicked(contactId) // For logging or other VM logic
                navController.navigate(Screen.ContactDetails.createRoute(contactId))
            }
        )
    }
}

@Composable
fun ContactsScreenContent(
    modifier: Modifier = Modifier,
    uiState: ContactsUiState,
    onContactClick: (Long) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.contacts.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_contacts_list),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Slightly less space than FoodStock
            ) {
                items(uiState.contacts, key = { it.contactId }) { contact ->
                    ContactItemCard(contact = contact, onClick = { onContactClick(contact.contactId) })
                }
            }
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Contacts Screen - Loaded")
@Composable
fun ContactsScreenPreview_Loaded() {
    PetManagerTheme {
        val sampleContacts = listOf(
            Contact(1, "Dr. Alice Vétérinaire", "Vétérinaire", "Canin", "123-456-7890", "alice@vet.com", "123 Rue des Animaux", "www.alicevet.com", true, "Très douce avec les animaux."),
            Contact(2, "Bob le Toiletteur", "Toiletteur", "Toutes races", "987-654-3210", null, "456 Avenue du Poil Soyeux", null, false, null)
        )
        ContactsScreenContent(uiState = ContactsUiState(isLoading = false, contacts = sampleContacts), onContactClick = {})
    }
}

@Preview(showBackground = true, name = "Contacts Screen - Empty")
@Composable
fun ContactsScreenPreview_Empty() {
    PetManagerTheme {
        ContactsScreenContent(uiState = ContactsUiState(isLoading = false, contacts = emptyList()), onContactClick = {})
    }
}

// String resources for preview and actual use:
// <string name="title_contacts">Contacts</string>
// <string name="action_add_contact">Ajouter un contact</string>
// <string name="empty_contacts_list">Aucun contact ajouté. Cliquez sur + pour commencer.</string>
// (BottomNavItem data class needs to be accessible, e.g. moved to ui.screens or ui.common)
// For preview, I've assumed BottomNavItem is accessible. If not, a local version might be needed for preview.
