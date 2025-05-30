//package com.example.imilipocket.ui.passcode
package com.example.mywalletapp.MyWallet.ui.passcode


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mywalletapp.databinding.ActivityPasscodeBinding
import com.example.mywalletapp.MyWallet.ui.dashboard.DashboardActivity

class PasscodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasscodeBinding
    private val correctPasscode = "123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val enteredPasscode = binding.editPasscode.text.toString()
            if (enteredPasscode == correctPasscode) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect passcode. Please try again.", Toast.LENGTH_SHORT).show()
                binding.editPasscode.text?.clear()
            }
        }
    }
} 