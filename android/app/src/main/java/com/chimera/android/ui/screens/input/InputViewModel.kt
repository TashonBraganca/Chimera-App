package com.chimera.android.ui.screens.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.android.data.model.RankingRequest
import com.chimera.android.data.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class InputUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InputViewModel @Inject constructor(
    private val rankingRepository: RankingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()
    
    fun submitRankingRequest(request: RankingRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Pre-fetch rankings to cache them
                rankingRepository.getRankings(request)
                _uiState.value = _uiState.value.copy(isLoading = false)
                Timber.d("Rankings pre-fetched successfully for navigation")
            } catch (e: Exception) {
                Timber.e(e, "Failed to pre-fetch rankings")
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = "Failed to load rankings: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}