import 'package:equatable/equatable.dart';

class AchievementItem extends Equatable {
  const AchievementItem({
    required this.id,
    required this.title,
    required this.description,
    required this.unlocked,
    this.unlockedAt,
  });

  final String id;
  final String title;
  final String description;
  final bool unlocked;
  final DateTime? unlockedAt;

  factory AchievementItem.fromJson(Map<String, dynamic> json) {
    final unlockedAtRaw = (json['unlockedAt'] ?? json['unlocked_at'])?.toString();
    return AchievementItem(
      id: (json['id'] ?? json['achievementId'] ?? json['achievement_id'] ?? '').toString(),
      title: (json['title'] ?? json['name'] ?? 'Achievement').toString(),
      description: (json['description'] ?? json['subtitle'] ?? '').toString(),
      unlocked: (json['unlocked'] ?? json['isUnlocked'] ?? json['completed'] ?? false) == true,
      unlockedAt: unlockedAtRaw == null ? null : DateTime.tryParse(unlockedAtRaw),
    );
  }

  @override
  List<Object?> get props => [id, title, description, unlocked, unlockedAt];
}

class GamificationSnapshot extends Equatable {
  const GamificationSnapshot({
    required this.xp,
    required this.level,
    required this.streakDays,
    required this.totalActivities,
    required this.nextLevelXp,
    required this.achievements,
  });

  final int xp;
  final int level;
  final int streakDays;
  final int totalActivities;
  final int nextLevelXp;
  final List<AchievementItem> achievements;

  double get levelProgress {
    if (nextLevelXp <= 0) return 0;
    final progress = xp / nextLevelXp;
    if (progress < 0) return 0;
    if (progress > 1) return 1;
    return progress;
  }

  factory GamificationSnapshot.fromJson(Map<String, dynamic> json) {
    final source = _sourceMap(json);

    final achievementsRaw = (source['achievements'] ?? source['user_achievements']) as List? ?? const <dynamic>[];

    return GamificationSnapshot(
      xp: _readInt(source, ['xp', 'totalXp', 'total_xp']),
      level: _readInt(source, ['level', 'currentLevel', 'current_level']),
      streakDays: _readInt(source, ['streakDays', 'streak_days', 'currentStreak', 'current_streak']),
      totalActivities: _readInt(source, ['totalActivities', 'total_activities', 'activityCount', 'activity_count']),
      nextLevelXp: _readInt(source, ['nextLevelXp', 'next_level_xp', 'nextLevelTarget', 'next_level_target']),
      achievements: achievementsRaw
          .whereType<Map>()
          .map((item) => AchievementItem.fromJson(item.cast<String, dynamic>()))
          .toList(),
    );
  }

  static Map<String, dynamic> _sourceMap(Map<String, dynamic> json) {
    final data = json['data'];
    if (data is Map<String, dynamic>) return data;
    if (data is Map) return data.cast<String, dynamic>();

    final stats = json['stats'] ?? json['user_stats'];
    if (stats is Map<String, dynamic>) return stats;
    if (stats is Map) return stats.cast<String, dynamic>();

    return json;
  }

  static int _readInt(Map<String, dynamic> source, List<String> keys) {
    for (final key in keys) {
      final value = source[key];
      if (value is int) return value;
      if (value is num) return value.toInt();
      if (value is String) {
        final parsed = int.tryParse(value);
        if (parsed != null) return parsed;
      }
    }
    return 0;
  }

  @override
  List<Object?> get props => [xp, level, streakDays, totalActivities, nextLevelXp, achievements];
}
