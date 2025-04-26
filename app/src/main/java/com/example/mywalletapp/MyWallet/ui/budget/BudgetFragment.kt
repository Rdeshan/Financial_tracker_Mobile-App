package com.example.mywalletapp.MyWallet.ui.budget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mywalletapp.R
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import com.example.mywalletapp.databinding.FragmentBudgetBinding
import com.example.mywalletapp.MyWallet.util.NotificationHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.color.MaterialColors

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: BudgetViewModel
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentBudgetBinding.inflate(inflater, container, false)
            preferenceManager = PreferenceManager(requireContext())
            viewModel = ViewModelProvider(
                this,
                BudgetViewModel.Factory(preferenceManager, requireContext())
            )[BudgetViewModel::class.java]
            notificationHelper = NotificationHelper(requireContext())

            setupUI()
            setupClickListeners()
            observeViewModel()
            checkNotificationPermission()

            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error initializing budget screen", Toast.LENGTH_SHORT).show()
            return binding.root
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.loadBudget()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        try {
            val currentBudget = preferenceManager.getMonthlyBudget()
            binding.etMonthlyBudget.setText(currentBudget.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            binding.etMonthlyBudget.setText("0")
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }
    }

    private fun observeViewModel() {
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            try {
                binding.etMonthlyBudget.setText(budget.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel.budgetAlert.observe(viewLifecycleOwner) { alert ->
            alert?.let { showBudgetAlert(it) }
        }
    }

    private fun showBudgetAlert(alert: BudgetViewModel.BudgetAlert) {
        val snackbar = Snackbar.make(binding.root, alert.message, Snackbar.LENGTH_LONG)

        if (alert.isWarning) {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.errorColor))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.Accent))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }

        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                super.onDismissed(snackbar, event)
                viewModel.clearAlert()
            }
        })

        snackbar.show()
    }

    private fun saveBudget() {
        try {
            val budget = binding.etMonthlyBudget.text.toString().toDouble()
            if (budget < 0) {
                Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.updateBudget(budget)
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }
} 