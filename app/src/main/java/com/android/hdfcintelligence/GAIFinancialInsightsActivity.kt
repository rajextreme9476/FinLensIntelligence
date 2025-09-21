package com.android.hdfcintelligence

import FinancialInsights
import TopExpense
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.hdfcintelligence.api.AccountDetails
import com.android.hdfcintelligence.api.AccountSummary
import com.android.hdfcintelligence.api.ApiResponse
import com.android.hdfcintelligence.api.BankStatementRequest
import com.android.hdfcintelligence.api.RetrofitClient
import com.android.hdfcintelligence.databinding.ActivityGaifinancialInsightsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.Locale

class GAIFinancialInsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGaifinancialInsightsBinding
    private lateinit var bannerViewPager: ViewPager2
    private lateinit var bannerIndicator: TabLayout
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGaifinancialInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val insightsData: FinancialInsights? = intent.getParcelableExtra("financialInsights")

        if (insightsData != null) {
            setupPieChartNew(pieChart, insightsData.top_5_expenses)
        }

        insightsData?.let {
            val balance = findViewById<TextView>(R.id.textBalance)
            val userName = findViewById<TextView>(R.id.textUserName)
            val accountNumber = findViewById<TextView>(R.id.textAccountNumber)
            val monthlyChange = findViewById<TextView>(R.id.textMonthlyChange)
            val creditsAmount = findViewById<TextView>(R.id.textCreditsAmount)
            val debitsAmount = findViewById<TextView>(R.id.textDebitsAmount)
            val tvStatementPeroid = findViewById<TextView>(R.id.textStatementPeriod)
            val mostFrequentPayee = findViewById<TextView>(R.id.tv_most_frequent_payee)
            val mostFrequentPayeeAmt = findViewById<TextView>(R.id.tv_most_frequent_payee_amt)
            val largestTransaction = findViewById<TextView>(R.id.tv_largest_transaction)
            val largestTransactionAmt = findViewById<TextView>(R.id.tv_largest_transaction_amt)
            val highestSpending = findViewById<TextView>(R.id.tv_highest_spending_date)
            val highestSpendingAmt = findViewById<TextView>(R.id.tv_highest_spending_amt)
            val lowestBalance = findViewById<TextView>(R.id.tv_lowest_balance_date)
            val lowestBalanceAmt = findViewById<TextView>(R.id.tv_lowest_balance_amt)
            val aiSuggestion = findViewById<TextView>(R.id.tv_ai_suggestion)

            val totalTransactions = findViewById<TextView>(R.id.tv_total_transactions)
            val totalTransactionsLabel = findViewById<TextView>(R.id.tv_total_label)
            val upiTransactions = findViewById<TextView>(R.id.tv_upi_transactions)
            val upiLabel = findViewById<TextView>(R.id.tv_upi_label)
            val impsTransactions = findViewById<TextView>(R.id.tv_imps_transactions)
            val impsLabel = findViewById<TextView>(R.id.tv_imps_label)
            val testTransactions = findViewById<TextView>(R.id.tv_test_transactions)
            val testLabel = findViewById<TextView>(R.id.tv_test_label)

            // Top Expense category views
            val tvCategoryOne: TextView = findViewById(R.id.tvCategoryOne)
            val tvCategoryOneAmt: TextView = findViewById(R.id.tvCategoryOneAmt)
            val tvCategoryTwo: TextView = findViewById(R.id.tvCategoryTwo)
            val tvCategoryTwoAmt: TextView = findViewById(R.id.tvCategoryTwoAmt)
            val tvCategoryThree: TextView = findViewById(R.id.tvCategoryThree)
            val tvCategoryThreeAmt: TextView = findViewById(R.id.tvCategoryThreeAmt)
            val tvCategoryFour: TextView = findViewById(R.id.tvCategoryFour)
            val tvCategoryFourAmt: TextView = findViewById(R.id.tvCategoryFourAmt)


            val tvInsightOne: TextView  = findViewById(R.id.tv_insight_one)
            val tvExplanationOne: TextView  = findViewById(R.id.tv_explanation_one)
            val tvInsightTwo: TextView  = findViewById(R.id.tv_insight_two)
            val tvExplanationTwo: TextView  = findViewById(R.id.tv_explanation_two)
            val tvInsightThree : TextView = findViewById(R.id.tv_insight_three)
            val tvExplanationThree : TextView = findViewById(R.id.tv_explanation_three)


            tvInsightOne.text = insightsData.aiDrivenPredictiveInsights?.get(0)?.insight.toString()
            tvExplanationOne.text = insightsData.aiDrivenPredictiveInsights?.get(0)?.explanation.toString()

            tvInsightTwo.text = insightsData.aiDrivenPredictiveInsights?.get(1)?.insight.toString()
            tvExplanationTwo.text = insightsData.aiDrivenPredictiveInsights?.get(1)?.explanation.toString()

            if (insightsData.aiDrivenPredictiveInsights?.size ?: 0 > 2) {
                tvInsightThree.text = insightsData.aiDrivenPredictiveInsights?.get(2)?.insight.toString()
                tvExplanationThree.text = insightsData.aiDrivenPredictiveInsights?.get(2)?.explanation.toString()
            } else {
                tvInsightThree.text = "No data available"
                tvExplanationThree.text = "No data available"
            }
            // Account summary section
            userName.text = " ${it.accountSummary?.accountHolderName}"
            val maskedAccountNumber = maskAccountNumber(it.accountSummary?.accountNumber)
            accountNumber.text = "$maskedAccountNumber"
            tvStatementPeroid.text = formatStatementPeriod(it.accountSummary?.statementPeriod)

            balance.text = "₹${String.format("%,.2f", it.accountSummary?.closingBalance?.replace(",", "")?.toDoubleOrNull() ?: 0.0)}"
            monthlyChange.text = "↑ +₹${String.format("%,.2f", it.accountSummary?.openingBalance?.replace(",", "")?.toDoubleOrNull() ?: 0.0)} Opening Balnace"
            creditsAmount.text = "₹${String.format("%,.2f", it.accountSummary?.totalCredits?.replace(",", "")?.toDoubleOrNull() ?: 0.0)}"
            debitsAmount.text = "₹${String.format("%,.2f", it.accountSummary?.totalDebits?.replace(",", "")?.toDoubleOrNull() ?: 0.0)}"

            // Transaction analysis section
            val mostFrequentTransaction = insightsData.transactionPatternAnalysis
                ?.regularTransactions
                ?.maxByOrNull { it.amount?.toDoubleOrNull() ?: 0.0 }
            mostFrequentPayee.text = mostFrequentTransaction?.description ?: "No Transactions"
            mostFrequentPayeeAmt.text = "₹${mostFrequentTransaction?.amount ?: "0.0"}"

            val transactions = insightsData.transactionPatternAnalysis?.regularTransactions ?: emptyList()
            // Overriding with safe access transaction values and adding rupee symbol
            mostFrequentPayee.text = transactions.getOrNull(1)?.description ?: "N/A"
            mostFrequentPayeeAmt.text = if (transactions.getOrNull(1)?.type != null) "₹${transactions.getOrNull(1)?.amount}" else "N/A"

            largestTransaction.text = transactions.getOrNull(0)?.description ?: "N/A"
            largestTransactionAmt.text = if (transactions.getOrNull(0)?.amount != null) "₹${transactions.getOrNull(0)?.amount}" else "N/A"

            highestSpending.text = transactions.getOrNull(2)?.description ?: "N/A"
            highestSpendingAmt.text = if (transactions.getOrNull(2)?.amount != null) "₹${transactions.getOrNull(2)?.amount}" else "N/A"


        //    highestSpending.text = "Aarti Raviraj Desai"
          //  highestSpendingAmt.text = "100000"

            lowestBalance.text = transactions.getOrNull(3)?.description ?: "N/A"
            lowestBalanceAmt.text = if (transactions.getOrNull(3)?.amount != null) "₹${transactions.getOrNull(3)?.amount}" else "N/A"

           // lowestBalance.text = ""
            //lowestBalanceAmt.text = "525.10"
            // Transaction summary section (transaction counts remain unchanged)
            totalTransactions.text = insightsData.transaction_summary?.total_transactions.toString()
            totalTransactionsLabel.text = "Total"

            upiTransactions.text = insightsData.transaction_summary?.total_upi_transactions.toString()
            upiLabel.text = "UPI"

            impsTransactions.text = insightsData.transaction_summary?.total_imps_transactions.toString()
            impsLabel.text = "IMPS"

            testTransactions.text = insightsData.transaction_summary?.total_ach_transactions.toString()
            testLabel.text = "EMI"

            // AI suggestion section
            aiSuggestion.text = insightsData.aiRecommendation?.suggestion.toString()

            // Top 5 expenses section with rupee symbol added before each amount
            tvCategoryOne.text = insightsData.top_5_expenses?.get(0)?.category
            tvCategoryOneAmt.text = if (insightsData.top_5_expenses?.get(0)?.amount != null)
                "₹${insightsData.top_5_expenses?.get(0)?.amount}" else ""

            tvCategoryTwo.text = insightsData.top_5_expenses?.get(1)?.category
            tvCategoryTwoAmt.text = if (insightsData.top_5_expenses?.get(1)?.amount != null)
                "₹${insightsData.top_5_expenses?.get(1)?.amount}" else ""

            tvCategoryThree.text = insightsData.top_5_expenses?.get(2)?.category
            tvCategoryThreeAmt.text = if (insightsData.top_5_expenses?.get(2)?.amount != null)
                "₹${insightsData.top_5_expenses?.get(2)?.amount}" else ""

            tvCategoryFour.text = insightsData.top_5_expenses?.get(3)?.category
            tvCategoryFourAmt.text = if (insightsData.top_5_expenses?.get(3)?.amount != null)
                "₹${insightsData.top_5_expenses?.get(3)?.amount}" else ""
        }

        loadBanners()

        val request = BankStatementRequest(
            bank_statement = AccountSummary(
                Account_Summary = AccountDetails(
                    Account_Holder_Name = "RAVIRAJ KISAN DESAI",
                    Account_Number = "50100190233055",
                    Statement_Period_From = "01/07/2024",
                    Statement_Period_To = "26/07/2024",
                    Opening_Balance = "2,642.10",
                    Closing_Balance = "26,264.10",
                    Total_Debits = "215,455.00",
                    Total_Credits = "239,077.00"
                )
            )
        )

        fetchRecommendations(request)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_transactions -> true
                R.id.nav_insights -> true
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadBanners() {
        bannerViewPager = findViewById(R.id.bannerViewPager)
        bannerIndicator = findViewById(R.id.bannerIndicator)

        val banners = listOf(
            BannerModel(R.drawable.ic_credit_card, "Exclusive Credit Card Offer", "Get 5% cashback on all purchases", ""),
            BannerModel(R.drawable.ic_home, "Home Loan at 6.5%", "Low-interest rates for your dream home", ""),
            BannerModel(R.drawable.ic_person, "Instant Personal Loan", "Get approved within minutes", ""),
            BannerModel(R.drawable.ic_transaction, "AI Prediction", "Save ₹15,000 by reducing food expenses", ""),
            BannerModel(R.drawable.ic_bar, "AI Prediction", "Your monthly savings increased by 20%", "")
        )

        bannerViewPager.adapter = BannerAdapter(banners)
        bannerViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Set 80%-20% visibility
        bannerViewPager.offscreenPageLimit = 3
        bannerViewPager.setPageTransformer { page, position ->
            val scaleFactor = 0.8f + (1 - Math.abs(position)) * 0.2f
            page.scaleY = scaleFactor
        }

        // Attach indicator dots to ViewPager2
        TabLayoutMediator(bannerIndicator, bannerViewPager) { _, _ -> }.attach()

        // Start Auto-scroll
        startAutoScroll()

        val tabLayout = findViewById<TabLayout>(R.id.bannerIndicator)
        tabLayout.setTabTextColors(
            ContextCompat.getColor(this, R.color.unselected_tab),
            ContextCompat.getColor(this, R.color.selected_tab)
        )
    }



    private fun loadBannersRT(banners: List<BannerModel>) {
        bannerViewPager = findViewById(R.id.bannerViewPager)
        bannerIndicator = findViewById(R.id.bannerIndicator)

      /*  val banners = listOf(
            BannerModel(R.drawable.ic_credit_card, "Exclusive Credit Card Offer", "Get 5% cashback on all purchases", ""),
            BannerModel(R.drawable.ic_home, "Home Loan at 6.5%", "Low-interest rates for your dream home", ""),
            BannerModel(R.drawable.ic_person, "Instant Personal Loan", "Get approved within minutes", ""),
            BannerModel(R.drawable.ic_transaction, "AI Prediction", "Save ₹15,000 by reducing food expenses", ""),
            BannerModel(R.drawable.ic_bar, "AI Prediction", "Your monthly savings increased by 20%", "")
        )*/

        bannerViewPager.adapter = BannerAdapter(banners)
        bannerViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Set 80%-20% visibility
        bannerViewPager.offscreenPageLimit = 3
        bannerViewPager.setPageTransformer { page, position ->
            val scaleFactor = 0.8f + (1 - Math.abs(position)) * 0.2f
            page.scaleY = scaleFactor
        }

        // Attach indicator dots to ViewPager2
        TabLayoutMediator(bannerIndicator, bannerViewPager) { _, _ -> }.attach()

        // Start Auto-scroll
        startAutoScroll()

        val tabLayout = findViewById<TabLayout>(R.id.bannerIndicator)
        tabLayout.setTabTextColors(
            ContextCompat.getColor(this, R.color.unselected_tab),
            ContextCompat.getColor(this, R.color.selected_tab)
        )
    }


    private fun startAutoScroll() {
        val runnable = object : Runnable {
            override fun run() {
                if (currentPage == bannerViewPager.adapter?.itemCount) {
                    currentPage = 0
                }
                bannerViewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    fun maskAccountNumber(accountNumber: String?): String {
        if (accountNumber.isNullOrEmpty() || accountNumber.length < 4) return "****"
        return "*".repeat(accountNumber.length - 4) + accountNumber.takeLast(4)
    }

    private fun setupPieChartNew(pieChart: PieChart, top5Expenses: List<TopExpense>?) {
        val entries = listOf(
            PieEntry(top5Expenses?.get(0)?.amount?.toFloatOrNull() ?: 0.0f, top5Expenses?.get(0)!!.category),
            PieEntry(top5Expenses?.get(1)?.amount?.toFloatOrNull() ?: 0.0f, top5Expenses?.get(1)!!.category),
            PieEntry(top5Expenses?.get(2)?.amount?.toFloatOrNull() ?: 0.0f, top5Expenses?.get(2)!!.category),
            PieEntry(top5Expenses?.get(3)?.amount?.toFloatOrNull() ?: 0.0f, top5Expenses?.get(3)!!.category)
        )

        val dataSet = PieDataSet(entries, "Top Expenses")
        dataSet.setColors(
            ContextCompat.getColor(this, R.color.colorUPI),
            ContextCompat.getColor(this, R.color.colorCredit),
            ContextCompat.getColor(this, R.color.colorMerchant),
            ContextCompat.getColor(this, R.color.colorDebit)
        )

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Top Expenses"
        pieChart.setCenterTextSize(12f)
        pieChart.setDrawEntryLabels(false) // Hides labels
        pieChart.data.setDrawValues(false)

        pieChart.setDrawHoleEnabled(true) // Enable hole in the center
        pieChart.holeRadius = 80f // Increase to make the ring thicker (adjust as needed)
        pieChart.transparentCircleRadius = 85f // Slightly larger for a subtle transparent effect

        // Optional: Remove center text if it's not needed
        pieChart.setDrawCenterText(true)

        pieChart.legend.isEnabled = false
        pieChart.invalidate()
    }

    fun formatStatementPeriod(statementPeriod: String?): String {
        if (statementPeriod.isNullOrEmpty()) return "No Statement Period Available"

        val dateRange = statementPeriod.split(" - ")
        if (dateRange.size != 2) return statementPeriod

        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        return try {
            val startDate = inputFormat.parse(dateRange[0])
            val endDate = inputFormat.parse(dateRange[1])
            if (startDate != null && endDate != null) {
                "📅 Statement Period:${outputFormat.format(startDate)} →  ${outputFormat.format(endDate)}"
            } else {
                statementPeriod
            }
        } catch (e: Exception) {
            e.printStackTrace()
            statementPeriod
        }
    }



    private fun fetchRecommendations(request: BankStatementRequest)
    {

        // Make API Call
        val call = RetrofitClient.instance.getRecommendations(request)
        call.enqueue(object : retrofit2.Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                if (response.isSuccessful) {

                    val gson = Gson()
                    val json = gson.toJson(response.body()) // Replace with your actual object
                    Log.d("Banner_API", "onResponse: "+json)


                    response.body()?.let {

                        val banners = it.output.cross_marketing_banners.smart_suggestions.mapIndexed { index, suggestion ->
                            val logo = when (index) {
                                0 -> R.drawable.ic_credit_card
                                1 -> R.drawable.ic_home
                                2 -> R.drawable.ic_person
                                3 -> R.drawable.ic_transaction
                                4 -> R.drawable.ic_bar
                                else -> R.drawable.ic_credit_card
                            }
                            BannerModel(
                                logo,
                                suggestion.title,
                                suggestion.description,
                                suggestion.actionText
                            )

                        }

                        loadBannersRT(banners)

                        it.output.cross_marketing_banners.smart_suggestions.forEach { suggestion ->
                            println("Title: ${suggestion.title}, Description: ${suggestion.description}, Action: ${suggestion.actionText}")
                        }
                    }
                } else {
                    println("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("API Call Failed: ${t.message}")
            }
        })

    }
}
