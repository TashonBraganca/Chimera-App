import 'dart:convert';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/ranking_models.dart';
import '../services/api_service.dart';

class RankingRepository {
  final ApiService _apiService;
  static const String _cachedRankingsKey = 'cached_rankings';
  static const String _cachedRankingsTimestampKey = 'cached_rankings_timestamp';
  static const String _cachedChatKey = 'cached_chat_messages';

  const RankingRepository(this._apiService);

  Future<RankingResponse> getRankings(RankingRequest request) async {
    // Check connectivity first
    final connectivityResult = await Connectivity().checkConnectivity();
    final hasConnection = connectivityResult.any((result) => result != ConnectivityResult.none);
    
    try {
      if (hasConnection) {
        // Try to fetch from API first
        final response = await _apiService.getRankings(request);
        
        // Cache the successful response
        await _cacheRankings(response);
        
        return response;
      } else {
        // Offline: try to return cached data
        final cachedResponse = await _getCachedRankings();
        if (cachedResponse != null) {
          return cachedResponse.copyWith(
            metadata: cachedResponse.metadata.copyWith(
              dataSource: 'Cached Data (Offline)',
              disclaimer: '⚠️ Using cached data from ${_getCacheAge()}. No internet connection available.',
            ),
          );
        }
        
        // If no cache, return mock data
        return _getMockRankingResponse();
      }
    } catch (e) {
      // If API fails even with connection, try to return cached data
      final cachedResponse = await _getCachedRankings();
      if (cachedResponse != null) {
        return cachedResponse.copyWith(
          metadata: cachedResponse.metadata.copyWith(
            dataSource: 'Cached Data (Error Fallback)',
            disclaimer: '⚠️ Using cached data from ${_getCacheAge()}. Server temporarily unavailable.',
          ),
        );
      }
      
      // If no cache, return mock data
      return _getMockRankingResponse();
    }
  }

  Future<ChatResponse> sendChatMessage(ChatRequest request) async {
    try {
      final response = await _apiService.sendChatMessage(request);
      
      // Cache successful chat responses
      await _cacheChatMessage(request, response);
      
      return response;
    } catch (e) {
      // Return mock chat response if API fails
      return _getMockChatResponse(request.question);
    }
  }

  Future<bool> testConnection() async {
    return await _apiService.testConnection();
  }

