package com.example.mvidixml.di

import com.example.mvidixml.data.CustomerRepository
import com.example.mvidixml.data.InMemoryCustomerRepository
import com.example.mvidixml.domain.CustomerInteractor
import com.example.mvidixml.presentation.customers.CustomersViewModelFactory

class AppContainer {

    private val customerRepository: CustomerRepository = InMemoryCustomerRepository()

    private val customerInteractor = CustomerInteractor(customerRepository)

    val customersViewModelFactory = CustomersViewModelFactory(customerInteractor)
}
