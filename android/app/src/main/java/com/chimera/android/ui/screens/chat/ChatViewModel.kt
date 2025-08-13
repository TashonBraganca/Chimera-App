package com.chimera.android.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.android.data.model.ChatMessage
import com.chimera.android.data.model.MessageType
import com.chimera.android.data.model.QARequest
import com.chimera.android.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Add user message
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = content,
                    type = MessageType.USER,
                    timestamp = timeFormatter.format(Date())
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + userMessage,
                    isLoading = true,
                    error = null
                )
                
                // Send to backend
                val request = QARequest(question = content)
                val response = chatRepository.askQuestion(request)
                
                // Add assistant response
                val assistantMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = response.answer,
                    type = MessageType.ASSISTANT,
                    timestamp = timeFormatter.format(Date()),
                    citations = response.citations,
                    confidence = response.confidence
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isLoading = false
                )
                
                Timber.d("Chat message sent and response received")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to send chat message")
                
                // Add error message
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "I'm sorry, I encountered an error processing your question. Please try again or check your connection.",
                    type = MessageType.ASSISTANT,
                    timestamp = timeFormatter.format(Date()),
                    citations = emptyList()
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearChat() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }
}