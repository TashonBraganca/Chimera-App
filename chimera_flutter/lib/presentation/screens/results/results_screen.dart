import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../../providers/app_providers.dart';
import '../../widgets/glass_app_bar.dart';
import '../../../data/models/ranking_models.dart';
import '../../../core/theme/app_theme.dart';

class ResultsScreen extends ConsumerWidget {
  final Map<String, dynamic> requestData;

  const ResultsScreen({
    super.key,
    required this.requestData,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final rankingState = ref.watch(rankingStateProvider);

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          // Glass App Bar
          GlassAppBar(
            title: 'Investment Rankings',
            backgroundColor: colorScheme.primary,
            leading: IconButton(
              icon: Icon(Icons.arrow_back_rounded, color: colorScheme.onPrimary),
              onPressed: () => context.pop(),
            ),
            actions: [
              IconButton(
                icon: Icon(Icons.chat_rounded, color: colorScheme.onPrimary),
                onPressed: () => context.goNamed('chat'),
              ),
            ],
          ),

          // Content
          if (rankingState.isLoading)
            SliverFillRemaining(
              child: _buildLoadingState(context, colorScheme),
            )
          else if (rankingState.error != null)
            SliverFillRemaining(
              child: _buildErrorState(context, ref, rankingState.error!, colorScheme),
            )
          else
            SliverPadding(
              padding: const EdgeInsets.all(20),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  // Summary card
                  _buildSummaryCard(context, rankingState, colorScheme)
                      .animate()
                      .fadeIn(duration: 600.ms)
                      .slideY(begin: 0.3, end: 0),

                  const SizedBox(height: 16),

                  // Rankings list
                  ...rankingState.rankings.asMap().entries.map((entry) {
                    final index = entry.key;
                    final ranking = entry.value;
                    return _buildRankingCard(context, ranking, colorScheme)
                        .animate()
                        .fadeIn(duration: 600.ms, delay: (200 + index * 100).ms)
                        .slideX(begin: 0.3, end: 0);
                  }).toList(),

                  const SizedBox(height: 16),

                  // Disclaimer and metadata
                  if (rankingState.metadata != null)
                    _buildMetadataCard(context, rankingState.metadata!, colorScheme)
                        .animate()
                        .fadeIn(duration: 600.ms, delay: 800.ms),

                  const SizedBox(height: 20),
                ]),
              ),
            ),
        ],
      ),
      floatingActionButton: rankingState.rankings.isNotEmpty
          ? FloatingActionButton.extended(
              onPressed: () => context.goNamed('chat'),
              icon: const Icon(Icons.psychology_rounded),
              label: const Text('Ask AI'),
              backgroundColor: colorScheme.tertiaryContainer,
              foregroundColor: colorScheme.onTertiaryContainer,
            ).animate()
                .fadeIn(duration: 600.ms, delay: 1000.ms)
                .scale(begin: const Offset(0.8, 0.8))
          : null,
    );
  }

  Widget _buildLoadingState(BuildContext context, ColorScheme colorScheme) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SizedBox(
            width: 60,
            height: 60,
            child: CircularProgressIndicator(
              strokeWidth: 3,
              valueColor: AlwaysStoppedAnimation(colorScheme.primary),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'Analyzing Investments...',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: colorScheme.onSurface,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'This may take a few seconds',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      )
          .animate(onPlay: (controller) => controller.repeat())
          .shimmer(duration: 2000.ms, color: colorScheme.primary.withOpacity(0.1)),
    );
  }

  Widget _buildErrorState(BuildContext context, WidgetRef ref, String error, ColorScheme colorScheme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline_rounded,
              size: 64,
              color: colorScheme.error,
            ),
            const SizedBox(height: 24),
            Text(
              'Unable to Load Rankings',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                color: colorScheme.error,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              error,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: () {
                ref.read(rankingStateProvider.notifier).clearError();
                context.pop();
              },
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Try Again'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCard(BuildContext context, RankingState rankingState, ColorScheme colorScheme) {
    final currencyFormat = NumberFormat.currency(
      locale: 'en_IN',
      symbol: '₹',
      decimalDigits: 0,
    );

    return Card(
      elevation: 4,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              colorScheme.primaryContainer.withOpacity(0.8),
              colorScheme.secondaryContainer.withOpacity(0.6),
            ],
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.insights_rounded,
                  color: colorScheme.primary,
                  size: 28,
                ),
                const SizedBox(width: 12),
                Text(
                  'Analysis Complete',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            Row(
              children: [
                Expanded(
                  child: _buildSummaryItem(
                    context,
                    'Assets Analyzed',
                    '${rankingState.rankings.length}',
                    Icons.list_alt_rounded,
                    colorScheme,
                  ),
                ),
                Expanded(
                  child: _buildSummaryItem(
                    context,
                    'Investment Amount',
                    currencyFormat.format(requestData['amountInr'] ?? 0),
                    Icons.currency_rupee_rounded,
                    colorScheme,
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 12),
            
            Row(
              children: [
                Expanded(
                  child: _buildSummaryItem(
                    context,
                    'Time Horizon',
                    '${requestData['horizonDays'] ?? 0} days',
                    Icons.schedule_rounded,
                    colorScheme,
                  ),
                ),
                Expanded(
                  child: _buildSummaryItem(
                    context,
                    'Risk Profile',
                    '${requestData['riskPreference'] ?? 'N/A'}'.toLowerCase().capitalize(),
                    Icons.psychology_rounded,
                    colorScheme,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryItem(BuildContext context, String label, String value, IconData icon, ColorScheme colorScheme) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: colorScheme.surface.withOpacity(0.8),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 16, color: colorScheme.primary),
              const SizedBox(width: 6),
              Text(
                label,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: colorScheme.onSurface,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRankingCard(BuildContext context, AssetRanking ranking, ColorScheme colorScheme) {
    final currencyFormat = NumberFormat.currency(
      locale: 'en_IN',
      symbol: '₹',
      decimalDigits: 2,
    );
    
    final changeColor = ranking.change.startsWith('+') ? AppTheme.profitGreen : AppTheme.lossRed;
    final recommendationColor = _getRecommendationColor(ranking.recommendation);

    return Card(
      elevation: 2,
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          // Navigate to chat with pre-filled question about this asset
          context.goNamed('chat');
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header row
              Row(
                children: [
                  // Rank badge
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: colorScheme.primary,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Center(
                      child: Text(
                        '#${ranking.rank}',
                        style: TextStyle(
                          color: colorScheme.onPrimary,
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                    ),
                  ),
                  
                  const SizedBox(width: 12),
                  
                  // Asset info
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          ranking.symbol,
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: colorScheme.onSurface,
                          ),
                        ),
                        Text(
                          ranking.name,
                          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurfaceVariant,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                  
                  // Recommendation badge
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: recommendationColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: recommendationColor.withOpacity(0.3)),
                    ),
                    child: Text(
                      ranking.recommendation,
                      style: TextStyle(
                        color: recommendationColor,
                        fontWeight: FontWeight.w600,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              
              const SizedBox(height: 16),
              
              // Metrics row
              Row(
                children: [
                  Expanded(
                    child: _buildMetricItem(
                      context,
                      'Score',
                      '${(ranking.score * 100).toStringAsFixed(0)}%',
                      Icons.star_rounded,
                      colorScheme.primary,
                    ),
                  ),
                  Expanded(
                    child: _buildMetricItem(
                      context,
                      'Confidence',
                      '${ranking.confidence}%',
                      Icons.trending_up_rounded,
                      colorScheme.tertiary,
                    ),
                  ),
                  Expanded(
                    child: _buildMetricItem(
                      context,
                      'Price',
                      currencyFormat.format(ranking.lastPrice),
                      Icons.currency_rupee_rounded,
                      colorScheme.secondary,
                    ),
                  ),
                  Expanded(
                    child: _buildMetricItem(
                      context,
                      'Change',
                      ranking.change,
                      ranking.change.startsWith('+') ? Icons.arrow_upward_rounded : Icons.arrow_downward_rounded,
                      changeColor,
                    ),
                  ),
                ],
              ),
              
              const SizedBox(height: 12),
              
              // Confidence indicator
              LinearProgressIndicator(
                value: ranking.confidence / 100,
                backgroundColor: colorScheme.surfaceContainerHigh,
                valueColor: AlwaysStoppedAnimation(colorScheme.primary),
                minHeight: 4,
                borderRadius: BorderRadius.circular(2),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildMetricItem(BuildContext context, String label, String value, IconData icon, Color color) {
    return Column(
      children: [
        Icon(icon, size: 16, color: color),
        const SizedBox(height: 4),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: Theme.of(context).textTheme.labelMedium?.copyWith(
            fontWeight: FontWeight.w600,
            color: Theme.of(context).colorScheme.onSurface,
          ),
        ),
      ],
    );
  }

  Widget _buildMetadataCard(BuildContext context, RankingMetadata metadata, ColorScheme colorScheme) {
    return Card(
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.info_outline_rounded, color: colorScheme.primary, size: 20),
                const SizedBox(width: 8),
                Text(
                  'Analysis Details',
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            
            Text(
              'Last Updated: ${metadata.lastUpdated}',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Data Source: ${metadata.dataSource}',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
            
            const SizedBox(height: 12),
            
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerHighest.withOpacity(0.5),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: colorScheme.outline.withOpacity(0.2)),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(
                    Icons.warning_amber_rounded,
                    color: colorScheme.onSurfaceVariant,
                    size: 16,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      metadata.disclaimer,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getRecommendationColor(String recommendation) {
    switch (recommendation.toUpperCase()) {
      case 'BUY':
        return AppTheme.profitGreen;
      case 'HOLD':
        return AppTheme.warningAmber;
      case 'SELL':
        return AppTheme.lossRed;
      default:
        return Colors.grey;
    }
  }
}

extension StringCapitalize on String {
  String capitalize() {
    if (isEmpty) return this;
    return '${this[0].toUpperCase()}${substring(1)}';
  }
}