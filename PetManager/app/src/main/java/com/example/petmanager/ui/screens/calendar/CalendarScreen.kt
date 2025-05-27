package com.example.petmanager.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth // For BottomNavBar
import androidx.compose.material.icons.filled.Contacts // For BottomNavBar
import androidx.compose.material.icons.filled.Home // For BottomNavBar
import androidx.compose.material.icons.filled.Inventory // For BottomNavBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.BottomNavItem // Assuming BottomNavItem is in a shared location
import com.example.petmanager.ui.theme.PetManagerTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle // Alias to avoid conflict
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
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
                title = { Text(stringResource(R.string.title_calendar)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditEvent.createRoute(null))
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add_event))
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
        CalendarScreenContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onDateSelected = viewModel::onDateSelected,
            onNextMonth = viewModel::onNextMonth,
            onPreviousMonth = viewModel::onPreviousMonth,
            onEventClick = { eventId ->
                navController.navigate(Screen.AddEditEvent.createRoute(eventId))
            }
        )
    }
}

@Composable
fun CalendarScreenContent(
    modifier: Modifier = Modifier,
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onEventClick: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        MonthSelector(
            currentMonth = uiState.currentMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            modifier = Modifier.padding(16.dp)
        )

        DaysOfWeekHeader(modifier = Modifier.padding(horizontal = 16.dp))

        CalendarGrid(
            days = uiState.daysInMonthWithPadding,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        Text(
            text = stringResource(R.string.events_for_date, uiState.selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else if (uiState.selectedDateEvents.isEmpty()) {
            Text(
                stringResource(R.string.no_events_for_date),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.selectedDateEvents, key = { it.eventId }) { event ->
                    EventItemCard(event = event, onClick = { onEventClick(event.eventId) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.action_previous_month))
        }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.action_next_month))
        }
    }
}

@Composable
fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        val daysOfWeek = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim") // Adjust for locale
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(days, key = { it.date.toEpochDay() }) { day ->
            CalendarDayCell(
                day = day,
                isSelected = day.date == selectedDate,
                onClick = { onDateSelected(day.date) }
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cellColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> Color.Transparent // Or MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Make cells square
            .clip(MaterialTheme.shapes.small)
            .background(cellColor)
            .clickable(enabled = day.isCurrentMonth || isSelected) { onClick() } // Allow clicking selected day even if not current month
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || day.date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
            )
            if (day.hasEvents && day.isCurrentMonth) { // Show dot only for current month days with events
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Calendar Screen - Loaded")
@Composable
fun CalendarScreenPreview_Loaded() {
    PetManagerTheme {
        val sampleEvents = mapOf(
            LocalDate.now().plusDays(2) to listOf(Event(eventId = 1, animalId = 1, title = "Vaccin", dateTime = LocalDate.now().plusDays(2).atTime(10,0), eventType = "RDV_VETO", contactId = null))
        )
        val days = (1..30).map { CalendarDay(LocalDate.now().withDayOfMonth(it.coerceIn(1,28)), true, sampleEvents.containsKey(LocalDate.now().withDayOfMonth(it.coerceIn(1,28)))) }

        CalendarScreenContent(
            uiState = CalendarUiState(
                isLoading = false,
                currentMonth = YearMonth.now(),
                eventsByDate = sampleEvents,
                selectedDate = LocalDate.now().plusDays(2),
                selectedDateEvents = sampleEvents[LocalDate.now().plusDays(2)] ?: emptyList(),
                daysInMonthWithPadding = days
            ),
            onDateSelected = {}, onNextMonth = {}, onPreviousMonth = {}, onEventClick = {}
        )
    }
}

// Required String resources:
// <string name="title_calendar">Calendrier</string>
// <string name="action_add_event">Ajouter un événement</string>
// <string name="action_previous_month">Mois précédent</string>
// <string name="action_next_month">Mois suivant</string>
// <string name="events_for_date">Événements pour le %1$s</string>
// <string name="no_events_for_date">Aucun événement pour cette date.</string>
// (BottomNav item labels like "Accueil", "Calendrier", "Réserve", "Contacts" are assumed from DashboardScreen)
// (BottomNavItem data class needs to be accessible, e.g. moved to ui.screens or ui.common)
// For preview, I've assumed BottomNavItem is accessible. If not, FoodStockScreen's BottomNavItem will need to be moved to a common location.
// For now, I'll create a local BottomNavItem in CalendarScreen.kt for preview to work if it's not shared.

// Local BottomNavItem for Preview if not shared:
// data class BottomNavItem(val label: String, val icon: ImageVector, val screen: Screen)