  // Caching methods
  Future<void> _cacheRankings(RankingResponse response) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final jsonString = jsonEncode(response.toJson());
      await prefs.setString(_cachedRankingsKey, jsonString);
      await prefs.setInt(_cachedRankingsTimestampKey, DateTime.now().millisecondsSinceEpoch);
    } catch (e) {
      // Ignore cache errors
    }
  }

  Future<RankingResponse?> _getCachedRankings() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final jsonString = prefs.getString(_cachedRankingsKey);
      final timestamp = prefs.getInt(_cachedRankingsTimestampKey);
      
      if (jsonString != null && timestamp != null) {
        // Check if cache is not too old (24 hours)
        final cacheAge = DateTime.now().millisecondsSinceEpoch - timestamp;
        if (cacheAge < 24 * 60 * 60 * 1000) {
          final json = jsonDecode(jsonString) as Map<String, dynamic>;
          return RankingResponse.fromJson(json);
        }
      }
    } catch (e) {
      // Ignore cache errors
    }
    return null;
  }

  Future<void> _cacheChatMessage(ChatRequest request, ChatResponse response) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final existingMessages = prefs.getStringList(_cachedChatKey) ?? [];
      
      final messageData = {
        'request': request.toJson(),
        'response': response.toJson(),
        'timestamp': DateTime.now().millisecondsSinceEpoch,
      };
      
      existingMessages.add(jsonEncode(messageData));
      
      // Keep only last 50 messages
      if (existingMessages.length > 50) {
        existingMessages.removeAt(0);
      }
      
      await prefs.setStringList(_cachedChatKey, existingMessages);
    } catch (e) {
      // Ignore cache errors
    }
  }

  // Mock data for offline mode
  RankingResponse _getMockRankingResponse() {
    return const RankingResponse(
      status: 'success',
      rankings: [
        BackendAssetRanking(
          symbol: 'RELIANCE',
          name: 'Reliance Industries Ltd.',
          score: 0.87,
          confidence: 92,
          rank: 1,
          recommendation: 'BUY',
          lastPrice: 2850.50,
          change: '+2.3%',
        ),
        BackendAssetRanking(
          symbol: 'TCS',
          name: 'Tata Consultancy Services Ltd.',
          score: 0.84,
          confidence: 89,
          rank: 2,
          recommendation: 'BUY',
          lastPrice: 4125.75,
          change: '+1.8%',
        ),
        BackendAssetRanking(
          symbol: 'INFY',
          name: 'Infosys Ltd.',
          score: 0.81,
          confidence: 85,
          rank: 3,
          recommendation: 'BUY',
          lastPrice: 1875.25,
          change: '+1.2%',
        ),
        BackendAssetRanking(
          symbol: 'HDFC',
          name: 'Housing Development Finance Corporation Ltd.',
          score: 0.78,
          confidence: 82,
          rank: 4,
          recommendation: 'HOLD',
          lastPrice: 2650.00,
          change: '+0.9%',
        ),
        BackendAssetRanking(
          symbol: 'ICICI',
          name: 'ICICI Bank Ltd.',
          score: 0.75,
          confidence: 79,
          rank: 5,
          recommendation: 'HOLD',
          lastPrice: 1235.60,
          change: '+0.5%',
        ),
      ],
      metadata: RankingMetadata(
        totalAssets: 50,
        displayedAssets: 5,
        lastUpdated: '2024-01-10 15:30:00 IST',
        dataSource: 'Mock Data (Offline)',
        disclaimer: 'This is mock data for offline use. Educational purposes only.',
      ),
    );
  }

  ChatResponse _getMockChatResponse(String question) {
    String answer;
    if (question.toLowerCase().contains('reliance')) {
      answer = 'Reliance Industries is ranked #1 with 87% confidence due to strong fundamentals: '
          'robust revenue growth of 15% YoY, expanding digital business (Jio), and consistent dividend payments. '
          'The stock shows low volatility and high liquidity, making it suitable for medium-term investments.';
    } else if (question.toLowerCase().contains('tcs')) {
      answer = 'TCS ranks #2 with 89% confidence based on excellent operational metrics: '
          'industry-leading margins (25%+), strong dollar revenue growth, and digital transformation leadership. '
          'Low client concentration risk and consistent free cash flow generation support the high ranking.';
    } else {
      answer = 'The ranking is based on our proprietary scoring model that considers: '
          'multi-horizon returns (1D, 1W, 1M, 3M), EWMA volatility, maximum drawdown, liquidity indicators, '
          'momentum factors (12-1 month), and sentiment analysis from news sources. '
          'All factors are cross-sectionally normalized and weighted by investment horizon.';
    }

    return ChatResponse(
      status: 'success',
      answer: answer,
      citations: const [
        BackendCitation(
          source: 'NSE Bhavcopy',
          date: '2024-01-10',
          title: 'End of Day Prices',
        ),
        BackendCitation(
          source: 'AMFI NAV',
          date: '2024-01-10',
          title: 'Mutual Fund Net Asset Values',
        ),
        BackendCitation(
          source: 'Mock Analysis',
          date: '2024-01-10',
          title: 'Offline Analysis (Mock Data)',
        ),
      ],
      confidence: 85,
      lastUpdated: '2024-01-10 15:30:00 IST',
      disclaimer: 'This is mock analysis for offline use. Educational purposes only.',
    );
  }

  Future<void> clearCache() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_cachedRankingsKey);
      await prefs.remove(_cachedRankingsTimestampKey);
      await prefs.remove(_cachedChatKey);
    } catch (e) {
      // Ignore cache errors
    }
  }

  String _getCacheAge() {
    try {
      final prefs = SharedPreferences.getInstance();
      prefs.then((prefs) {
        final timestamp = prefs.getInt(_cachedRankingsTimestampKey);
        if (timestamp != null) {
          final cacheTime = DateTime.fromMillisecondsSinceEpoch(timestamp);
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
      });
    } catch (e) {
      // Ignore errors
    }
    return 'unknown time';
  }

  Future<bool> hasCachedData() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      return prefs.containsKey(_cachedRankingsKey);
    } catch (e) {
      return false;
    }
  }

  Future<DateTime?> getCacheTimestamp() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final timestamp = prefs.getInt(_cachedRankingsTimestampKey);
      if (timestamp != null) {
        return DateTime.fromMillisecondsSinceEpoch(timestamp);
      }
    } catch (e) {
      // Ignore errors
    }
    return null;
  }
}