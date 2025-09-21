package com.android.hdfcintelligence;

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.hdfcintelligence.databinding.ActivityChatBinding
import com.google.android.gms.location.FusedLocationProviderClient

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private val chatAdapter = ChatAdapter(chatMessages)
    private var bakingViewModel: BakingViewModel = BakingViewModel()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSendButton()
        setupPredefinedQueries()

        lifecycleScope.launchWhenStarted {
            bakingViewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        Log.d("ChatActivity", "Loading...")
                        Toast.makeText(this@ChatActivity, "Loading response...", Toast.LENGTH_SHORT).show()
                    }

                    is UiState.Success -> {
                        val responseText = state.outputText
                        Log.d("ChatActivity", "Response: $responseText")

                        // Add response from Gemini to chat
                        chatAdapter.addMessage(ChatMessage(responseText, false))
                        binding.recyclerView.scrollToPosition(chatMessages.size - 1)
                    }

                    is UiState.Error -> {
                        Log.e("ChatActivity", "Error: ${state.errorMessage}")
                        Toast.makeText(this@ChatActivity, "Failed to get response: ${state.errorMessage}", Toast.LENGTH_SHORT).show()
                    }

                    UiState.Initial -> {
                        Log.d("ChatActivity", "Initial...")
                        Toast.makeText(this@ChatActivity, "Initial response...", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }

       /* if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getLocation()
        }

        // Observe UI state changes using lifecycleScope

        lifecycleScope.launchWhenStarted {
            bakingViewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        Log.d("ChatActivity", "Loading...")
                        Toast.makeText(this@ChatActivity, "Loading response...", Toast.LENGTH_SHORT).show()
                    }

                    is UiState.Success -> {
                        val responseText = state.outputText
                        Log.d("ChatActivity", "Response: $responseText")

                        // Add response from Gemini to chat
                        chatAdapter.addMessage(ChatMessage(responseText, false))
                        binding.recyclerView.scrollToPosition(chatMessages.size - 1)
                    }

                    is UiState.Error -> {
                        Log.e("ChatActivity", "Error: ${state.errorMessage}")
                        Toast.makeText(this@ChatActivity, "Failed to get response: ${state.errorMessage}", Toast.LENGTH_SHORT).show()
                    }

                    UiState.Initial -> {
                        Log.d("ChatActivity", "Initial...")
                        Toast.makeText(this@ChatActivity, "Initial response...", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }*/

    }


    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Toast.makeText(this, "Lat: $latitude, Long: $longitude", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }


    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {

                if (isHdfcBankQuery(message)) {
                    bakingViewModel.sendPromptText(message)
                    chatAdapter.addMessage(ChatMessage(message, true))
                } else {
                    val errorMsg = "Sorry, I can assist only with HDFC Bank-related queries. Please ask about our banking services."
                    chatAdapter.addMessage(ChatMessage(errorMsg, false))

                }

                /*chatAdapter.addMessage(ChatMessage(message, true))
                getResponseForUserMessage(message)*/
                binding.messageInput.text.clear()

            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Optional: Handle UI changes based on input
            }
        })
    }

    private fun getResponseForUserMessage(message: String) {
        // Simple response logic
        val response = when {
            message.contains("expenses", ignoreCase = true) -> "Your total expenses this month are ₹2,450.75."
            message.contains("income", ignoreCase = true) -> "Your total income this month is ₹2,90,000.00."
            else -> "I'm here to help! Please ask about your statement."

        }

        bakingViewModel.sendPromptTextHDFC(message)

       // chatAdapter.addMessage(ChatMessage(response, false))
    }




    private fun isHdfcBankQuery(message: String): Boolean {
        val keywords = listOf(
            // General Banking
            "HDFC", "bank", "account", "balance", "savings", "current account", "passbook", "statement",
            "banking hours", "IFSC code", "branch locator", "interest rates", "SWIFT code",
            "customer care", "net banking", "online banking", "mobile banking",

            // Cards
            "credit card", "debit card", "prepaid card", "card activation", "card blocking", "lost card",
            "PIN generation", "card limit", "reward points", "card statement", "EMI conversion",

            // Loans
            "personal loan", "home loan", "auto loan", "business loan", "education loan", "loan application",
            "loan status", "EMI calculator", "interest rate", "foreclosure", "top-up loan", "mortgage",

            // Investments & Wealth Management
            "fixed deposit", "FD", "recurring deposit", "RD", "mutual fund", "SIP",
            "ULIP", "stocks", "portfolio management", "investment plans", "capital gains", "returns",

            // Digital Payments
            "UPI", "IMPS", "NEFT", "RTGS", "fund transfer", "PayZapp", "Bharat QR", "autopay",
            "bill payment", "recharge", "payment failure",

            // Security & Compliance
            "OTP", "2FA", "account freeze", "account recovery", "phishing", "scam reporting",
            "KYC", "Aadhaar linking", "PAN card linking", "SSL pinning", "OneSpan",

            // Banking Products
            "savings account", "current account", "salary account", "corporate banking",
            "NRI banking", "Demat account", "wealth account", "priority banking",

            // Insurance
            "life insurance", "health insurance", "travel insurance", "motor insurance",
            "insurance premium", "claim settlement", "policy renewal", "insurance coverage",

            // Corporate & SME Banking
            "corporate banking", "trade finance", "cash management services", "working capital loan",
            "overdraft", "invoice discounting", "forex services", "treasury solutions",

            // International Banking
            "NRI account", "foreign exchange", "remittance", "SWIFT code", "overseas investment",
            "foreign currency account",

            // Regulatory & Compliance
            "RBI guidelines", "AML", "FATCA compliance", "KYC updates", "SEBI regulations",
            "credit bureau", "CIBIL score",

            // Banking Issues
            "transaction failed", "account blocked", "card declined", "ATM not dispensing cash",
            "unauthorised transaction", "statement mismatch", "app not working"
        )
        return keywords.any { keyword -> message.contains(keyword, ignoreCase = true) }
    }

    private fun setupPredefinedQueries() {
        val predefinedQueries = listOf(
            binding.tvTotalExpenses, binding.tvTopCategories,
            binding.tvExpensesThisMonth, binding.tvRecurringPayments,
            binding.tvLastMonthReport
        )

        for (queryView in predefinedQueries) {
            queryView.setOnClickListener {
                val query = (it as TextView).text.toString()
                processUserQuery(query)
            }
        }
    }
    private fun processUserQuery(query: String) {
        chatAdapter.addMessage(ChatMessage(query, true)) // User message
        val response = when {
            query.contains("total expenses", ignoreCase = true) -> "Your total expenses this month: ₹215,455.00"
            query.contains("top categories", ignoreCase = true) -> "Top Spending Categories:\n• UPI Payments\n• IMPS Transfers\n• Fuel"
            query.contains("expenses this month", ignoreCase = true) -> "Your expenses for this month: ₹215,455.00"
            query.contains("recurring payments", ignoreCase = true) -> "Recurring Payments:\n• Renuka Hospitality\n• Neelkanth Enterprise\n• IMPS to Aarti Desai"
            query.contains("last month’s report", ignoreCase = true) -> "Last Month Report:\n• Opening Balance: ₹2,642.10\n• Closing Balance: ₹26,264.10"
            else -> "I can help with your statement. Try asking about expenses, top categories, or recurring payments."
        }
        chatAdapter.addMessage(ChatMessage(response, false)) // Bot response
    }
}