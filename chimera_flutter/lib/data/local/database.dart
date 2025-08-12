import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

import '../models/ranking_models.dart';

part 'database.g.dart';

// Tables
class CachedRankings extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get symbol => text().withLength(min: 1, max: 20)();
  TextColumn get name => text().withLength(min: 1, max: 100)();
  RealColumn get score => real()();
  IntColumn get confidence => integer()();
  IntColumn get rank => integer()();
  TextColumn get recommendation => text().withLength(min: 1, max: 50)();
  RealColumn get lastPrice => real()();
  TextColumn get change => text().withLength(min: 1, max: 20)();
  DateTimeColumn get cachedAt => dateTime()();
  DateTimeColumn get lastUpdated => dateTime()();
  
  // Request context for cache validation
  RealColumn get requestAmount => real()();
  IntColumn get requestHorizonDays => integer()();
  TextColumn get requestRiskPreference => text().withLength(min: 1, max: 20)();
  TextColumn get requestAssetType => text().withLength(min: 1, max: 20)();
}

class CachedChats extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get messageId => text().withLength(min: 1, max: 50)();
  TextColumn get question => text()();
  TextColumn get answer => text()();
  TextColumn get citations => text()(); // JSON serialized citations
  IntColumn get confidence => integer()();
  TextColumn get disclaimer => text()();
  DateTimeColumn get cachedAt => dateTime()();
  DateTimeColumn get lastUpdated => dateTime()();
}

class CacheMetadata extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get cacheKey => text().withLength(min: 1, max: 100)();
  TextColumn get cacheType => text().withLength(min: 1, max: 50)(); // 'rankings' or 'chat'
  DateTimeColumn get lastRefresh => dateTime()();
  DateTimeColumn get expiresAt => dateTime()();
  TextColumn get metadata => text()(); // JSON for additional metadata
}

