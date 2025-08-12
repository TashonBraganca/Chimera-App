import 'package:dio/dio.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import '../models/ranking_models.dart';

class ApiService {
  static const String _baseUrl = 'http://192.168.1.106:8080';

  late final Dio _dio;

  ApiService() {
    _dio = Dio(BaseOptions(
      baseUrl: _baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      sendTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    // Add logging in debug mode
    _dio.interceptors.add(
      PrettyDioLogger(
        requestHeader: true,
        requestBody: true,
        responseBody: true,
        responseHeader: false,
        error: true,
        compact: true,
      ),
    );

    // Add retry interceptor
    _dio.interceptors.add(
      InterceptorsWrapper(
        onError: (error, handler) async {
          if (error.type == DioExceptionType.connectionTimeout ||
              error.type == DioExceptionType.receiveTimeout ||
              error.type == DioExceptionType.sendTimeout) {
            // Retry once for timeout errors
            try {
              final response = await _dio.request(
                error.requestOptions.path,
                data: error.requestOptions.data,
                queryParameters: error.requestOptions.queryParameters,
                options: Options(
                  method: error.requestOptions.method,
                  headers: error.requestOptions.headers,
                ),
              );
              handler.resolve(response);
              return;
            } catch (e) {
              // If retry fails, continue with original error
            }
          }
          handler.next(error);
        },
      ),
    );
  }

  // Health check
  Future<Map<String, dynamic>> checkHealth() async {
    try {
      final response = await _dio.get('/health');
      return response.data as Map<String, dynamic>;
    } catch (e) {
      throw ApiException('Health check failed: ${e.toString()}');
    }
  }

  // Get asset rankings
  Future<RankingResponse> getRankings(RankingRequest request) async {
    try {
      final response = await _dio.post(
        '/api/rank',
        data: request.toJson(),
      );
      
      return RankingResponse.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      if (e is DioException) {
        if (e.response?.statusCode == 404) {
          throw ApiException('Rankings endpoint not found');
        } else if (e.response?.statusCode == 500) {
          throw ApiException('Server error while fetching rankings');
        } else if (e.type == DioExceptionType.connectionError) {
          throw ApiException('Connection failed. Please check your internet connection.');
        } else if (e.type == DioExceptionType.connectionTimeout) {
          throw ApiException('Connection timeout. Please try again.');
        }
      }
      throw ApiException('Failed to get rankings: ${e.toString()}');
    }
  }

  // Chat with AI
  Future<ChatResponse> sendChatMessage(ChatRequest request) async {
    try {
      final response = await _dio.post(
        '/api/chat',
        data: request.toJson(),
      );
      
      return ChatResponse.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      if (e is DioException) {
        if (e.response?.statusCode == 404) {
          throw ApiException('Chat endpoint not found');
        } else if (e.response?.statusCode == 500) {
          throw ApiException('Server error during chat');
        } else if (e.type == DioExceptionType.connectionError) {
          throw ApiException('Connection failed. Please check your internet connection.');
        } else if (e.type == DioExceptionType.connectionTimeout) {
          throw ApiException('Connection timeout. Please try again.');
        }
      }
      throw ApiException('Failed to send chat message: ${e.toString()}');
    }
  }

  // Test connection with mock data fallback
  Future<bool> testConnection() async {
    try {
      final health = await checkHealth();
      return health['status'] == 'UP';
    } catch (e) {
      return false;
    }
  }

  void dispose() {
    _dio.close();
  }
}

class ApiException implements Exception {
  final String message;
  const ApiException(this.message);

  @override
  String toString() => 'ApiException: $message';
}