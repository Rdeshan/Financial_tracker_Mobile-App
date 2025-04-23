
package com.example.mywalletapp.MyWallet.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.mywalletapp.R
import com.example.mywalletapp.databinding.ActivityOnboardingBinding
import com.example.mywalletapp.MyWallet.ui.passcode.PasscodeActivity
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewPagerAdapter: OnboardingViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        viewPagerAdapter = OnboardingViewPagerAdapter()
        binding.viewPager.adapter = viewPagerAdapter



        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.btnNext.text = if (position == viewPagerAdapter.itemCount - 1) "Get Started" else "Next"
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem == viewPagerAdapter.itemCount - 1) {
                startPasscodeActivity()
            } else {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            }
        }


    }

    private fun startPasscodeActivity() {
        startActivity(Intent(this, PasscodeActivity::class.java))
        finish()
    }
} 