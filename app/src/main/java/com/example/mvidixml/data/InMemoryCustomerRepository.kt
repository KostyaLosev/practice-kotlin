package com.example.mvidixml.data

class InMemoryCustomerRepository : CustomerRepository {
    private var nextId = 5L

    private val customers = mutableListOf(
        Customer(
            id = 1L,
            name = "Alice",
            account = "ACC001",
            phone = "555-1111",
            category = CustomerCategoryType.REGULAR
        ),
        Customer(
            id = 2L,
            name = "Bob",
            account = "ACC002",
            phone = "555-2222",
            category = CustomerCategoryType.MEDIUM
        ),
        Customer(
            id = 3L,
            name = "Charlie",
            account = "ACC003",
            phone = "555-3333",
            category = CustomerCategoryType.PREMIUM,
            assistantPhone = "888-0000"
        ),
        Customer(
            id = 4L,
            name = "David",
            account = "ACC004",
            phone = "555-4444",
            category = CustomerCategoryType.REGULAR
        )
    )

    private val categories = mutableMapOf(
        CustomerCategoryType.REGULAR to CustomerCategory(
            type = CustomerCategoryType.REGULAR,
            title = CustomerCategoryType.REGULAR.title,
            cashbackPerDay = 1.0
        ),
        CustomerCategoryType.MEDIUM to CustomerCategory(
            type = CustomerCategoryType.MEDIUM,
            title = CustomerCategoryType.MEDIUM.title,
            cashbackPerDay = 15.0
        ),
        CustomerCategoryType.PREMIUM to CustomerCategory(
            type = CustomerCategoryType.PREMIUM,
            title = CustomerCategoryType.PREMIUM.title,
            cashbackPerDay = 10.0
        )
    )

    override fun getCustomers(): List<Customer> = customers.toList()

    override fun getCustomer(id: Long): Customer? =
        customers.firstOrNull { it.id == id }

    override fun addCustomer(
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String
    ): Customer {
        val customer = Customer(
            id = nextId++,
            name = name.trim(),
            account = account.trim(),
            phone = phone.trim(),
            category = category,
            assistantPhone = assistantPhone.trim()
        )
        customers.add(0, customer)
        return customer
    }

    override fun updateCustomer(
        id: Long,
        name: String,
        account: String,
        phone: String,
        category: CustomerCategoryType,
        assistantPhone: String
    ): Boolean {
        val index = customers.indexOfFirst { it.id == id }
        if (index == -1) return false

        customers[index] = customers[index].copy(
            name = name.trim(),
            account = account.trim(),
            phone = phone.trim(),
            category = category,
            assistantPhone = assistantPhone.trim()
        )
        return true
    }

    override fun deleteCustomer(id: Long): Boolean =
        customers.removeAll { it.id == id }

    override fun getCategories(): Map<CustomerCategoryType, CustomerCategory> =
        categories.toMap()

    override fun updateCategory(type: CustomerCategoryType, cashbackPerDay: Double) {
        val previous = categories[type] ?: return
        categories[type] = previous.copy(cashbackPerDay = cashbackPerDay)
    }
}
