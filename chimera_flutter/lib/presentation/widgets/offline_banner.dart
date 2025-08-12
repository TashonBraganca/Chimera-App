import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../providers/app_providers.dart';

class OfflineBanner extends ConsumerWidget {
  final Widget child;
  
  const OfflineBanner({
    super.key,
    required this.child,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return StreamBuilder<List<ConnectivityResult>>(
      stream: Connectivity().onConnectivityChanged,
      builder: (context, snapshot) {
        final isOnline = snapshot.data?.any((result) => result != ConnectivityResult.none) ?? true;
        
        return Column(
          children: [
            if (!isOnline) _buildOfflineBanner(context, ref),
            Expanded(child: child),
          ],
        );
      },
    );
  }

  Widget _buildOfflineBanner(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: colorScheme.errorContainer,
        border: Border(
          bottom: BorderSide(
            color: colorScheme.error.withOpacity(0.3),
            width: 1,
          ),
        ),
      ),
      child: Semantics(
        label: 'Offline mode banner',
        child: Row(
          children: [
            Icon(
              Icons.cloud_off_rounded,
              color: colorScheme.onErrorContainer,
              size: 20,
              semanticLabel: 'Offline icon',
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Offline Mode',
                    style: theme.textTheme.titleSmall?.copyWith(
                      color: colorScheme.onErrorContainer,
                      fontWeight: FontWeight.w600,
                    ),
                    semanticsLabel: 'You are currently offline',
                  ),
                  Text(
                    'Using cached data when available',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onErrorContainer.withOpacity(0.8),
                    ),
                  ),
                ],
              ),
            ),
            _buildCacheStatusButton(context, ref),
          ],
        ),
      ),
    );
  }

  Widget _buildCacheStatusButton(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    
    return Semantics(
      label: 'Show cache status',
      button: true,
      child: InkWell(
        onTap: () => _showCacheStatusDialog(context, ref),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(8),
          child: Icon(
            Icons.info_outline_rounded,
            color: colorScheme.onErrorContainer,
            size: 20,
          ),
        ),
      ),
    );
  }

  Future<void> _showCacheStatusDialog(BuildContext context, WidgetRef ref) async {
    final repository = ref.read(rankingRepositoryProvider);
    final hasCachedData = await repository.hasCachedData();
    final cacheTimestamp = await repository.getCacheTimestamp();
    
    if (!context.mounted) return;
    
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(
              Icons.storage_rounded,
              color: colorScheme.primary,
            ),
            const SizedBox(width: 12),
            const Text('Cache Status'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildCacheInfoRow(
              'Cached Data Available',
              hasCachedData ? 'Yes' : 'No',
              hasCachedData ? Icons.check_circle : Icons.cancel,
              hasCachedData ? Colors.green : Colors.red,
            ),
            if (cacheTimestamp != null) ...[
              const SizedBox(height: 8),
              _buildCacheInfoRow(
                'Last Updated',
                _formatCacheTime(cacheTimestamp),
                Icons.access_time,
                colorScheme.primary,
              ),
            ],
            const SizedBox(height: 16),
            Text(
              'Cached data is used when you\'re offline or when the server is unavailable. '
              'Data is automatically refreshed when you\'re online.',
              style: theme.textTheme.bodySmall?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
        actions: [
          if (hasCachedData)
            TextButton(
              onPressed: () async {
                await repository.clearCache();
                if (context.mounted) {
                  Navigator.of(context).pop();
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Cache cleared')),
                  );
                }
              },
              child: const Text('Clear Cache'),
            ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  Widget _buildCacheInfoRow(String label, String value, IconData icon, Color iconColor) {
    return Row(
      children: [
        Icon(icon, size: 16, color: iconColor),
        const SizedBox(width: 8),
        Text('$label: '),
        Expanded(
          child: Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.w600),
            textAlign: TextAlign.end,
          ),
        ),
      ],
    );
  }

  String _formatCacheTime(DateTime cacheTime) {
    final now = DateTime.now();
    final diff = now.difference(cacheTime);
    
    if (diff.inMinutes < 60) {
      return '${diff.inMinutes} minutes ago';
    } else if (diff.inHours < 24) {
      return '${diff.inHours} hours ago';
    } else {
      return '${diff.inDays} days ago';
    }
  }
}