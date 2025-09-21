import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FinancialInsights(
    val accountSummary: AccountSummary? = AccountSummary(),
    val transaction_summary: TransactionSummary? = TransactionSummary(), // Added transaction summary
    val top_5_expenses: List<TopExpense>? = emptyList(), // Added top 5 expenses
    val transactionPatternAnalysis: TransactionPatternAnalysis? = TransactionPatternAnalysis(),
    val aiDrivenPredictiveInsights: List<AiInsight>? = emptyList(),
    val aiRecommendation: AiRecommendation? = AiRecommendation()
) : Parcelable

@Parcelize
data class AccountSummary(
    val accountHolderName: String? = "",
    val accountNumber: String? = "",
    val statementPeriod: String? = "",
    val openingBalance: String? = "0.0",
    val closingBalance: String? = "0.0",
    val totalDebits: String? = "0.0",
    val totalCredits: String? = "0.0"
) : Parcelable

@Parcelize
data class TransactionSummary(  // New data class for transaction summary
    val total_transactions: Int? = 0,
    val total_upi_transactions: Int? = 0,
    val total_imps_transactions: Int? = 0,
    val total_ach_transactions: Int? = 0,
    val total_internal_transfer: Int? = 0,
    val total_salary_credits: Int? = 0,
    val total_rd_installments: Int? = 0
) : Parcelable

@Parcelize
data class TopExpense( // New data class for top expenses
    val category: String? = "",
    val amount: String? = "0.0"
) : Parcelable


@Parcelize
data class TransactionPatternAnalysis(
    val dominantTransactionTypes: List<String>? = emptyList(),
    val spendingAndIncomePatterns: String? = "",
    val regularTransactions: List<RegularTransaction>? = emptyList(),
    val anomalousTransactions: List<String>? = emptyList(),
    val cashFlowAnalysis: String? = ""
) : Parcelable

@Parcelize
data class RegularTransaction(
    val type: String? = "",
    val description: String? = "",
    val amount: String? = "0.0",
    val frequency: String? = ""
) : Parcelable

@Parcelize
data class AiInsight(
    val insight: String? = "",
    val explanation: String? = "",
    val personalizedFinancialProductRecommendations: String? = null
) : Parcelable

@Parcelize
data class AiRecommendation(
    val recommendation: String? = "You have a high volume of transactions with 'RENUKA HOSPITALITY S'. Review these transactions to ensure they align with your budget and financial goals.",
    val suggestion: String? = "The large salary credit is a positive sign. Consider setting up automatic transfers to savings or investment accounts to optimize your finances. You have been spending a lot on transfers and home EMIs, so fill in the spending insights to get a more accurate recommendation."
) : Parcelable