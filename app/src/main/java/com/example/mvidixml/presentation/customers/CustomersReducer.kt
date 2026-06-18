package com.example.mvidixml.presentation.customers

import com.example.mvidixml.data.Customer
import com.example.mvidixml.data.CustomerCategory
import com.example.mvidixml.data.CustomerCategoryType
import com.example.mvidixml.presentation.customers.CustomersContract.State

object CustomersReducer {

    fun loading(previous: State): State =
        previous.copy(isLoading = true, emptyMessage = null)

    fun content(previous: State, customers: List<Customer>): State =
        previous.copy(
            customers = customers,
            isLoading = false,
            emptyMessage = when {
                customers.isNotEmpty() -> null
                previous.searchQuery.isNotBlank() -> "No customers match the search"
                else -> "No customers yet"
            }
        )

    fun categoriesLoaded(
        previous: State,
        categories: Map<CustomerCategoryType, CustomerCategory>
    ): State =
        previous.copy(
            categoryCashbackInputs = categories.mapValues { (_, category) ->
                category.cashbackPerDay.formatInput()
            }
        )

    fun searchChanged(previous: State, query: String, customers: List<Customer>): State =
        content(previous.copy(searchQuery = query), customers)

    fun nameChanged(previous: State, value: String): State =
        previous.copy(nameInput = value)

    fun accountChanged(previous: State, value: String): State =
        previous.copy(accountInput = value)

    fun phoneChanged(previous: State, value: String): State =
        previous.copy(phoneInput = value)

    fun categoryChanged(previous: State, value: CustomerCategoryType): State =
        previous.copy(
            selectedCategory = value,
            assistantPhoneInput = if (value == CustomerCategoryType.PREMIUM) {
                previous.assistantPhoneInput
            } else {
                ""
            }
        )

    fun assistantPhoneChanged(previous: State, value: String): State =
        previous.copy(assistantPhoneInput = value)

    fun editStarted(previous: State, customer: Customer): State =
        previous.copy(
            editingCustomerId = customer.id,
            nameInput = customer.name,
            accountInput = customer.account,
            phoneInput = customer.phone,
            selectedCategory = customer.category,
            assistantPhoneInput = customer.assistantPhone,
            detailText = null
        )

    fun formCleared(previous: State): State =
        previous.copy(
            editingCustomerId = null,
            nameInput = "",
            accountInput = "",
            phoneInput = "",
            selectedCategory = CustomerCategoryType.REGULAR,
            assistantPhoneInput = ""
        )

    fun customerSaved(previous: State, customers: List<Customer>): State =
        content(
            previous = formCleared(previous),
            customers = customers
        )

    fun detailShown(previous: State, detailText: String): State =
        previous.copy(detailText = detailText)

    fun categoryCashbackChanged(
        previous: State,
        type: CustomerCategoryType,
        value: String
    ): State =
        previous.copy(categoryCashbackInputs = previous.categoryCashbackInputs + (type to value))

    fun categoryCashbackSaved(
        previous: State,
        categories: Map<CustomerCategoryType, CustomerCategory>
    ): State =
        categoriesLoaded(previous, categories)

    private fun Double.formatInput(): String =
        if (this % 1.0 == 0.0) toInt().toString() else toString()
}
