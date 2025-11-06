package com.example.calenderintegration.ui.accounts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            val google = accountsRepository.getGoogleAccounts(context)
            val zoho   = accountsRepository.getZohoAccounts(context)

            val combined = buildList {
                addAll(google.map { AccountItem(provider = "Google", email = it.email) })
                addAll(zoho.map   { AccountItem(provider = "Zoho",   email = it.email) })
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
            // try both stores; whichever has it will remove it
            accountsRepository.deleteGoogleAccount(context, email)
            accountsRepository.deleteZohoAccount(context, email)
            loadAllAccounts(context)
        }
    }

    // -------- Zoho email-only add --------
    fun addZohoByEmail(context: Context, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountsRepository.addZohoAccount(context, email.trim())
            loadAllAccounts(context)
        }
    }
}

data class AccountItem(
    val provider: String,   // "Google" or "Zoho"
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
