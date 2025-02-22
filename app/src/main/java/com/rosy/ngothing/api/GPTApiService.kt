package com.rosy.ngothing.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GPTApiService {
    @Headers("Content-Type: application/json", "Authorization: Bearer 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")
    @POST("v1/chat/completions")
    fun translateCode(@Body requestBody: GPTChatRequest): Call<GPTChatResponse>
}
