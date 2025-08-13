package com.chimera.android.ui.screens.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.android.AppState
import com.chimera.android.data.model.AssetRanking
import com.chimera.android.data.model.AssetType
import com.chimera.android.data.model.RankingRequest
import com.chimera.android.ui.components.DisclaimerCard
import com.chimera.android.ui.theme.ChimeraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    appState: AppState,
    onAppStateChange: (AppState) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabOptions = listOf("All", "Top 10", "High Confidence", "Recent Movers")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Investment Rankings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open filters */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter results"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToChat("") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = "Open chat for questions" }
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Ask questions",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appState.lastRankingRequest?.let { request ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Amount: ₹${formatAmount(request.amountInr)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${request.horizonDays} days • ${request.riskPreference}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (uiState.rankings.isNotEmpty()) {
                        Text(
                            text = "Found ${uiState.rankings.size} recommendations • Last updated: ${uiState.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                edgePadding = 0.dp
            ) {
                tabOptions.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Content based on loading state
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Analyzing market data...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading rankings",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        uiState.error?.let { errorMessage ->
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                uiState.rankings.isEmpty() && !uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No rankings available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                else -> {
                    // Filter rankings based on selected tab
                    val filteredRankings = when (selectedTabIndex) {
                        0 -> uiState.rankings // All
                        1 -> uiState.rankings.take(10) // Top 10
                        2 -> uiState.rankings.filter { it.confidencePercent >= 80.0 } // High confidence
                        3 -> uiState.rankings.sortedByDescending { it.returnScore ?: 0.0 }.take(15) // Recent movers
                        else -> uiState.rankings
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredRankings) { index, ranking ->
                            AssetRankingCard(
                                ranking = ranking,
                                onClick = { onNavigateToChat(ranking.assetId) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Disclaimer at bottom
                        item {
                            DisclaimerCard(
                                isAcknowledged = true,
                                onAcknowledge = { },
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetRankingCard(
    ranking: AssetRanking,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with rank and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${ranking.rank}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ranking.assetName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Confidence indicator
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val confidenceColor = when {
                        ranking.confidencePercent >= 90 -> Color(0xFF4CAF50)
                        ranking.confidencePercent >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFFE91E63)
                    }
                    
                    CircularProgressIndicator(
                        progress = { (ranking.confidencePercent / 100).toFloat() },
                        modifier = Modifier.size(32.dp),
                        color = confidenceColor,
                        strokeWidth = 3.dp
                    )
                    
                    Text(
                        text = "${ranking.confidencePercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Score breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreIndicator(
                    label = "Return",
                    score = ranking.returnScore ?: 0.0,
                    icon = when {
                        (ranking.returnScore ?: 0.0) > 0.3 -> Icons.Default.TrendingUp
                        (ranking.returnScore ?: 0.0) < -0.3 -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    }
                )
                
                ScoreIndicator(
                    label = "Risk",
                    score = -(ranking.volatilityScore ?: 0.0), // Invert for display
                    icon = Icons.Default.TrendingFlat
                )
                
                ScoreIndicator(
                    label = "Momentum",
                    score = ranking.momentumScore ?: 0.0,
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun ScoreIndicator(
    label: String,
    score: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label score",
            modifier = Modifier.size(16.dp),
            tint = when {
                score > 0.2 -> MaterialTheme.colorScheme.primary
                score < -0.2 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = "${(score * 100).toInt()}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatAmount(amount: Long): String {
    return when {
        amount >= 10000000 -> "${amount / 10000000}Cr"
        amount >= 100000 -> "${amount / 100000}L"
        amount >= 1000 -> "${amount / 1000}K"
        else -> amount.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    ChimeraTheme {
        ResultsScreen(
            appState = AppState(
                lastRankingRequest = RankingRequest(
                    amountInr = 100000,
                    horizonDays = 30,
                    assetType = AssetType.EQUITY,
                    riskPreference = "balanced"
                )
            ),
            onAppStateChange = { },
            onNavigateToChat = { },
            onNavigateBack = { }
        )
    }
}