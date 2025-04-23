package com.example.mywalletapp.MyWallet.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mywalletapp.R
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import com.example.mywalletapp.MyWallet.data.Transaction
import com.example.mywalletapp.databinding.FragmentAddTransactionBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTransactionViewModel by viewModels {
        AddTransactionViewModelFactory(PreferenceManager(requireContext()))
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var selectedDate: String = LocalDate.now().format(dateFormatter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup category spinner
        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Setup type radio group
        binding.radioGroupType.setOnCheckedChangeListener { _, _ ->
            binding.spinnerCategory.setSelection(0) // Reset category
        }

        updateDateButtonText()
    }

    private fun setupClickListeners() {
        binding.buttonSave.setOnClickListener { saveTransaction() }
        binding.buttonCancel.setOnClickListener { findNavController().navigateUp() }
        binding.buttonDatePicker.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val now = LocalDate.now()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)
                updateDateButtonText()
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        dialog.show()
    }

    private fun updateDateButtonText() {
        binding.buttonDatePicker.text = "Date: $selectedDate"
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddTransactionViewModel.SaveResult.Success -> {
                    Toast.makeText(requireContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                    if (isAdded && !isDetached) findNavController().navigateUp()
                }
                is AddTransactionViewModel.SaveResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveTransaction() {
        val title = binding.editTextTitle.text.toString().trim()
        val amountText = binding.editTextAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val type = when (binding.radioGroupType.checkedRadioButtonId) {
            R.id.radioIncome -> Transaction.Type.INCOME
            R.id.radioExpense -> Transaction.Type.EXPENSE
            else -> {
                Snackbar.make(binding.root, "Please select transaction type", Snackbar.LENGTH_LONG).show()
                return
            }
        }

        if (title.isBlank()) {
            binding.editTextTitle.error = "Title is required"
            return
        }

        if (amountText.isBlank()) {
            binding.editTextAmount.error = "Amount is required"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.editTextAmount.error = "Amount must be greater than 0"
            return
        }

        val dateInMillis = LocalDate.parse(selectedDate, dateFormatter)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModel.addTransaction(title, amount, category, type, dateInMillis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
