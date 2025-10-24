package com.example.calenderintegration.ui.eventView

import androidx.lifecycle.ViewModel
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repo: EventRepository
): ViewModel() {
    private val _eventState = MutableStateFlow(EventState())
    val eventState: StateFlow<EventState> = _eventState

    fun getEventById(id: String) {
        // update state by doing a repo call to get event by id
    }
}

data class EventState (
    val event: Event? = null,
    val error: String? = null
)

