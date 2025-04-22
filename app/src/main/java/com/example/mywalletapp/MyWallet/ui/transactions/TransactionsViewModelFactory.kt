package com.example.mywalletapp.MyWallet.ui.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mywalletapp.MyWallet.data.PreferenceManager

class TransactionsViewModelFactory(
    private val preferenceManager: PreferenceManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(preferenceManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 