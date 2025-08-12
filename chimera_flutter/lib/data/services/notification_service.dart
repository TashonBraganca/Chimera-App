import 'dart:developer';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';

class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  FirebaseMessaging? _messaging;
  bool _isInitialized = false;

  Future<void> initialize() async {
    if (_isInitialized) return;

    try {
      _messaging = FirebaseMessaging.instance;

      // Request permissions (iOS/web)
      final settings = await _messaging!.requestPermission(
        alert: true,
        announcement: false,
        badge: true,
        carPlay: false,
        criticalAlert: false,
        provisional: false,
        sound: true,
      );

      if (settings.authorizationStatus == AuthorizationStatus.authorized) {
        log('User granted permission for notifications');
        
        // Get FCM token
        final token = await _messaging!.getToken();
        if (token != null) {
          log('FCM Token: $token');
          // TODO: Send token to backend for targeted notifications
        }

        // Set up foreground message handler
        FirebaseMessaging.onMessage.listen(_handleForegroundMessage);
        
        // Set up background message handler
        FirebaseMessaging.onBackgroundMessage(_backgroundHandler);
        
        // Handle notification tap when app is in background or terminated
        FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);
        
        _isInitialized = true;
      } else {
        log('User declined or has not accepted permission for notifications');
      }
    } catch (e) {
      log('Error initializing notifications: $e');
      // Graceful degradation - app continues without notifications
    }
  }

  // Handle messages when app is in foreground
  void _handleForegroundMessage(RemoteMessage message) {
    log('Got a message whilst in the foreground!');
    log('Message data: ${message.data}');

    if (message.notification != null) {
      log('Message also contained a notification: ${message.notification}');
      // TODO: Show in-app notification or update UI
    }
  }

  // Handle notification tap
  void _handleNotificationTap(RemoteMessage message) {
    log('User tapped on notification: ${message.data}');
    
    // Navigate based on notification data
    final route = message.data['route'];
    if (route != null) {
      // TODO: Navigate to specific screen using GoRouter
      switch (route) {
        case 'rankings':
          // Navigate to rankings/results screen
          break;
        case 'chat':
          // Navigate to chat screen
          break;
        default:
          // Navigate to home
          break;
      }
    }
  }

  Future<String?> getToken() async {
    if (_messaging == null) return null;
    return await _messaging!.getToken();
  }

  Future<void> subscribeToTopic(String topic) async {
    if (_messaging == null) return;
    await _messaging!.subscribeToTopic(topic);
    log('Subscribed to topic: $topic');
  }

  Future<void> unsubscribeFromTopic(String topic) async {
    if (_messaging == null) return;
    await _messaging!.unsubscribeFromTopic(topic);
    log('Unsubscribed from topic: $topic');
  }

  // Subscribe to relevant topics based on user preferences
  Future<void> setupDefaultSubscriptions() async {
    try {
      // Market updates (can be turned off by user)
      await subscribeToTopic('market_updates');
      
      // App updates and important announcements
      await subscribeToTopic('app_updates');
    } catch (e) {
      log('Error setting up default subscriptions: $e');
    }
  }

  void dispose() {
    _messaging = null;
    _isInitialized = false;
  }
}

// Top-level function for background message handling
@pragma('vm:entry-point')
Future<void> _backgroundHandler(RemoteMessage message) async {
  log('Handling a background message: ${message.messageId}');
  
  // Handle background notifications
  // Note: Keep this lightweight as it runs in isolate
}

// Notification categories for organization
enum NotificationCategory {
  marketUpdate('market_update', 'Market Updates'),
  rankingReady('ranking_ready', 'Rankings Ready'),
  appUpdate('app_update', 'App Updates'),
  educational('educational', 'Educational Content');

  const NotificationCategory(this.id, this.displayName);
  final String id;
  final String displayName;
}

// Notification payload structure
class NotificationPayload {
  final String title;
  final String body;
  final NotificationCategory category;
  final Map<String, dynamic> data;

  const NotificationPayload({
    required this.title,
    required this.body,
    required this.category,
    this.data = const {},
  });

  Map<String, dynamic> toMap() {
    return {
      'title': title,
      'body': body,
      'category': category.id,
      ...data,
    };
  }
}