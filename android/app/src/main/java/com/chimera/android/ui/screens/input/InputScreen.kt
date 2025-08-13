package com.chimera.android.ui.screens.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.android.AppState
import com.chimera.android.data.model.AssetType
import com.chimera.android.data.model.RankingRequest
import com.chimera.android.ui.components.DisclaimerCard
import com.chimera.android.ui.components.LoadingButton
import com.chimera.android.ui.theme.ChimeraTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    appState: AppState,
    onAppStateChange: (AppState) -> Unit,
    onNavigateToResults: () -> Unit,
    viewModel: InputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var amountText by remember { mutableStateOf("100000") }
    var selectedHorizonIndex by remember { mutableIntStateOf(1) } // Default to 1 month
    var selectedAssetType by remember { mutableStateOf(AssetType.EQUITY) }
    var selectedRiskProfile by remember { mutableIntStateOf(1) } // Balanced
    
    val horizonOptions = listOf("1 Week", "1 Month", "3 Months", "6 Months", "1 Year")
    val horizonDays = listOf(7, 30, 90, 180, 365)
    val assetTypeOptions = listOf("Equity", "Mutual Funds")
    val riskProfiles = listOf("Conservative", "Balanced", "Aggressive")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chimera Investment Analysis",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Investment trends",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Investment Amount Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Investment Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            // Only allow digits and remove non-numeric characters
                            val cleanValue = newValue.filter { it.isDigit() }
                            if (cleanValue.length <= 10) { // Max 10 digits
                                amountText = cleanValue
                            }
                        },
                        label = { Text("Amount in ₹") },
                        prefix = { Text("₹ ") },
                        placeholder = { Text("100,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Investment amount in rupees" },
                        supportingText = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            val formatted = NumberFormat.getNumberInstance(Locale("en", "IN"))
                                .format(amount)
                            Text("₹ $formatted")
                        }
                    )
                }
            }

            // Investment Horizon Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Investment Horizon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        horizonOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = horizonOptions.size
                                ),
                                onClick = { selectedHorizonIndex = index },
                                selected = index == selectedHorizonIndex,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Asset Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Asset Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        assetTypeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = assetTypeOptions.size
                                ),
                                onClick = {
                                    selectedAssetType = if (index == 0) AssetType.EQUITY else AssetType.MUTUAL_FUND
                                },
                                selected = (index == 0 && selectedAssetType == AssetType.EQUITY) ||
                                         (index == 1 && selectedAssetType == AssetType.MUTUAL_FUND),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = option,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Risk Profile Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Risk Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        riskProfiles.forEachIndexed { index, profile ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = riskProfiles.size
                                ),
                                onClick = { selectedRiskProfile = index },
                                selected = index == selectedRiskProfile,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = profile,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Disclaimer
            DisclaimerCard(
                isAcknowledged = appState.disclaimerAcknowledged,
                onAcknowledge = {
                    onAppStateChange(appState.copy(disclaimerAcknowledged = true))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            LoadingButton(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount >= 1000) {
                        val request = RankingRequest(
                            amountInr = amount,
                            horizonDays = horizonDays[selectedHorizonIndex],
                            assetType = selectedAssetType,
                            riskPreference = riskProfiles[selectedRiskProfile].lowercase(),
                            topK = 20
                        )
                        
                        onAppStateChange(appState.copy(lastRankingRequest = request))
                        viewModel.submitRankingRequest(request)
                        onNavigateToResults()
                    }
                },
                enabled = amountText.isNotBlank() && 
                         (amountText.toLongOrNull() ?: 0L) >= 1000 &&
                         appState.disclaimerAcknowledged,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (uiState.isLoading) "Generating Rankings..." else "Get Investment Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Show offline banner
            if (!appState.isOnline) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "You're offline. Using cached data from ${appState.lastUpdateTime}.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InputScreenPreview() {
    ChimeraTheme {
        InputScreen(
            appState = AppState(disclaimerAcknowledged = true),
            onAppStateChange = { },
            onNavigateToResults = { }
        )
    }
}