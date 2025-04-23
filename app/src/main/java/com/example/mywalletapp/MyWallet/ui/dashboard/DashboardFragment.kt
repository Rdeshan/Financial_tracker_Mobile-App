package com.example.mywalletapp.MyWallet.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mywalletapp.R
import com.example.mywalletapp.MyWallet.data.PreferenceManager
import com.example.mywalletapp.MyWallet.data.Transaction
import com.example.mywalletapp.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import java.text.NumberFormat
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onCreateView: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViewModel()
            setupUI()
            observeViewModel()
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onViewCreated: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupViewModel() {
        val preferenceManager = PreferenceManager(requireContext())
        val factory = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(preferenceManager) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }

    private fun setupUI() {
        setupBarChart() // setup the horizontal bar chart
        binding.switchDetails.setOnCheckedChangeListener { _, checked ->
            binding.detailsContainer.visibility = if (checked) View.VISIBLE else View.GONE
        }
    }

    private fun observeViewModel() {
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.loadDashboardData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            try {
                binding.tvTotalBalance.text = formatCurrency(balance ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalBalance.text = formatCurrency(0.0)
            }
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            try {
                binding.tvTotalIncome.text = formatCurrency(income ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalIncome.text = formatCurrency(0.0)
            }
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            try {
                binding.tvTotalExpense.text = formatCurrency(expense ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalExpense.text = formatCurrency(0.0)
            }
        }

        viewModel.categorySpending.observe(viewLifecycleOwner) { spending ->
            try {
                updateBarChart(spending ?: emptyMap())
                updateSummaryTables(spending ?: emptyMap())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisLeft.axisMinimum = 0f
            setNoDataText("No transactions yet")
            animateY(1000)
        }
    }

    private fun updateBarChart(spending: Map<String, Double>) {
        if (spending.isEmpty()) {
            binding.barChart.setNoDataText("No transactions yet")
            binding.barChart.invalidate()
            return
        }

        val entries = spending.entries.mapIndexed { index, (cat, amt) ->
            BarEntry(index.toFloat(), amt.toFloat(), cat)
        }

        val dataSet = BarDataSet(entries, "").apply {
            valueTextSize = 12f
            valueFormatter = PercentFormatter()
            colors = entries.map {
                if ((it.data as String).startsWith("Income:"))
                    ContextCompat.getColor(requireContext(), R.color.green_500)
                else
                    ContextCompat.getColor(requireContext(), R.color.red_500)
            }
        }

        binding.barChart.data = BarData(dataSet)
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(
            entries.map { it.data as String }
        )
        binding.barChart.invalidate()
    }

    private fun updateSummaryTables(spending: Map<String, Double>) {
        try {
            binding.incomeTable.removeAllViews()
            binding.expenseTable.removeAllViews()

            addTableHeader(binding.incomeTable)
            addTableHeader(binding.expenseTable)

            val incomeCategories = spending.filter { it.key.startsWith("Income:") }
            val expenseCategories = spending.filter { it.key.startsWith("Expense:") }

            incomeCategories.forEach { (category, amount) ->
                addTableRow(binding.incomeTable, category.removePrefix("Income: "), amount)
            }

            expenseCategories.forEach { (category, amount) ->
                addTableRow(binding.expenseTable, category.removePrefix("Expense: "), amount)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addTableHeader(table: TableLayout) {
        val headerRow = TableRow(requireContext()).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 8)
        }

        val categoryHeader = TextView(requireContext()).apply {
            text = "Category"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val amountHeader = TextView(requireContext()).apply {
            text = "Amount"
            textSize = 14f
            gravity = android.view.Gravity.END
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        headerRow.addView(categoryHeader)
        headerRow.addView(amountHeader)
        table.addView(headerRow)
    }

    private fun addTableRow(table: TableLayout, category: String, amount: Double) {
        val row = TableRow(requireContext()).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 4, 0, 4)
        }

        val categoryText = TextView(requireContext()).apply {
            text = category
            textSize = 14f
        }

        val amountText = TextView(requireContext()).apply {
            text = formatCurrency(amount)
            textSize = 14f
            gravity = android.view.Gravity.END
        }

        row.addView(categoryText)
        row.addView(amountText)
        table.addView(row)
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            NumberFormat.getCurrencyInstance().format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }
}
