package com.example.mywalletapp.MyWallet.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import java.io.File

class SettingsViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

    private val _backupStatus = MutableLiveData<String>()
    val backupStatus: LiveData<String> = _backupStatus

    private val _restoreStatus = MutableLiveData<String>()
    val restoreStatus: LiveData<String> = _restoreStatus

    fun createBackup() {
        try {
            val transactions = preferenceManager.getTransactions()
            val budget = preferenceManager.getMonthlyBudget()
            val currency = preferenceManager.getSelectedCurrency()

            val backupData = mapOf(
                "transactions" to transactions,
                "budget" to budget,
                "currency" to currency
            )

            val success = preferenceManager.createBackup(preferenceManager.gson.toJson(backupData))
            if (success) {
                _backupStatus.value = "Backup created successfully"
            } else {
                _backupStatus.value = "Failed to create backup"
            }
        } catch (e: Exception) {
            _backupStatus.value = "Error creating backup: ${e.message}"
        }
    }

    fun restoreFromBackup(backupFile: File) {
        try {
            val success = preferenceManager.restoreFromBackup(backupFile)
            if (success) {
                _restoreStatus.value = "Data restored successfully"
            } else {
                _restoreStatus.value = "Failed to restore data"
            }
        } catch (e: Exception) {
            _restoreStatus.value = "Error restoring data: ${e.message}"
        }
    }

    fun getBackupFiles(): List<File> {
        return preferenceManager.getBackupFiles()
    }
} 