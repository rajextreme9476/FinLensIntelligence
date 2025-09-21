package com.android.hdfcintelligence.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

// Define Request Data Model
data class BankStatementRequest(
    val bank_statement: AccountSummary
)

data class AccountSummary(
    val Account_Summary: AccountDetails
)

data class AccountDetails(
    val Account_Holder_Name: String,
    val Account_Number: String,
    val Statement_Period_From: String,
    val Statement_Period_To: String,
    val Opening_Balance: String,
    val Closing_Balance: String,
    val Total_Debits: String,
    val Total_Credits: String
)

// Define Response Data Model
data class ApiResponse(
    val output: CrossMarketingBanners
)

data class CrossMarketingBanners(
    val cross_marketing_banners: SmartSuggestions
)

data class SmartSuggestions(
    val smart_suggestions: List<Suggestion>
)

data class Suggestion(
    val title: String,
    val description: String,
    val actionText: String
)

// Retrofit API Interface
interface ApiService {
    @POST("recommendations")
    fun getRecommendations(@Body request: BankStatementRequest): Call<ApiResponse>
}

// Retrofit Client Setup
object RetrofitClient {
    private const val BASE_URL = "https://dc1g600ge8.execute-api.ap-south-1.amazonaws.com/"

    val instance: ApiServiceTest by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiServiceTest::class.java)
    }
}

// Usage Example (in Coroutine Scope or Background Thread)
suspend fun fetchRecommendations() {
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

    val call = RetrofitClient.instance.getRecommendations(request)

    call.enqueue(object : retrofit2.Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
            if (response.isSuccessful) {
                response.body()?.let {
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
