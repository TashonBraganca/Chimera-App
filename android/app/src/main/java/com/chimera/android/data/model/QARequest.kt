package com.chimera.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QARequest(
    val question: String,
    val assetId: String? = null,
    val category: String? = null
)

@Serializable
data class QAResponse(
    val status: String,
    val answer: String,
    val citations: List<BackendCitation>,
    val confidence: Int,
    val lastUpdated: String,
    val disclaimer: String
)

@Serializable
data class BackendCitation(
    val source: String,
    val date: String,
    val title: String
)