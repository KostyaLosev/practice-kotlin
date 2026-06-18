package com.example.mvidixml.domain

import com.example.mvidixml.data.Customer
import com.example.mvidixml.data.CustomerCategory
import com.example.mvidixml.data.CustomerCategoryType
import com.example.mvidixml.data.CustomerRepository

class CustomerInteractor(
    private val repository: CustomerRepository
) {
    fun loadCustomers(query: String = ""): List<Customer> =
        repository.getCustomers().filterByQuery(query)

    fun getCategories(): Map<CustomerCategoryType, CustomerCategory> =
        repository.getCategories()

    fun addCustomer(
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String,
        query: String
    ): Result<List<Customer>> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name is required"))
        }

        repository.addCustomer(
            name = name,
            account = account,
            phone = phone,
            category = category,
            assistantPhone = assistantPhone
        )
        return Result.success(repository.getCustomers().filterByQuery(query))
    }

    fun updateCustomer(
        id: Long,
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String,
        query: String
    ): Result<List<Customer>> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name is required"))
        }

        val updated = repository.updateCustomer(
            id = id,
            name = name,
            account = account,
            phone = phone,
            category = category,
            assistantPhone = assistantPhone
        )
        return if (updated) {
            Result.success(repository.getCustomers().filterByQuery(query))
        } else {
            Result.failure(IllegalArgumentException("Customer was not found"))
        }
    }

    fun deleteCustomer(id: Long, query: String): Result<List<Customer>> {
        val deleted = repository.deleteCustomer(id)
        return if (deleted) {
            Result.success(repository.getCustomers().filterByQuery(query))
        } else {
            Result.failure(IllegalArgumentException("Customer was not found"))
        }
    }

    fun getCustomerDetail(id: Long): Result<String> {
        val customer = repository.getCustomer(id)
            ?: return Result.failure(IllegalArgumentException("Customer was not found"))

        val categories = repository.getCategories()
        val category = categories[customer.category]
        val categoryTitle = category?.title ?: customer.category.title

        val specialDetail = when (customer.category) {
            CustomerCategoryType.REGULAR -> "Regular client"
            CustomerCategoryType.MEDIUM -> {
                val cashback = (category?.cashbackPerDay ?: 15.0) * 30
                "Cashback for 30 days: $cashback"
            }
            CustomerCategoryType.PREMIUM -> {
                val assistant = customer.assistantPhone.ifBlank { "not assigned" }
                "Assistant phone: $assistant"
            }
        }

        return Result.success(
            listOf(
                customer.name,
                "Category: $categoryTitle",
                "Account: ${customer.account}",
                "Phone: ${customer.phone}",
                specialDetail
            ).joinToString(separator = "\n")
        )
    }

    fun updateCategoryCashback(
        values: Map<CustomerCategoryType, String>
    ): Result<Map<CustomerCategoryType, CustomerCategory>> {
        values.forEach { (type, rawValue) ->
            val parsed = rawValue.trim().toDoubleOrNull()
                ?: return Result.failure(IllegalArgumentException("${type.title} cashback must be a number"))
            if (parsed < 0) {
                return Result.failure(IllegalArgumentException("${type.title} cashback cannot be negative"))
            }
            repository.updateCategory(type, parsed)
        }
        return Result.success(repository.getCategories())
    }

    private fun List<Customer>.filterByQuery(query: String): List<Customer> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return this

        return filter { customer ->
            customer.name.contains(normalizedQuery, ignoreCase = true) ||
                customer.account.contains(normalizedQuery, ignoreCase = true) ||
                customer.phone.contains(normalizedQuery, ignoreCase = true) ||
                customer.category.title.contains(normalizedQuery, ignoreCase = true)
        }
    }
}
