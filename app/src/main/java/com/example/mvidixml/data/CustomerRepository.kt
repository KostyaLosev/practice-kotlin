package com.example.mvidixml.data

interface CustomerRepository {
    fun getCustomers(): List<Customer>
    fun getCustomer(id: Long): Customer?
    fun addCustomer(
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String
    ): Customer
    fun updateCustomer(
        id: Long,
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String
    ): Boolean
    fun deleteCustomer(id: Long): Boolean
    fun getCategories(): Map<CustomerCategoryType, CustomerCategory>
    fun updateCategory(type: CustomerCategoryType, cashbackPerDay: Double)
}
