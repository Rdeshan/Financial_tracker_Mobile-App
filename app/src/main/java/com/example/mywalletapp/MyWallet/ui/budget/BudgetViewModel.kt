package com.example.mywalletapp.MyWallet.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import com.example.mywalletapp.MyWallet.util.NotificationHelper
import android.content.Context

class BudgetViewModel(
    private val preferenceManager: PreferenceManager,
    private val context: Context
) : ViewModel() {
    private val _budget = MutableLiveData<Double>(0.0)
    val budget: LiveData<Double> = _budget

    private val _budgetAlert = MutableLiveData<BudgetAlert>()
    val budgetAlert: LiveData<BudgetAlert> = _budgetAlert

    private val notificationHelper = NotificationHelper(context)

    init {
        loadBudget()
        startBudgetMonitoring()
    }

    fun loadBudget() {
        try {
            _budget.value = preferenceManager.getMonthlyBudget()
            checkBudgetThresholds()
        } catch (e: Exception) {
            e.printStackTrace()
            _budget.value = 0.0
        }
    }

    fun updateBudget(newBudget: Double) {
        try {
            preferenceManager.saveMonthlyBudget(newBudget)
            _budget.value = newBudget
            clearAlert()
            checkBudgetThresholds()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startBudgetMonitoring() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = getMonthlyExpenses()
            checkBudgetThresholds(monthlyBudget, monthlyExpenses)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkBudgetThresholds() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = getMonthlyExpenses()
            checkBudgetThresholds(monthlyBudget, monthlyExpenses)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkBudgetThresholds(monthlyBudget: Double, monthlyExpenses: Double) {
        if (monthlyBudget <= 0) return

        val progress = (monthlyExpenses / monthlyBudget * 100).toInt()
        val remaining = monthlyBudget - monthlyExpenses

        when {
            progress >= 100 -> {
                val alert = BudgetAlert(
                    "Budget Exceeded!",
                    "You've exceeded your budget by ${formatCurrency(monthlyExpenses - monthlyBudget)}",
                    true
                )
                _budgetAlert.postValue(alert)
                notificationHelper.showBudgetAlert(alert.title, alert.message, alert.isWarning)
            }
            progress >= 90 -> {
                val alert = BudgetAlert(
                    "Budget Warning!",
                    "You have ${formatCurrency(remaining)} remaining (${100 - progress}% left)",
                    true
                )
                _budgetAlert.postValue(alert)
                notificationHelper.showBudgetAlert(alert.title, alert.message, alert.isWarning)
            }
            progress >= 70 -> {
                val alert = BudgetAlert(
                    "Budget Alert!",
                    "You have ${formatCurrency(remaining)} remaining (${100 - progress}% left)",
                    false
                )
                _budgetAlert.postValue(alert)
                notificationHelper.showBudgetAlert(alert.title, alert.message, alert.isWarning)
            }
        }
    }

    fun clearAlert() {
        _budgetAlert.postValue(null)
    }

    fun getMonthlyExpenses(): Double {
        return try {
            preferenceManager.getMonthlyExpenses()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val currency = preferenceManager.getSelectedCurrency()
            val locale = when (currency) {
                "USD" -> java.util.Locale.US
                "EUR" -> java.util.Locale.GERMANY
                "GBP" -> java.util.Locale.UK
                else -> java.util.Locale.US
            }
            java.text.NumberFormat.getCurrencyInstance(locale).format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }

    data class BudgetAlert(
        val title: String,
        val message: String,
        val isWarning: Boolean
    )

    class Factory(
        private val preferenceManager: PreferenceManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(preferenceManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 