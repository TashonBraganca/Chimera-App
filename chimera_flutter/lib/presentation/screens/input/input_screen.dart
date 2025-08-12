import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../providers/app_providers.dart';
import '../../widgets/glass_app_bar.dart';
import '../../widgets/connection_status_indicator.dart';
import 'package:flutter_animate/flutter_animate.dart';

class InputScreen extends ConsumerWidget {
  const InputScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final formState = ref.watch(investmentFormProvider);
    final connectionStatus = ref.watch(connectionStatusProvider);
    final isLoading = ref.watch(rankingStateProvider).isLoading;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          // Glass App Bar with gradient
          GlassAppBar(
            title: 'Investment Analysis',
            backgroundColor: colorScheme.primary,
            actions: [
              ConnectionStatusIndicator(status: connectionStatus),
            ],
          ),
          
          SliverPadding(
            padding: const EdgeInsets.all(20),
            sliver: SliverList(
              delegate: SliverChildListDelegate([
                // Hero section
                _buildHeroSection(context, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms)
                    .slideY(begin: 0.3, end: 0),
                
                const SizedBox(height: 32),
                
                // Amount input section
                _buildAmountSection(context, ref, formState, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms, delay: 200.ms)
                    .slideY(begin: 0.3, end: 0),
                
                const SizedBox(height: 24),
                
                // Investment horizon section
                _buildHorizonSection(context, ref, formState, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms, delay: 400.ms)
                    .slideY(begin: 0.3, end: 0),
                
                const SizedBox(height: 24),
                
                // Risk preference section
                _buildRiskSection(context, ref, formState, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms, delay: 600.ms)
                    .slideY(begin: 0.3, end: 0),
                
                const SizedBox(height: 32),
                
                // Action button
                _buildAnalyzeButton(context, ref, formState, isLoading, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms, delay: 800.ms)
                    .slideY(begin: 0.3, end: 0),
                
                const SizedBox(height: 24),
                
                // Disclaimer
                _buildDisclaimer(context, colorScheme)
                    .animate()
                    .fadeIn(duration: 600.ms, delay: 1000.ms),
              ]),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeroSection(BuildContext context, ColorScheme colorScheme) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            colorScheme.primaryContainer.withOpacity(0.8),
            colorScheme.secondaryContainer.withOpacity(0.6),
          ],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: colorScheme.shadow.withOpacity(0.1),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.trending_up_rounded,
            size: 48,
            color: colorScheme.primary,
          ),
          const SizedBox(height: 16),
          Text(
            'AI-Powered Asset Rankings',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: colorScheme.onPrimaryContainer,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Get personalized investment recommendations based on your risk profile and investment horizon.',
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
              color: colorScheme.onPrimaryContainer.withOpacity(0.8),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAmountSection(BuildContext context, WidgetRef ref, InvestmentFormState formState, ColorScheme colorScheme) {
    final currencyFormat = NumberFormat.currency(
      locale: 'en_IN',
      symbol: '₹',
      decimalDigits: 0,
    );

    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.currency_rupee_rounded,
                  color: colorScheme.primary,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Text(
                  'Investment Amount',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            // Amount display
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerHigh,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Amount:',
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                  Text(
                    currencyFormat.format(formState.amount),
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: colorScheme.primary,
                    ),
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Amount slider
            Slider(
              value: formState.amount,
              min: 10000,
              max: 10000000,
              divisions: 100,
              label: currencyFormat.format(formState.amount),
              onChanged: (value) {
                HapticFeedback.selectionClick();
                ref.read(investmentFormProvider.notifier).updateAmount(value);
              },
            ),
            
            // Quick amount buttons
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              children: [
                _buildQuickAmountChip(context, ref, '₹1L', 100000, formState.amount == 100000),
                _buildQuickAmountChip(context, ref, '₹5L', 500000, formState.amount == 500000),
                _buildQuickAmountChip(context, ref, '₹10L', 1000000, formState.amount == 1000000),
                _buildQuickAmountChip(context, ref, '₹50L', 5000000, formState.amount == 5000000),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickAmountChip(BuildContext context, WidgetRef ref, String label, double amount, bool isSelected) {
    final colorScheme = Theme.of(context).colorScheme;
    
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (_) {
        HapticFeedback.selectionClick();
        ref.read(investmentFormProvider.notifier).updateAmount(amount);
      },
      backgroundColor: colorScheme.surfaceContainerHighest,
      selectedColor: colorScheme.primaryContainer,
    );
  }

  Widget _buildHorizonSection(BuildContext context, WidgetRef ref, InvestmentFormState formState, ColorScheme colorScheme) {
    final horizonOptions = [
      (1, '1 Day', Icons.today_rounded),
      (7, '1 Week', Icons.calendar_view_week_rounded),
      (30, '1 Month', Icons.calendar_month_rounded),
      (90, '3 Months', Icons.calendar_view_month_rounded),
      (180, '6 Months', Icons.date_range_rounded),
      (365, '1 Year', Icons.event_available_rounded),
    ];

    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.schedule_rounded,
                  color: colorScheme.primary,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Text(
                  'Investment Horizon',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: horizonOptions.map((option) {
                final (days, label, icon) = option;
                final isSelected = formState.horizonDays == days;
                
                return FilterChip(
                  avatar: Icon(icon, size: 20),
                  label: Text(label),
                  selected: isSelected,
                  onSelected: (_) {
                    HapticFeedback.selectionClick();
                    ref.read(investmentFormProvider.notifier).updateHorizon(days);
                  },
                  backgroundColor: colorScheme.surfaceContainerHighest,
                  selectedColor: colorScheme.primaryContainer,
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRiskSection(BuildContext context, WidgetRef ref, InvestmentFormState formState, ColorScheme colorScheme) {
    final riskOptions = [
      ('CONSERVATIVE', 'Conservative', Icons.security_rounded, Colors.green),
      ('MODERATE', 'Moderate', Icons.balance_rounded, Colors.orange),
      ('AGGRESSIVE', 'Aggressive', Icons.trending_up_rounded, Colors.red),
    ];

    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.psychology_rounded,
                  color: colorScheme.primary,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Text(
                  'Risk Preference',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            Column(
              children: riskOptions.map((option) {
                final (value, label, icon, color) = option;
                final isSelected = formState.riskPreference == value;
                
                return Container(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: isSelected ? color.withOpacity(0.2) : colorScheme.surfaceContainerHigh,
                      child: Icon(icon, color: isSelected ? color : colorScheme.onSurfaceVariant),
                    ),
                    title: Text(
                      label,
                      style: TextStyle(
                        fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                        color: isSelected ? colorScheme.primary : null,
                      ),
                    ),
                    subtitle: Text(_getRiskDescription(value)),
                    trailing: isSelected ? Icon(Icons.check_circle, color: colorScheme.primary) : null,
                    selected: isSelected,
                    onTap: () {
                      HapticFeedback.selectionClick();
                      ref.read(investmentFormProvider.notifier).updateRiskPreference(value);
                    },
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }

  String _getRiskDescription(String risk) {
    switch (risk) {
      case 'CONSERVATIVE':
        return 'Lower risk, stable returns';
      case 'MODERATE':
        return 'Balanced risk-reward profile';
      case 'AGGRESSIVE':
        return 'Higher risk, potential for higher returns';
      default:
        return '';
    }
  }

  Widget _buildAnalyzeButton(BuildContext context, WidgetRef ref, InvestmentFormState formState, bool isLoading, ColorScheme colorScheme) {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: isLoading ? null : () => _analyzeInvestment(context, ref, formState),
        style: ElevatedButton.styleFrom(
          padding: const EdgeInsets.all(20),
          backgroundColor: colorScheme.primary,
          foregroundColor: colorScheme.onPrimary,
          elevation: 4,
          shadowColor: colorScheme.primary.withOpacity(0.3),
        ),
        child: isLoading
            ? Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      color: colorScheme.onPrimary,
                      strokeWidth: 2,
                    ),
                  ),
                  const SizedBox(width: 16),
                  const Text('Analyzing...'),
                ],
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.auto_graph_rounded, size: 24),
                  const SizedBox(width: 12),
                  const Text(
                    'Analyze Investments',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
      ),
    );
  }

  Widget _buildDisclaimer(BuildContext context, ColorScheme colorScheme) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerHighest.withOpacity(0.5),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: colorScheme.outline.withOpacity(0.2),
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.info_outline_rounded,
            color: colorScheme.onSurfaceVariant,
            size: 20,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              'This analysis is for educational purposes only and should not be considered as personalized investment advice. '
              'Please consult with a financial advisor before making investment decisions.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _analyzeInvestment(BuildContext context, WidgetRef ref, InvestmentFormState formState) async {
    HapticFeedback.mediumImpact();
    
    final request = formState.toRequest();
    await ref.read(rankingStateProvider.notifier).fetchRankings(request);
    
    if (context.mounted) {
      context.goNamed('results', extra: formState.toRequest().toJson());
    }
  }
}