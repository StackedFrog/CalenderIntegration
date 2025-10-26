package com.example.calenderintegration.ui.auth

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var currentAccount: GoogleAccount? = null

    /** Called at app startup to restore any saved login session. */
    fun initialize(context: Context) {
        val savedAccounts = authRepository.loadSavedAccounts(context)
        currentAccount = savedAccounts.firstOrNull()

        _authState.update {
            it.copy(
                isLoggedIn = currentAccount != null,
                isInitialized = true
            )
        }
    }

    /** Handles interactive Google Sign-In flow. */
    fun logIn(context: Context, startIntentSender: (IntentSenderRequest) -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authRepository.logIn(context, startIntentSender)
                currentAccount = result
                _authState.update {
                    it.copy(
                        isLoggedIn = result != null,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    /** Clears session and logs out the active account. */
    fun logOut(context: Context) {
        viewModelScope.launch {
            authRepository.logOut(context, currentAccount)
            currentAccount = null
            _authState.update { it.copy(isLoggedIn = false) }
        }
    }

    fun isLoggedIn(): Boolean = _authState.value.isLoggedIn

    fun goToLoginScreen() {
        _authState.update { it.copy(isLoggedIn = false) }
    }
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)
