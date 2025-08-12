import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/theme/app_theme.dart';
import 'data/services/notification_service.dart';
import 'presentation/screens/input/input_screen.dart';
import 'presentation/screens/results/results_screen.dart';
import 'presentation/screens/chat/chat_screen.dart';
import 'presentation/widgets/offline_banner.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Firebase (graceful fallback if not configured)
  try {
    await Firebase.initializeApp();
    
    // Initialize notifications
    await NotificationService().initialize();
  } catch (e) {
    // Graceful degradation - continue without Firebase/notifications
    debugPrint('Firebase initialization failed: $e');
  }

  // Set system UI overlay style
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.dark,
      systemNavigationBarColor: Colors.transparent,
      systemNavigationBarIconBrightness: Brightness.dark,
    ),
  );

  runApp(
    const ProviderScope(
      child: ChimeraApp(),
    ),
  );
}

class ChimeraApp extends ConsumerWidget {
  const ChimeraApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'Chimera MVP',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      routerConfig: _router,
    );
  }
}

final _router = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      name: 'input',
      builder: (context, state) => const OfflineBanner(
        child: InputScreen(),
      ),
    ),
    GoRoute(
      path: '/results',
      name: 'results',
      builder: (context, state) {
        final extra = state.extra as Map<String, dynamic>?;
        return OfflineBanner(
          child: ResultsScreen(
            requestData: extra ?? {},
          ),
        );
      },
    ),
    GoRoute(
      path: '/chat',
      name: 'chat',
      builder: (context, state) => const OfflineBanner(
        child: ChatScreen(),
      ),
    ),
  ],
);