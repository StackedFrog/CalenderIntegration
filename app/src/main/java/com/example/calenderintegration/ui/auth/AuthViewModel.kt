package com.example.calenderintegration.ui.auth

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun logIn(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authRepository.logIn(context, startIntentSender)
                _authState.update { it.copy(isLoggedIn = response != null) }
            } catch (e: Exception) {
                _authState.update { it.copy(error = e.message) }
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /**
     * Sends user back to the login screen without clearing saved accounts.
     * Used when pressing "Add Account" to add another Google account.
     */
    fun goToLoginScreen() {
        _authState.update { it.copy(isLoggedIn = false) }
    }
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)

