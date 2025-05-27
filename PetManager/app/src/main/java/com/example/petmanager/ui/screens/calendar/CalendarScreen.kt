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
import com.example.petmanager.R // Assuming R class is generated
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.BottomNavItem // Assuming BottomNavItem is in ui.screens
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

    val bottomNavItems = listOf(
        // TODO: Use string resources for labels R.string.bottom_nav_home, etc.
        BottomNavItem(stringResource(id = R.string.bottom_nav_home_placeholder), Icons.Filled.Home, Screen.Dashboard),
        BottomNavItem(stringResource(id = R.string.bottom_nav_calendar_placeholder), Icons.Filled.CalendarMonth, Screen.Calendar),
        BottomNavItem(stringResource(id = R.string.bottom_nav_food_stock_placeholder), Icons.Filled.Inventory, Screen.FoodStock),
        BottomNavItem(stringResource(id = R.string.bottom_nav_contacts_placeholder), Icons.Filled.Contacts, Screen.Contacts)
    )
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val selectedBottomNavItem = remember(currentRoute) {
        bottomNavItems.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // TODO: Use string resource R.string.title_calendar
                title = { Text(stringResource(id = R.string.title_calendar_placeholder), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // M3 style
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditEvent.createRoute(null))
                },
                modifier = Modifier.minimumInteractiveComponentSize() // Ensure touch target
            ) {
                Icon(
                    Icons.Filled.Add,
                    // TODO: Use string resource R.string.action_add_event
                    contentDescription = stringResource(id = R.string.action_add_event_placeholder)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }, // M3 label style
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp) // Adjusted padding
        )

        DaysOfWeekHeader(modifier = Modifier.padding(horizontal = 16.dp))

        CalendarGrid(
            days = uiState.daysInMonthWithPadding,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // Added vertical padding
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        Text(
            // TODO: Use string resource R.string.events_for_date
            text = stringResource(id = R.string.events_for_date_placeholder, uiState.selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))),
            style = MaterialTheme.typography.titleMedium, // Consistent typography
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading && uiState.eventsByDate.isEmpty()) { // Show loader only if events are truly not loaded yet
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else if (uiState.selectedDateEvents.isEmpty()) {
            Text(
                // TODO: Use string resource R.string.no_events_for_date
                stringResource(id = R.string.no_events_for_date_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Consistent spacing
            ) {
                items(uiState.selectedDateEvents, key = { it.eventId }) { event ->
                    EventItemCard(event = event, onClick = { onEventClick(event.eventId) })
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
        IconButton(onClick = onPreviousMonth, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                // TODO: Use string resource R.string.action_previous_month
                contentDescription = stringResource(id = R.string.action_previous_month_placeholder)
            )
        }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.titleLarge, // Adjusted for prominence
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextMonth, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                // TODO: Use string resource R.string.action_next_month
                contentDescription = stringResource(id = R.string.action_next_month_placeholder)
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) { // Added vertical padding
        // TODO: Potentially localize day abbreviations
        val daysOfWeek = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall, // Appropriate for header
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color
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
        verticalArrangement = Arrangement.spacedBy(6.dp), // Adjusted spacing
        horizontalArrangement = Arrangement.spacedBy(6.dp), // Adjusted spacing
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
    val cellBackgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) // More subtle for non-current month
        else -> Color.Transparent
    }
    val cellBorderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.date == LocalDate.now() && day.isCurrentMonth -> MaterialTheme.colorScheme.outline // Today's outline
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        day.date == LocalDate.now() && day.isCurrentMonth -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val eventIndicatorColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Maintain square cells
            .clip(MaterialTheme.shapes.small) // Consistent M3 shape
            .background(cellBackgroundColor)
            .border(BorderStroke(1.dp, cellBorderColor), MaterialTheme.shapes.small)
            .clickable(enabled = day.isCurrentMonth || isSelected) { onClick() }
            .minimumInteractiveComponentSize(), // Ensure touch target
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp, // Standard body size
                fontWeight = if (isSelected || (day.date == LocalDate.now() && day.isCurrentMonth)) FontWeight.Bold else FontWeight.Normal
            )
            if (day.hasEvents && day.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp) // Space between number and dot
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(eventIndicatorColor)
                )
            } else {
                 // Ensure consistent height even without dot by adding a Spacer or minHeight to Column
                Spacer(modifier = Modifier.height(8.dp)) // Adjust to match dot size + padding
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

// Placeholder string resource IDs used:
// R.string.title_calendar_placeholder
// R.string.action_add_event_placeholder
// R.string.action_previous_month_placeholder
// R.string.action_next_month_placeholder
// R.string.events_for_date_placeholder
// R.string.no_events_for_date_placeholder
// Assuming BottomNavItem placeholders are defined as in FoodStockScreen or shared.
