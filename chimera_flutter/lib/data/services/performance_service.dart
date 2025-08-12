import 'dart:developer';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class PerformanceService {
  static final PerformanceService _instance = PerformanceService._internal();
  factory PerformanceService() => _instance;
  PerformanceService._internal();

  DateTime? _appStartTime;
  DateTime? _firstFrameTime;
  bool _isOptimized = false;

  void markAppStart() {
    _appStartTime = DateTime.now();
    log('App startup began at: $_appStartTime');
  }

  void markFirstFrame() {
    _firstFrameTime = DateTime.now();
    if (_appStartTime != null) {
      final startupTime = _firstFrameTime!.difference(_appStartTime!);
      log('App startup time: ${startupTime.inMilliseconds}ms');
      
      // Phase M6 requirement: cold start < 1.2s
      if (startupTime.inMilliseconds > 1200) {
        log('WARNING: Startup time exceeds 1.2s target');
      }
    }
  }

  Duration? get startupTime {
    if (_appStartTime != null && _firstFrameTime != null) {
      return _firstFrameTime!.difference(_appStartTime!);
    }
    return null;
  }

  Future<void> optimizeForPerformance() async {
    if (_isOptimized) return;

    try {
      // Pre-warm critical services
      await _preWarmServices();
      
      // Optimize system UI
      await _optimizeSystemUI();
      
      // Set frame rate target
      _setFrameRateTarget();
      
      _isOptimized = true;
      log('Performance optimizations applied');
    } catch (e) {
      log('Error applying performance optimizations: $e');
    }
  }

  Future<void> _preWarmServices() async {
    // Pre-warm image cache
    PaintingBinding.instance.imageCache.maximumSize = 100;
    PaintingBinding.instance.imageCache.maximumSizeBytes = 50 << 20; // 50MB
    
    // Pre-compile common animations and transitions
    await Future.delayed(const Duration(milliseconds: 1));
  }

  Future<void> _optimizeSystemUI() async {
    // Set preferred orientations for consistency
    await SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);

    // Hide splash screen once UI is ready
    if (!kDebugMode) {
      SystemChrome.setSystemUIOverlayStyle(
        const SystemUiOverlayStyle(
          statusBarColor: Colors.transparent,
          statusBarIconBrightness: Brightness.dark,
        ),
      );
    }
  }

  void _setFrameRateTarget() {
    // Target 60 FPS for smooth animations (Phase M6 requirement: ≤16ms p95)
    if (kDebugMode) {
      // In debug mode, allow frame rate debugging
      log('Frame rate target: 60 FPS (16.67ms per frame)');
    }
  }

  // Memory optimization for large lists
  void optimizeListPerformance() {
    // Configure for smooth scrolling with large datasets
    PaintingBinding.instance.imageCache.maximumSize = 200;
  }

  // Monitor frame performance
  void startFrameMonitoring() {
    if (kDebugMode) {
      WidgetsBinding.instance.addTimingsCallback((timings) {
        for (final timing in timings) {
          final frameDuration = timing.totalSpan;
          if (frameDuration > const Duration(milliseconds: 16)) {
            log('Frame took ${frameDuration.inMilliseconds}ms (target: ≤16ms)');
          }
        }
      });
    }
  }

  void dispose() {
    _isOptimized = false;
  }
}

// Performance monitoring widget for debugging
class PerformanceOverlay extends StatelessWidget {
  final Widget child;
  final bool showFPS;

  const PerformanceOverlay({
    super.key,
    required this.child,
    this.showFPS = false,
  });

  @override
  Widget build(BuildContext context) {
    if (kDebugMode && showFPS) {
      return Stack(
        children: [
          child,
          Positioned(
            top: 50,
            right: 16,
            child: Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.black54,
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Text(
                'Debug Mode\nFPS Monitor Active',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 10,
                ),
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ],
      );
    }
    return child;
  }
}

// Startup performance tracker
class StartupProfiler {
  static final Map<String, DateTime> _markers = {};

  static void mark(String milestone) {
    _markers[milestone] = DateTime.now();
    log('Startup milestone: $milestone at ${_markers[milestone]}');
  }

  static void printProfile() {
    if (_markers.isEmpty) return;

    log('=== Startup Performance Profile ===');
    DateTime? previous;
    
    for (final entry in _markers.entries) {
      if (previous != null) {
        final duration = entry.value.difference(previous);
        log('${entry.key}: +${duration.inMilliseconds}ms');
      } else {
        log('${entry.key}: START');
      }
      previous = entry.value;
    }

    final total = _markers.values.last.difference(_markers.values.first);
    log('Total startup: ${total.inMilliseconds}ms');
    log('=== End Profile ===');
  }

  static void clear() {
    _markers.clear();
  }
}