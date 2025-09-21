package com.android.hdfcintelligence

import FinancialInsights
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream

class UploadStatementActivity : AppCompatActivity() {

    private lateinit var btnAnalyze: Button
    private val bakingViewModel: BakingViewModel = BakingViewModel()
    private val TAG = "PdfTextExtractor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_statement)

        val btnSelectFile = findViewById<Button>(R.id.btn_select_file)
        btnAnalyze = findViewById(R.id.btn_analyze)
        btnAnalyze.isEnabled = false

        val loadingContainer = findViewById<View>(R.id.loadingContainer)
        val lottieAnimation = findViewById<LottieAnimationView>(R.id.lottieAnimation)

        lifecycleScope.launch {
            bakingViewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        loadingContainer.visibility = View.VISIBLE
                        lottieAnimation.playAnimation()
                    }
                    is UiState.Success -> handleSuccess(state.outputText, loadingContainer, lottieAnimation)
                    is UiState.Error -> handleError(state.errorMessage, loadingContainer, lottieAnimation)
                    UiState.Initial -> {}
                }
            }
        }

        btnSelectFile.setOnClickListener { openFilePicker() }
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { processSelectedFile(it) } ?: run {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("application/pdf"))
    }

    private fun processSelectedFile(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                val pdfText = extractTextFromPdf(it)
                Log.d(TAG, "Extracted PDF Text: $pdfText")
                sendPdfToViewModel(pdfText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file", e)
        }
    }

    private fun extractTextFromPdf(inputStream: InputStream): String {
        return try {
            val reader = PdfReader(inputStream.readBytes())
            val result = StringBuilder()
            for (i in 1..reader.numberOfPages) {
                result.append(PdfTextExtractor.getTextFromPage(reader, i)).append("\n")
            }
            reader.close()
            result.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract text from PDF", e)
            ""
        }
    }

    private fun sendPdfToViewModel(pdfText: String) {
        bakingViewModel.sendPromptText("Analyze the following bank statement and return a structured JSON: \"Analyze the following bank account statement and provide a detailed JSON-formatted analysis. The JSON output should strictly follow the given structure, maintaining consistency while dynamically filling in relevant data and regularTransactions object proper data and maintaining consistency order while dynamically filling in relevant data\n.\\n\" +\n" +
                "                        \"\\n\"+\"{\\n\" +\n" +
                "                        \"  \\\"accountSummary\\\": {\\n\" +\n" +
                "                        \"    \\\"accountHolderName\\\": \\\"Raviraj Kisan Desai\\\",\\n\" +\n" +
                "                        \"    \\\"accountNumber\\\": \\\"50100190233055\\\",\\n\" +\n" +
                "                        \"    \\\"statementPeriod\\\": \\\"01/07/2024 - 26/07/2024\\\",\\n\" +\n" +
                "                        \"    \\\"openingBalance\\\": \\\"2642.10\\\",\\n\" +\n" +
                "                        \"    \\\"closingBalance\\\": \\\"26264.10\\\",\\n\" +\n" +
                "                        \"    \\\"totalDebits\\\": \\\"215455.00\\\",\\n\" +\n" +
                "                        \"    \\\"totalCredits\\\": \\\"239077.00\\\"\\n\" +\n" +
                "                        \"  },\\n\" +\n" +
                "                        \"  \\\"transaction_summary\\\": {\\n\" +\n" +
                "                        \"    \\\"total_transactions\\\": 68,\\n\" +\n" +
                "                        \"    \\\"total_upi_transactions\\\": 45,\\n\" +\n" +
                "                        \"    \\\"total_imps_transactions\\\": 17,\\n\" +\n" +
                "                        \"    \\\"total_ach_transactions\\\": 2,\\n\" +\n" +
                "                        \"    \\\"total_internal_transfer\\\": 0,\\n\" +\n" +
                "                        \"    \\\"total_salary_credits\\\": 2,\\n\" +\n" +
                "                        \"    \\\"total_rd_installments\\\": 6\\n\" +\n" +
                "                        \"  },\\n\" +\n" +
                "                        \"  \\\"top_5_expenses\\\": [\\n\" +\n" +
                "                        \"    {\\\"category\\\": \\\"Investments\\\", \\\"amount\\\": \\\"27000\\\"},\\n\" +\n" +
                "                        \"    {\\\"category\\\": \\\"Transfers\\\", \\\"amount\\\": \\\"209100\\\"},\\n\" +\n" +
                "                        \"    {\\\"category\\\": \\\"RD Installments\\\", \\\"amount\\\": \\\"3000\\\"},\\n\" +\n" +
                "                        \"    {\\\"category\\\": \\\"Shopping\\\", \\\"amount\\\": \\\"5000\\\"},\\n\" +\n" +
                "                        \"    {\\\"category\\\": \\\"Food & Dining\\\", \\\"amount\\\": \\\"2300\\\"}\\n\" +\n" +
                "                        \"  ],\\n\" +\n" +
                "                        \"  \\\"ai_recommendation\\\": \\\"You have a high volume of transactions with 'RENUKA HOSPITALITY S' and frequent IMPS transfers to 'RAVIRAJ DESAI' and UPI transfers to 'AARTI RAVIRAJ DESAI'. Review these transactions to ensure they align with your budget and financial goals. The large salary credit is a positive sign. Consider setting up automatic transfers to savings or investment accounts to optimize your finances. After analyzing the full data, fill in the spending insights to get a more accurate recommendation.\\\",\\n\" +\n" +
                "                        \"  \\\"transactionPatternAnalysis\\\": {\\n\" +\n" +
                "                        \"    \\\"dominantTransactionTypes\\\": [\\n\" +\n" +
                "                        \"      \\\"UPI\\\",\\n\" +\n" +
                "                        \"      \\\"IMPS\\\",\\n\" +\n" +
                "                        \"      \\\"ACH D\\\"\\n\" +\n" +
                "                        \"    ],\\n\" +\n" +
                "                        \"    \\\"spendingAndIncomePatterns\\\": \\\"Income mainly from salary. Spending predominantly on UPI transactions (Renuka Hospitality, fuel), transfer to another account and recurring ACH debits.\\\",\\n\" +\n" +
                "                        \"    \\\"\"regularTransactions\": [\n" +
                "      {\n" +
                // regularTransactions maintaining consistency order while dynamically filling in relevant data
                // 1.Largest Transaction statement. Refer Col No 6 Col: Deposit Amt
                "        \"type\": \"Largest Transaction credited\",\n" +
                "        \"description\": \"HDFC BANK SALARY\",\n" +
                "        \"amount\": \"200,140.00\",\n" +
                "        \"frequency\": \"Complete Statement\"\n" +
                "      },\n" +
                "      {\n" +
                // 2.Most Frequent Payee statement.
                "        \"type\": \"Most Frequent Payee in statement\",\n" +
                "        \"description\": \"EMI 151736844\",\n" +
                "        \"amount\": \"13,001.00\",\n" +
                "        \"frequency\": \"Complete Statement\"\n" +
                "      },\n" +
                "      {\n" +
                // 3.highest Spending the statement. Refer Col No 5 Col: Withdrawal Amt
                "        \"type\": \"Highest Spending in statement\",\n" +
                "        \"description\": \"Flipkart\",\n" +
                "        \"amount\": \"89000.00\",\n" +
                "        \"frequency\": \"Complete Statement\"\n" +
                "      },\n" +
                "      {\n" +
                // 4.Lowest Balance Refer Col No 7 Col: Closing Balance
                "        \"type\": \"Lowest Balance in Column named Closing Balance statement\",\n" +
                "        \"description\": \"Closing Balance\",\n" +
                "        \"amount\": \"10000.00\",\n" +
                "        \"frequency\": \"Complete Statement\"\n" +
                "      }\n" +
                "    ]" +
                "\"    ],\\n\" +\n" +
                "                        \"    \\\"anomalousTransactions\\\": [\\n\" +\n" +
                "                        \"      \\\"ACH Debit Return Charges\\\",\\n\" +\n" +
                "                        \"      \\\"Multiple UPI transactions to Renuka Hospitality S\\\"\\n\" +\n" +
                "                        \"    ],\\n\" +\n" +
                "                        \"    \\\"cashFlowAnalysis\\\": \\\"Strong cash flow this month, with a significant increase in closing balance due to salary credit. Heavy reliance on UPI for expenses.\\\"\\n\" +\n" +
                "                        \"  },\\n\" +\n" +
                "                        \"  \\\"aiDrivenPredictiveInsights\\\": [\\n\" +\n" +
                "                        \"    {\\n\" +\n" +
                "                        \"      \\\"insight\\\": \\\"Increased Savings Opportunities\\\",\\n\" +\n" +
                "                        \"      \\\"explanation\\\": \\\"Significant salary credit suggests potential for increased savings or investments.\\\"\\n\" +\n" +
                "                        \"    },\\n\" +\n" +
                "                        \"    {\\n\" +\n" +
                "                        \"      \\\"insight\\\": \\\"Potential Budget Review\\\",\\n\" +\n" +
                "                        \"      \\\"explanation\\\": \\\"High frequency of small UPI transactions, especially to Renuka Hospitality, indicates a need to review spending habits.\\\"\\n\" +\n" +
                "                        \"    },\\n\" +\n" +
                "                        \"    {\\n\" +\n" +
                "                        \"      \\\"insight\\\": \\\"Risk of Fees\\\",\\n\" +\n" +
                "                        \"      \\\"explanation\\\": \\\"Past ACH debit return charges suggest monitoring account balance to avoid insufficient funds fees.\\\"\\n\" +\n" +
                "                        \"    }\\n\" +\n" +
                "                        \"  ]\\n\" +\n" +
                "                        \"}\"+text.toString())\n$pdfText")
    }

    private fun handleSuccess(responseText: String, loadingContainer: View, lottieAnimation: LottieAnimationView) {
        lottieAnimation.cancelAnimation()
        loadingContainer.visibility = View.GONE
        try {
            val cleanedJson = cleanJsonResponse(responseText)
            val insightsData: FinancialInsights? = parseFinancialInsights(cleanedJson)
            if (insightsData != null) {
                val intent = Intent(this, GAIFinancialInsightsActivity::class.java)
                intent.putExtra("financialInsights", insightsData)
                startActivity(intent)
                finish()
            } else {
                Log.e("JSON_ERROR", "Invalid JSON format")
            }
        } catch (e: Exception) {
            Log.e("JSON_ERROR", "Invalid JSON format: ${e.message}")
        }
    }

    private fun handleError(errorMessage: String, loadingContainer: View, lottieAnimation: LottieAnimationView) {
        lottieAnimation.cancelAnimation()
        loadingContainer.visibility = View.GONE
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun cleanJsonResponse(response: String?): String? {
        response ?: return null
        val startIndex = response.indexOf("{")
        val endIndex = response.lastIndexOf("}")
        return if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            response.substring(startIndex, endIndex + 1).trim()
        } else {
            null
        }
    }

    private fun parseFinancialInsights(cleanedJson: String?): FinancialInsights? {
        val validJson = cleanJsonResponse(cleanedJson) ?: return null
        return try {
            val gson = Gson()
            val jsonObject = JSONObject(validJson)
            gson.fromJson(jsonObject.toString(), FinancialInsights::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
