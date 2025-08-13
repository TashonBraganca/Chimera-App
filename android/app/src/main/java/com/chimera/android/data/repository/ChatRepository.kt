package com.chimera.android.data.repository

import com.chimera.android.data.model.QARequest
import com.chimera.android.data.model.QAResponse
import com.chimera.android.data.remote.api.AnalyticsApiService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: AnalyticsApiService
) {
    
    /**
     * Send question to LLM service
     */
    suspend fun askQuestion(request: QARequest): QAResponse {
        return try {
            val response = apiService.askQuestion(request)
            Timber.d("Chat response received: ${response.answer.length} chars")
            response
        } catch (e: Exception) {
            Timber.e(e, "Failed to get chat response")
            
            // Return fallback response
            QAResponse(
                question = request.question,
                answer = "I apologize, but I'm currently unable to process your question due to a connection issue. " +
                        "Please check your internet connection and try again. This service provides educational " +
                        "analysis only and is not investment advice.",
                citations = emptyList(),
                confidence = null,
                lastUpdated = java.time.Instant.now().toString(),
                responseTime = null,
                disclaimer = "Educational content only - not investment advice.",
                refusalReason = "Service temporarily unavailable"
            )
        }
    }
}