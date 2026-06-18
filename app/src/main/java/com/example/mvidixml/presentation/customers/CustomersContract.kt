package com.example.mvidixml.presentation.customers

import com.example.mvidixml.data.Customer
import com.example.mvidixml.data.CustomerCategoryType

object CustomersContract {

    sealed interface Intent {
        data object Load : Intent
        data class SearchChanged(val query: String) : Intent
        data class NameChanged(val value: String) : Intent
        data class AccountChanged(val value: String) : Intent
        data class PhoneChanged(val value: String) : Intent
        data class CategoryChanged(val value: CustomerCategoryType) : Intent
        data class AssistantPhoneChanged(val value: String) : Intent
        data object SubmitClicked : Intent
        data object CancelEditClicked : Intent
        data class EditClicked(val id: Long) : Intent
        data class DeleteClicked(val id: Long) : Intent
        data class DetailsClicked(val id: Long) : Intent
        data class CategoryCashbackChanged(
            val type: CustomerCategoryType,
            val value: String
        ) : Intent
        data object SaveCategoriesClicked : Intent
    }

    data class State(
        val customers: List<Customer> = emptyList(),
        val searchQuery: String = "",
        val nameInput: String = "",
        val accountInput: String = "",
        val phoneInput: String = "",
        val selectedCategory: CustomerCategoryType = CustomerCategoryType.REGULAR,
        val assistantPhoneInput: String = "",
        val editingCustomerId: Long? = null,
        val categoryCashbackInputs: Map<CustomerCategoryType, String> = emptyMap(),
        val detailText: String? = null,
        val isLoading: Boolean = false,
        val emptyMessage: String? = null
    ) {
        val isEditing: Boolean
            get() = editingCustomerId != null

        val showAssistantPhone: Boolean
            get() = selectedCategory == CustomerCategoryType.PREMIUM

        val canAdd: Boolean
            get() = nameInput.isNotBlank()

        val submitText: String
            get() = if (isEditing) "Save changes" else "Add client"
    }

    sealed interface Effect {
        data class ShowMessage(val text: String) : Effect
    }
}
