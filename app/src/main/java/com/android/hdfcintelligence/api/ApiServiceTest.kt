package com.android.hdfcintelligence.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServiceTest {
    @POST("recommendations")
    fun getRecommendations(@Body request: BankStatementRequest): Call<ApiResponse>
}

