package com.example.calenderintegration.ui.accounts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.repository.AccountsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _accountsState = MutableStateFlow(AccountsState())
    val accountsState: StateFlow<AccountsState> = _accountsState

    fun loadAllAccounts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // Currently only Google, but can add more later
            val googleAccounts = accountsRepository.getGoogleAccounts(context)

            val combined = buildList {
                addAll(googleAccounts.map { AccountItem("Google", it.email) })
                // Future expansion example:
                // addAll(outlookRepository.getOutlookAccounts(context).map { AccountItem("Outlook", it.email) })
            }

            _accountsState.update {
                it.copy(accounts = combined, loading = false, error = null)
            }
        }
    }

    fun openProviderPicker() {
        _accountsState.update { it.copy(showProviderPicker = true) }
    }

    fun dismissProviderPicker() {
        _accountsState.update { it.copy(showProviderPicker = false) }
    }

    fun requestDelete(email: String) {
        _accountsState.update { it.copy(showDeleteDialog = true, pendingDeleteEmail = email) }
    }

    fun cancelDelete() {
        _accountsState.update { it.copy(showDeleteDialog = false, pendingDeleteEmail = null) }
    }

    fun confirmDelete(context: Context) {
        val email = _accountsState.value.pendingDeleteEmail ?: return
        viewModelScope.launch(Dispatchers.IO) {
            accountsRepository.deleteGoogleAccount(context, email)
            loadAllAccounts(context) // refresh after deletion
        }
    }
}

data class AccountItem(
    val provider: String,   // e.g. "Google", "Outlook"
    val email: String
)

data class AccountsState(
    val accounts: List<AccountItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val pendingDeleteEmail: String? = null,
    val showProviderPicker: Boolean = false
)
