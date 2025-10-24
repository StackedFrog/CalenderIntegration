package com.example.calenderintegration.ui.accounts

import androidx.lifecycle.ViewModel
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.OutlookAccount
import com.example.calenderintegration.repository.AccountsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository
): ViewModel() {

    private val _accountsState = MutableStateFlow(AccountsState())
    val accountsState: StateFlow<AccountsState> = _accountsState

    fun loadAllAccounts() {
        try {
            // repository calls here
        } catch (e: Exception) {
            _accountsState.update { it.copy(error = e.message) }
        }
    }

}

data class AccountsState (
    val googleAccounts: List<GoogleAccount> = emptyList(),
    val outlookAccounts: List<OutlookAccount> = emptyList(),
    val error: String? = null,
)
