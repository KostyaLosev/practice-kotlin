package com.example.mvidixml.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvidixml.data.CustomerCategoryType
import com.example.mvidixml.domain.CustomerInteractor
import com.example.mvidixml.presentation.customers.CustomersContract.Effect
import com.example.mvidixml.presentation.customers.CustomersContract.Intent
import com.example.mvidixml.presentation.customers.CustomersContract.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val customerInteractor: CustomerInteractor
) : ViewModel() {

    private val intents = Channel<Intent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            intents.receiveAsFlow().collect(::handleIntent)
        }

        accept(Intent.Load)
    }

    fun accept(intent: Intent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    private suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadCustomers()
            is Intent.SearchChanged -> searchCustomers(intent.query)
            is Intent.NameChanged -> updateState(CustomersReducer.nameChanged(_state.value, intent.value))
            is Intent.AccountChanged -> updateState(CustomersReducer.accountChanged(_state.value, intent.value))
            is Intent.PhoneChanged -> updateState(CustomersReducer.phoneChanged(_state.value, intent.value))
            is Intent.CategoryChanged -> updateState(CustomersReducer.categoryChanged(_state.value, intent.value))
            is Intent.AssistantPhoneChanged -> {
                updateState(CustomersReducer.assistantPhoneChanged(_state.value, intent.value))
            }
            Intent.SubmitClicked -> saveCustomer()
            Intent.CancelEditClicked -> updateState(CustomersReducer.formCleared(_state.value))
            is Intent.EditClicked -> startEdit(intent.id)
            is Intent.DeleteClicked -> deleteCustomer(intent.id)
            is Intent.DetailsClicked -> showDetails(intent.id)
            is Intent.CategoryCashbackChanged -> {
                updateState(
                    CustomersReducer.categoryCashbackChanged(
                        previous = _state.value,
                        type = intent.type,
                        value = intent.value
                    )
                )
            }
            Intent.SaveCategoriesClicked -> saveCategories()
        }
    }

    private fun loadCustomers() {
        updateState(CustomersReducer.loading(_state.value))
        updateState(CustomersReducer.categoriesLoaded(_state.value, customerInteractor.getCategories()))
        updateState(
            CustomersReducer.content(
                previous = _state.value,
                customers = customerInteractor.loadCustomers(_state.value.searchQuery)
            )
        )
    }

    private fun searchCustomers(query: String) {
        updateState(
            CustomersReducer.searchChanged(
                previous = _state.value,
                query = query,
                customers = customerInteractor.loadCustomers(query)
            )
        )
    }

    private suspend fun saveCustomer() {
        val current = _state.value
        val result = if (current.isEditing) {
            customerInteractor.updateCustomer(
                id = current.editingCustomerId ?: return,
                name = current.nameInput,
                account = current.accountInput,
                phone = current.phoneInput,
                category = current.selectedCategory,
                assistantPhone = current.assistantPhoneInput,
                query = current.searchQuery
            )
        } else {
            customerInteractor.addCustomer(
                name = current.nameInput,
                account = current.accountInput,
                phone = current.phoneInput,
                category = current.selectedCategory,
                assistantPhone = current.assistantPhoneInput,
                query = current.searchQuery
            )
        }

        result.fold(
            onSuccess = { customers ->
                updateState(CustomersReducer.customerSaved(current, customers))
                _effect.emit(
                    Effect.ShowMessage(
                        if (current.isEditing) "Client updated" else "Client added"
                    )
                )
            },
            onFailure = { error ->
                _effect.emit(Effect.ShowMessage(error.message ?: "Could not save client"))
            }
        )
    }

    private suspend fun startEdit(id: Long) {
        val customer = _state.value.customers.firstOrNull { it.id == id }
        if (customer == null) {
            _effect.emit(Effect.ShowMessage("Client was not found"))
            return
        }

        updateState(CustomersReducer.editStarted(_state.value, customer))
    }

    private suspend fun showDetails(id: Long) {
        customerInteractor.getCustomerDetail(id).fold(
            onSuccess = { detail ->
                updateState(CustomersReducer.detailShown(_state.value, detail))
            },
            onFailure = { error ->
                _effect.emit(Effect.ShowMessage(error.message ?: "Could not show client details"))
            }
        )
    }

    private suspend fun saveCategories() {
        val current = _state.value
        val values = CustomerCategoryType.values().associateWith { type ->
            current.categoryCashbackInputs[type].orEmpty()
        }

        customerInteractor.updateCategoryCashback(values).fold(
            onSuccess = { categories ->
                updateState(CustomersReducer.categoryCashbackSaved(_state.value, categories))
                _effect.emit(Effect.ShowMessage("Categories updated"))
            },
            onFailure = { error ->
                _effect.emit(Effect.ShowMessage(error.message ?: "Could not update categories"))
            }
        )
    }

    private suspend fun deleteCustomer(id: Long) {
        customerInteractor.deleteCustomer(id, _state.value.searchQuery).fold(
            onSuccess = { customers ->
                updateState(CustomersReducer.content(_state.value, customers))
                _effect.emit(Effect.ShowMessage("Client deleted"))
            },
            onFailure = { error ->
                _effect.emit(Effect.ShowMessage(error.message ?: "Could not delete customer"))
            }
        )
    }

    private fun updateState(state: State) {
        _state.value = state
    }
}
