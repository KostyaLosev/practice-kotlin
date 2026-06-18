package com.example.mvidixml.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvidixml.domain.CustomerInteractor

class CustomersViewModelFactory(
    private val customerInteractor: CustomerInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
            return CustomersViewModel(customerInteractor) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
