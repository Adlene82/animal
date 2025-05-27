package com.example.petmanager.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val eventsByDate: Map<LocalDate, List<Event>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateEvents: List<Event> = emptyList(),
    val daysInMonthWithPadding: List<CalendarDay> = emptyList(),
    val errorMessage: String? = null
)

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val hasEvents: Boolean
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var loadEventsJob: Job? = null

    init {
        val initialMonth = YearMonth.now()
        val initialSelectedDate = LocalDate.now()
        _uiState.update {
            it.copy(
                currentMonth = initialMonth,
                selectedDate = initialSelectedDate
            )
        }
        loadEventsForMonth(initialMonth, initialSelectedDate)
    }

    private fun loadEventsForMonth(yearMonth: YearMonth, newSelectedDate: LocalDate? = null) {
        loadEventsJob?.cancel() // Cancel previous loading job if any
        loadEventsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allEvents = petRepository.getAllEvents().firstOrNull() ?: emptyList()

                val firstDayOfMonth = yearMonth.atDay(1)
                val lastDayOfMonth = yearMonth.atEndOfMonth()

                val monthEvents = allEvents.filter { event ->
                    val eventDate = event.dateTime.toLocalDate()
                    !eventDate.isBefore(firstDayOfMonth) && !eventDate.isAfter(lastDayOfMonth)
                }

                val eventsByDateMap = monthEvents.groupBy { it.dateTime.toLocalDate() }

                val finalSelectedDate = newSelectedDate ?: _uiState.value.selectedDate.let {
                    // Adjust selected date if it's not in the new month, default to 1st day
                    if (it.year == yearMonth.year && it.month == yearMonth.month) it else yearMonth.atDay(1)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentMonth = yearMonth,
                        eventsByDate = eventsByDateMap,
                        selectedDate = finalSelectedDate,
                        selectedDateEvents = eventsByDateMap[finalSelectedDate] ?: emptyList(),
                        daysInMonthWithPadding = generateCalendarDays(yearMonth, eventsByDateMap),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur chargement événements: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun generateCalendarDays(yearMonth: YearMonth, events: Map<LocalDate, List<Event>>): List<CalendarDay> {
        val firstOfMonth = yearMonth.atDay(1)
        val lastOfMonth = yearMonth.atEndOfMonth()

        // Days from previous month to fill the first week
        val daysFromPrevMonth = mutableListOf<CalendarDay>()
        var current = firstOfMonth
        while (current.dayOfWeek != DayOfWeek.MONDAY) { // Assuming week starts on Monday
            current = current.minusDays(1)
            daysFromPrevMonth.add(0, CalendarDay(current, false, events.containsKey(current)))
        }

        // Days of the current month
        val daysInCurrentMonth = (1..lastOfMonth.dayOfMonth).map { day ->
            val date = yearMonth.atDay(day)
            CalendarDay(date, true, events.containsKey(date))
        }

        // Days from next month to fill the last week
        val daysFromNextMonth = mutableListOf<CalendarDay>()
        current = lastOfMonth
        // Grid usually has 6 rows * 7 columns = 42 cells
        val totalCells = 42 // Fixed size for simplicity, can be dynamic
        val cellsToFill = totalCells - (daysFromPrevMonth.size + daysInCurrentMonth.size)

        for (i in 1..cellsToFill) {
            current = current.plusDays(1)
            if (current.dayOfWeek == DayOfWeek.MONDAY && daysFromNextMonth.isNotEmpty()) break // Avoid filling more than needed if week already starts
            daysFromNextMonth.add(CalendarDay(current, false, events.containsKey(current)))
            if (daysFromNextMonth.size >= 7 && current.dayOfWeek == DayOfWeek.SUNDAY) break // Stop if a full week is filled
        }
        // Ensure total cells if grid is fixed size, or adjust logic for dynamic rows
        val allDays = daysFromPrevMonth + daysInCurrentMonth + daysFromNextMonth
        // If the grid is strictly 6 rows (42 days), pad further if needed.
        // For simplicity, this might result in less than 42 days if the month + padding fits in 5 rows.
        return allDays
    }


    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedDateEvents = it.eventsByDate[date] ?: emptyList()
            )
        }
    }

    fun onNextMonth() {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        loadEventsForMonth(nextMonth)
    }

    fun onPreviousMonth() {
        val prevMonth = _uiState.value.currentMonth.minusMonths(1)
        loadEventsForMonth(prevMonth)
    }
}
