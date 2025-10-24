package com.example.calenderintegration.ui.auth

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import com.example.calenderintegration.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState
    suspend fun logIn(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ) {
        try {
            val response = authRepository.logIn(
                context = context,
                startIntentSender = startIntentSender
            )
            if (response != null) {
                _authState.update { it.copy(isLoggedIn = true) }
            } else {
                _authState.update { it.copy(isLoggedIn = false) }
            }
        } catch (e: Exception) {
            _authState.update { it.copy(error = e.message) }
        }
    }

    fun isLoggedIn(): Boolean {
        return authState.value.isLoggedIn
    }
}

data class AuthState (
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)