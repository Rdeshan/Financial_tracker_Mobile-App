package com.example.mywalletapp.MyWallet.ui.transaction


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import com.example.mywalletapp.MyWallet.data.Result
import com.example.mywalletapp.MyWallet.data.Transaction

class AddTransactionViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult

    fun addTransaction(transaction: Transaction) {
        try {
            preferenceManager.addTransaction(transaction)
            _saveResult.value = Result.Success(Unit)
        } catch (e: Exception) {
            _saveResult.value = Result.Error(e)
        }
    }
}

class AddTransactionViewModelFactory(
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 