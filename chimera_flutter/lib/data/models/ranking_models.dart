import 'package:freezed_annotation/freezed_annotation.dart';

part 'ranking_models.freezed.dart';
part 'ranking_models.g.dart';

@freezed
class RankingRequest with _$RankingRequest {
  const factory RankingRequest({
    @Default(100000) double amountInr,
    @Default(30) int horizonDays,
    @Default('CONSERVATIVE') String riskPreference,
    @Default('EQUITY') String assetType,
  }) = _RankingRequest;

  factory RankingRequest.fromJson(Map<String, dynamic> json) =>
      _$RankingRequestFromJson(json);
}

@freezed
class RankingResponse with _$RankingResponse {
  const factory RankingResponse({
    required String status,
    required List<BackendAssetRanking> rankings,
    required RankingMetadata metadata,
  }) = _RankingResponse;

  factory RankingResponse.fromJson(Map<String, dynamic> json) =>
      _$RankingResponseFromJson(json);
}

@freezed
class BackendAssetRanking with _$BackendAssetRanking {
  const factory BackendAssetRanking({
    required String symbol,
    required String name,
    required double score,
    required int confidence,
    required int rank,
    required String recommendation,
    required double lastPrice,
    required String change,
  }) = _BackendAssetRanking;

  factory BackendAssetRanking.fromJson(Map<String, dynamic> json) =>
      _$BackendAssetRankingFromJson(json);
}

@freezed
class RankingMetadata with _$RankingMetadata {
  const factory RankingMetadata({
    required int totalAssets,
    required int displayedAssets,
    required String lastUpdated,
    required String dataSource,
    required String disclaimer,
  }) = _RankingMetadata;

  factory RankingMetadata.fromJson(Map<String, dynamic> json) =>
      _$RankingMetadataFromJson(json);
}

// Chat models
@freezed
class ChatRequest with _$ChatRequest {
  const factory ChatRequest({
    required String question,
    String? assetId,
    String? category,
  }) = _ChatRequest;

  factory ChatRequest.fromJson(Map<String, dynamic> json) =>
      _$ChatRequestFromJson(json);
}

@freezed
class ChatResponse with _$ChatResponse {
  const factory ChatResponse({
    required String status,
    required String answer,
    required List<BackendCitation> citations,
    required int confidence,
    required String lastUpdated,
    required String disclaimer,
  }) = _ChatResponse;

  factory ChatResponse.fromJson(Map<String, dynamic> json) =>
      _$ChatResponseFromJson(json);
}

@freezed
class BackendCitation with _$BackendCitation {
  const factory BackendCitation({
    required String source,
    required String date,
    required String title,
  }) = _BackendCitation;

  factory BackendCitation.fromJson(Map<String, dynamic> json) =>
      _$BackendCitationFromJson(json);
}

// UI Models for internal use
@freezed
class AssetRanking with _$AssetRanking {
  const factory AssetRanking({
    required String symbol,
    required String name,
    required double score,
    required int confidence,
    required int rank,
    required String recommendation,
    required double lastPrice,
    required String change,
    required DateTime lastUpdated,
  }) = _AssetRanking;

  factory AssetRanking.fromBackend(BackendAssetRanking backend) {
    return AssetRanking(
      symbol: backend.symbol,
      name: backend.name,
      score: backend.score,
      confidence: backend.confidence,
      rank: backend.rank,
      recommendation: backend.recommendation,
      lastPrice: backend.lastPrice,
      change: backend.change,
      lastUpdated: DateTime.now(),
    );
  }
}

@freezed
class ChatMessage with _$ChatMessage {
  const factory ChatMessage({
    required String id,
    required String message,
    required bool isUser,
    required DateTime timestamp,
    String? answer,
    List<BackendCitation>? citations,
    int? confidence,
    String? disclaimer,
  }) = _ChatMessage;
}