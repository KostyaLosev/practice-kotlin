package com.example.mvidixml.data

data class Customer(
    val id: Long,
    val name: String,
    val account: String,
    val phone: String,
    val category: CustomerCategoryType,
    val assistantPhone: String = ""
) {
    val requiresAssistantPhone: Boolean
        get() = category == CustomerCategoryType.PREMIUM
}
