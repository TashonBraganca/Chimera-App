package com.chimera.android.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.android.data.model.AssetRanking
import com.chimera.android.data.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ResultsUiState(
    val rankings: List<AssetRanking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: String = ""
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val rankingRepository: RankingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ResultsUiState(isLoading = true))
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        observeCachedRankings()
    }
    
    private fun observeCachedRankings() {
        viewModelScope.launch {
            try {
                rankingRepository.observeCachedRankings().collect { cachedRankings ->
                    if (cachedRankings.isNotEmpty()) {
                        val rankings = cachedRankings.map { it.toAssetRanking() }
                        _uiState.value = _uiState.value.copy(
                            rankings = rankings,
                            isLoading = false,
                            lastUpdated = cachedRankings.first().createdAt
                        )
                        Timber.d("Updated UI with ${rankings.size} cached rankings")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to observe cached rankings")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load rankings: ${e.message}"
                )
            }
        }
    }
    
    fun refreshRankings() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Rankings will be refreshed through the cache observer
    }
}