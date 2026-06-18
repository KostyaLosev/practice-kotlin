package com.example.mvidixml.presentation.customers

import android.os.Bundle
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mvidixml.MviDiApplication
import com.example.mvidixml.data.Customer
import com.example.mvidixml.data.CustomerCategoryType
import com.example.mvidixml.databinding.ActivityMainBinding
import com.example.mvidixml.presentation.customers.CustomersContract.Effect
import com.example.mvidixml.presentation.customers.CustomersContract.Intent
import com.example.mvidixml.presentation.customers.CustomersContract.State
import kotlinx.coroutines.launch

class CustomersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val categoryTypes = CustomerCategoryType.values().toList()
    private var suppressCategorySelection = false

    private val viewModel: CustomersViewModel by viewModels {
        (application as MviDiApplication).appContainer.customersViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindCategorySpinner()
        bindInputs()
        bindClicks()
        observeMvi()
    }

    private fun bindInputs() = with(binding) {
        editSearch.bindAfterTextChanged { viewModel.accept(Intent.SearchChanged(it)) }
        editName.bindAfterTextChanged { viewModel.accept(Intent.NameChanged(it)) }
        editAccount.bindAfterTextChanged { viewModel.accept(Intent.AccountChanged(it)) }
        editPhone.bindAfterTextChanged { viewModel.accept(Intent.PhoneChanged(it)) }
        editAssistantPhone.bindAfterTextChanged { viewModel.accept(Intent.AssistantPhoneChanged(it)) }
        editRegularCashback.bindAfterTextChanged {
            viewModel.accept(Intent.CategoryCashbackChanged(CustomerCategoryType.REGULAR, it))
        }
        editMediumCashback.bindAfterTextChanged {
            viewModel.accept(Intent.CategoryCashbackChanged(CustomerCategoryType.MEDIUM, it))
        }
        editPremiumCashback.bindAfterTextChanged {
            viewModel.accept(Intent.CategoryCashbackChanged(CustomerCategoryType.PREMIUM, it))
        }
    }

    private fun bindClicks() = with(binding) {
        buttonAdd.setOnClickListener {
            viewModel.accept(Intent.SubmitClicked)
        }
        buttonCancelEdit.setOnClickListener {
            viewModel.accept(Intent.CancelEditClicked)
        }
        buttonSaveCategories.setOnClickListener {
            viewModel.accept(Intent.SaveCategoriesClicked)
        }
    }

    private fun bindCategorySpinner() = with(binding) {
        spinnerCategory.adapter = ArrayAdapter(
            this@CustomersActivity,
            android.R.layout.simple_spinner_dropdown_item,
            categoryTypes.map { it.title }
        )
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!suppressCategorySelection) {
                    viewModel.accept(Intent.CategoryChanged(categoryTypes[position]))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun observeMvi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect(::render)
                }
                launch {
                    viewModel.effect.collect(::handleEffect)
                }
            }
        }
    }

    private fun render(state: State) = with(binding) {
        progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        buttonAdd.isEnabled = state.canAdd
        buttonAdd.text = state.submitText
        buttonCancelEdit.visibility = if (state.isEditing) View.VISIBLE else View.GONE
        textEmpty.visibility = if (state.emptyMessage == null) View.GONE else View.VISIBLE
        textEmpty.text = state.emptyMessage.orEmpty()
        textDetail.visibility = if (state.detailText == null) View.GONE else View.VISIBLE
        textDetail.text = state.detailText.orEmpty()

        setTextIfDifferent(editSearch.text?.toString().orEmpty(), state.searchQuery) {
            editSearch.setText(state.searchQuery)
            editSearch.setSelection(state.searchQuery.length)
        }
        setTextIfDifferent(editName.text?.toString().orEmpty(), state.nameInput) {
            editName.setText(state.nameInput)
            editName.setSelection(state.nameInput.length)
        }
        setTextIfDifferent(editAccount.text?.toString().orEmpty(), state.accountInput) {
            editAccount.setText(state.accountInput)
            editAccount.setSelection(state.accountInput.length)
        }
        setTextIfDifferent(editPhone.text?.toString().orEmpty(), state.phoneInput) {
            editPhone.setText(state.phoneInput)
            editPhone.setSelection(state.phoneInput.length)
        }
        setTextIfDifferent(editAssistantPhone.text?.toString().orEmpty(), state.assistantPhoneInput) {
            editAssistantPhone.setText(state.assistantPhoneInput)
            editAssistantPhone.setSelection(state.assistantPhoneInput.length)
        }

        editAssistantPhone.visibility = if (state.showAssistantPhone) View.VISIBLE else View.GONE
        renderSelectedCategory(state.selectedCategory)
        renderCategoryInputs(state.categoryCashbackInputs)

        renderCustomers(state.customers)
    }

    private fun renderSelectedCategory(category: CustomerCategoryType) = with(binding.spinnerCategory) {
        val position = categoryTypes.indexOf(category).coerceAtLeast(0)
        if (selectedItemPosition != position) {
            suppressCategorySelection = true
            setSelection(position)
            suppressCategorySelection = false
        }
    }

    private fun renderCategoryInputs(inputs: Map<CustomerCategoryType, String>) = with(binding) {
        setTextIfDifferent(editRegularCashback.text?.toString().orEmpty(), inputs[CustomerCategoryType.REGULAR].orEmpty()) {
            editRegularCashback.setText(inputs[CustomerCategoryType.REGULAR].orEmpty())
        }
        setTextIfDifferent(editMediumCashback.text?.toString().orEmpty(), inputs[CustomerCategoryType.MEDIUM].orEmpty()) {
            editMediumCashback.setText(inputs[CustomerCategoryType.MEDIUM].orEmpty())
        }
        setTextIfDifferent(editPremiumCashback.text?.toString().orEmpty(), inputs[CustomerCategoryType.PREMIUM].orEmpty()) {
            editPremiumCashback.setText(inputs[CustomerCategoryType.PREMIUM].orEmpty())
        }
    }

    private fun renderCustomers(customers: List<Customer>) = with(binding.customersContainer) {
        removeAllViews()
        customers.forEach { customer ->
            addView(createCustomerRow(customer))
        }
    }

    private fun createCustomerRow(customer: Customer): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = getDrawable(com.example.mvidixml.R.drawable.customer_card_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val name = TextView(this).apply {
            text = customer.name
            textSize = 18f
            setTextColor(getColor(com.example.mvidixml.R.color.text_primary))
        }

        val details = TextView(this).apply {
            text = listOf(
                customer.category.title,
                "Account: ${customer.account}",
                "Phone: ${customer.phone}"
            )
                .filter { it.isNotBlank() }
                .joinToString(separator = "\n")
            textSize = 14f
            setTextColor(getColor(com.example.mvidixml.R.color.text_secondary))
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }

        val detail = Button(this).apply {
            text = getString(com.example.mvidixml.R.string.details_customer)
            setOnClickListener {
                viewModel.accept(Intent.DetailsClicked(customer.id))
            }
            layoutParams = actionButtonParams()
        }

        val edit = Button(this).apply {
            text = getString(com.example.mvidixml.R.string.edit_customer)
            setOnClickListener {
                viewModel.accept(Intent.EditClicked(customer.id))
            }
            layoutParams = actionButtonParams()
        }

        val delete = Button(this).apply {
            text = getString(com.example.mvidixml.R.string.delete_customer)
            setOnClickListener {
                viewModel.accept(Intent.DeleteClicked(customer.id))
            }
            layoutParams = actionButtonParams()
        }

        row.addView(name)
        row.addView(details)
        actions.addView(detail)
        actions.addView(edit)
        actions.addView(delete)
        row.addView(actions)
        return row
    }

    private fun handleEffect(effect: Effect) {
        when (effect) {
            is Effect.ShowMessage -> Toast.makeText(this, effect.text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun actionButtonParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        }

    private fun setTextIfDifferent(current: String, next: String, update: () -> Unit) {
        if (current != next) {
            update()
        }
    }

    private fun TextView.bindAfterTextChanged(onChanged: (String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                onChanged(s?.toString().orEmpty())
            }
        })
    }
}
