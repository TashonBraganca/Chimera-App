import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../providers/app_providers.dart';

class ConnectionStatusIndicator extends StatelessWidget {
  final ConnectionStatus status;

  const ConnectionStatusIndicator({
    super.key,
    required this.status,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      margin: const EdgeInsets.only(right: 16),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildStatusIcon(colorScheme),
          const SizedBox(width: 8),
          Text(
            _getStatusText(),
            style: TextStyle(
              color: colorScheme.onPrimary,
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusIcon(ColorScheme colorScheme) {
    switch (status) {
      case ConnectionStatus.online:
        return Icon(
          Icons.wifi_rounded,
          color: Colors.green[300],
          size: 16,
        ).animate(onPlay: (controller) => controller.repeat())
            .shimmer(duration: 2000.ms, color: Colors.white.withOpacity(0.3));
      
      case ConnectionStatus.offline:
        return Icon(
          Icons.wifi_off_rounded,
          color: Colors.orange[300],
          size: 16,
        );
      
      case ConnectionStatus.checking:
        return SizedBox(
          width: 16,
          height: 16,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            valueColor: AlwaysStoppedAnimation(colorScheme.onPrimary),
          ),
        );
      
      case ConnectionStatus.unknown:
      default:
        return Icon(
          Icons.help_outline_rounded,
          color: colorScheme.onPrimary.withOpacity(0.7),
          size: 16,
        );
    }
  }

  String _getStatusText() {
    switch (status) {
      case ConnectionStatus.online:
        return 'Online';
      case ConnectionStatus.offline:
        return 'Offline';
      case ConnectionStatus.checking:
        return 'Checking...';
      case ConnectionStatus.unknown:
      default:
        return 'Unknown';
    }
  }
}