@DriftDatabase(tables: [CachedRankings, CachedChats, CacheMetadata])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;

  // Rankings cache operations
  Future<List<CachedRanking>> getCachedRankings(RankingRequest request) async {
    final query = select(cachedRankings)
      ..where((r) => 
        r.requestAmount.equals(request.amountInr) &
        r.requestHorizonDays.equals(request.horizonDays) &
        r.requestRiskPreference.equals(request.riskPreference) &
        r.requestAssetType.equals(request.assetType)
      )
      ..orderBy([(r) => OrderingTerm.asc(r.rank)]);
    
    return await query.get();
  }

  Future<bool> isRankingCacheValid(RankingRequest request) async {
    final cached = await getCachedRankings(request);
    if (cached.isEmpty) return false;

    const cacheValidityDuration = Duration(minutes: 30);
    final now = DateTime.now();
    final oldestCache = cached.map((r) => r.cachedAt).reduce((a, b) => a.isBefore(b) ? a : b);
    
    return now.difference(oldestCache) < cacheValidityDuration;
  }

  Future<void> cacheRankings(RankingResponse response, RankingRequest request) async {
    await transaction(() async {
      // Clear old cache for this request
      await (delete(cachedRankings)
        ..where((r) => 
          r.requestAmount.equals(request.amountInr) &
          r.requestHorizonDays.equals(request.horizonDays) &
          r.requestRiskPreference.equals(request.riskPreference) &
          r.requestAssetType.equals(request.assetType)
        )
      ).go();

      // Insert new cache
      final now = DateTime.now();
      for (final ranking in response.rankings) {
        await into(cachedRankings).insert(CachedRankingsCompanion(
          symbol: Value(ranking.symbol),
          name: Value(ranking.name),
          score: Value(ranking.score),
          confidence: Value(ranking.confidence),
          rank: Value(ranking.rank),
          recommendation: Value(ranking.recommendation),
          lastPrice: Value(ranking.lastPrice),
          change: Value(ranking.change),
          cachedAt: Value(now),
          lastUpdated: Value(DateTime.parse(response.metadata.lastUpdated)),
          requestAmount: Value(request.amountInr),
          requestHorizonDays: Value(request.horizonDays),
          requestRiskPreference: Value(request.riskPreference),
          requestAssetType: Value(request.assetType),
        ));
      }

      // Update metadata
      await into(cacheMetadata).insertOnConflictUpdate(CacheMetadataCompanion(
        cacheKey: Value('rankings_${request.amountInr}_${request.horizonDays}_${request.riskPreference}_${request.assetType}'),
        cacheType: const Value('rankings'),
        lastRefresh: Value(now),
        expiresAt: Value(now.add(const Duration(minutes: 30))),
        metadata: Value('{"totalAssets": ${response.metadata.totalAssets}, "disclaimer": "${response.metadata.disclaimer}"}'),
      ));
    });
  }

  // Chat cache operations
  Future<CachedChat?> getCachedChat(String question) async {
    final query = select(cachedChats)
      ..where((c) => c.question.equals(question.trim().toLowerCase()))
      ..limit(1);
    
    final results = await query.get();
    return results.isNotEmpty ? results.first : null;
  }

  Future<List<CachedChat>> getRecentChats({int limit = 50}) async {
    final query = select(cachedChats)
      ..orderBy([(c) => OrderingTerm.desc(c.cachedAt)])
      ..limit(limit);
    
    return await query.get();
  }

  Future<void> cacheChat(ChatResponse response, String question) async {
    final now = DateTime.now();
    await into(cachedChats).insertOnConflictUpdate(CachedChatsCompanion(
      messageId: Value(DateTime.now().millisecondsSinceEpoch.toString()),
      question: Value(question.trim().toLowerCase()),
      answer: Value(response.answer),
      citations: Value(response.citations.map((c) => c.toJson()).toString()),
      confidence: Value(response.confidence),
      disclaimer: Value(response.disclaimer),
      cachedAt: Value(now),
      lastUpdated: Value(DateTime.parse(response.lastUpdated)),
    ));
  }

  // Cache management
  Future<void> clearOldCache() async {
    final cutoff = DateTime.now().subtract(const Duration(days: 7));
    
    await transaction(() async {
      // Clear old rankings
      await (delete(cachedRankings)..where((r) => r.cachedAt.isSmallerThanValue(cutoff))).go();
      
      // Clear old chats
      await (delete(cachedChats)..where((c) => c.cachedAt.isSmallerThanValue(cutoff))).go();
      
      // Clear expired metadata
      final now = DateTime.now();
      await (delete(cacheMetadata)..where((m) => m.expiresAt.isSmallerThanValue(now))).go();
    });
  }

  Future<void> clearAllCache() async {
    await transaction(() async {
      await delete(cachedRankings).go();
      await delete(cachedChats).go();
      await delete(cacheMetadata).go();
    });
  }

  Future<CacheStats> getCacheStats() async {
    final rankingsCount = await (select(cachedRankings)).get().then((rows) => rows.length);
    final chatsCount = await (select(cachedChats)).get().then((rows) => rows.length);
    
    final oldestRanking = await (select(cachedRankings)
      ..orderBy([(r) => OrderingTerm.asc(r.cachedAt)])
      ..limit(1)
    ).getSingleOrNull();
    
    return CacheStats(
      rankingsCount: rankingsCount,
      chatsCount: chatsCount,
      oldestCacheDate: oldestRanking?.cachedAt,
      totalSizeEstimate: (rankingsCount * 200) + (chatsCount * 500), // rough bytes estimate
    );
  }
}

class CacheStats {
  final int rankingsCount;
  final int chatsCount;
  final DateTime? oldestCacheDate;
  final int totalSizeEstimate;

  CacheStats({
    required this.rankingsCount,
    required this.chatsCount,
    this.oldestCacheDate,
    required this.totalSizeEstimate,
  });
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'chimera_cache.db'));

    return NativeDatabase.createInBackground(file);
  });
}