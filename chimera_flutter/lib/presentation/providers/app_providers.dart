import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/services/api_service.dart';
import '../../data/repositories/ranking_repository.dart';
import '../../data/models/ranking_models.dart';

// Service providers
final apiServiceProvider = Provider<ApiService>((ref) {
  return ApiService();
});

final rankingRepositoryProvider = Provider<RankingRepository>((ref) {
  final apiService = ref.watch(apiServiceProvider);
  return RankingRepository(apiService);
});

// State providers
final connectionStatusProvider = StateNotifierProvider<ConnectionStatusNotifier, ConnectionStatus>((ref) {
  final repository = ref.watch(rankingRepositoryProvider);
  return ConnectionStatusNotifier(repository);
});

final rankingStateProvider = StateNotifierProvider<RankingStateNotifier, RankingState>((ref) {
  final repository = ref.watch(rankingRepositoryProvider);
  return RankingStateNotifier(repository);
});

final chatStateProvider = StateNotifierProvider<ChatStateNotifier, ChatState>((ref) {
  final repository = ref.watch(rankingRepositoryProvider);
  return ChatStateNotifier(repository);
});

// Investment form state
final investmentFormProvider = StateNotifierProvider<InvestmentFormNotifier, InvestmentFormState>((ref) {
  return InvestmentFormNotifier();
});

// Connection Status
enum ConnectionStatus { unknown, checking, online, offline }

class ConnectionStatusNotifier extends StateNotifier<ConnectionStatus> {
  final RankingRepository _repository;
  
  ConnectionStatusNotifier(this._repository) : super(ConnectionStatus.unknown) {
    checkConnection();
  }
  
  Future<void> checkConnection() async {
    state = ConnectionStatus.checking;
    final isOnline = await _repository.testConnection();
    state = isOnline ? ConnectionStatus.online : ConnectionStatus.offline;
  }
}

// Ranking State
class RankingState {
  final List<AssetRanking> rankings;
  final RankingMetadata? metadata;
  final bool isLoading;
  final String? error;
  
  const RankingState({
    this.rankings = const [],
    this.metadata,
    this.isLoading = false,
    this.error,
  });
  
  RankingState copyWith({
    List<AssetRanking>? rankings,
    RankingMetadata? metadata,
    bool? isLoading,
    String? error,
  }) {
    return RankingState(
      rankings: rankings ?? this.rankings,
      metadata: metadata ?? this.metadata,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class RankingStateNotifier extends StateNotifier<RankingState> {
  final RankingRepository _repository;
  
  RankingStateNotifier(this._repository) : super(const RankingState());
  
  Future<void> fetchRankings(RankingRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    
    try {
      final response = await _repository.getRankings(request);
      final rankings = response.rankings.map((r) => AssetRanking.fromBackend(r)).toList();
      
      state = state.copyWith(
        rankings: rankings,
        metadata: response.metadata,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString(),
      );
    }
  }
  
  void clearError() {
    state = state.copyWith(error: null);
  }
}

// Chat State
class ChatState {
  final List<ChatMessage> messages;
  final bool isLoading;
  final String? error;
  
  const ChatState({
    this.messages = const [],
    this.isLoading = false,
    this.error,
  });
  
  ChatState copyWith({
    List<ChatMessage>? messages,
    bool? isLoading,
    String? error,
  }) {
    return ChatState(
      messages: messages ?? this.messages,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class ChatStateNotifier extends StateNotifier<ChatState> {
  final RankingRepository _repository;
  
  ChatStateNotifier(this._repository) : super(const ChatState());
  
  Future<void> sendMessage(String message) async {
    // Add user message
    final userMessage = ChatMessage(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      message: message,
      isUser: true,
      timestamp: DateTime.now(),
    );
    
    state = state.copyWith(
      messages: [...state.messages, userMessage],
      isLoading: true,
      error: null,
    );
    
    try {
      final request = ChatRequest(question: message);
      final response = await _repository.sendChatMessage(request);
      
      // Add AI response
      final aiMessage = ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString() + '_ai',
        message: message,
        isUser: false,
        timestamp: DateTime.now(),
        answer: response.answer,
        citations: response.citations,
        confidence: response.confidence,
        disclaimer: response.disclaimer,
      );
      
      state = state.copyWith(
        messages: [...state.messages, aiMessage],
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString(),
      );
    }
  }
  
  void clearMessages() {
    state = const ChatState();
  }
}

// Investment Form State
class InvestmentFormState {
  final double amount;
  final int horizonDays;
  final String riskPreference;
  final String assetType;
  
  const InvestmentFormState({
    this.amount = 100000,
    this.horizonDays = 30,
    this.riskPreference = 'MODERATE',
    this.assetType = 'EQUITY',
  });
  
  InvestmentFormState copyWith({
    double? amount,
    int? horizonDays,
    String? riskPreference,
    String? assetType,
  }) {
    return InvestmentFormState(
      amount: amount ?? this.amount,
      horizonDays: horizonDays ?? this.horizonDays,
      riskPreference: riskPreference ?? this.riskPreference,
      assetType: assetType ?? this.assetType,
    );
  }
  
  RankingRequest toRequest() {
    return RankingRequest(
      amountInr: amount,
      horizonDays: horizonDays,
      riskPreference: riskPreference,
      assetType: assetType,
    );
  }
}

class InvestmentFormNotifier extends StateNotifier<InvestmentFormState> {
  InvestmentFormNotifier() : super(const InvestmentFormState());
  
  void updateAmount(double amount) {
    state = state.copyWith(amount: amount);
  }
  
  void updateHorizon(int days) {
    state = state.copyWith(horizonDays: days);
  }
  
  void updateRiskPreference(String risk) {
    state = state.copyWith(riskPreference: risk);
  }
  
  void updateAssetType(String type) {
    state = state.copyWith(assetType: type);
  }
}