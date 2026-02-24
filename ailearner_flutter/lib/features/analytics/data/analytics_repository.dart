import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';
import '../../courses/data/course_repository.dart';

class AnalyticsSummary {
  const AnalyticsSummary({
    required this.totalSubtopics,
    required this.completedSubtopics,
    required this.units,
    required this.unitCompletion,
  });

  final int totalSubtopics;
  final int completedSubtopics;
  final int units;
  final List<double> unitCompletion;

  double get completionPercent => totalSubtopics == 0 ? 0 : (completedSubtopics / totalSubtopics) * 100;
}

class AnalyticsRepository {
  AnalyticsRepository(this._apiClient, this._courseRepository);

  final ApiClient _apiClient;
  final CourseRepository _courseRepository;

  Future<AnalyticsSummary> loadCourseSummary(String courseId) async {
    try {
      final response = await _apiClient.dio.get<Map<String, dynamic>>(
        ApiEndpoints.analyticsCourse(courseId),
      );
      final data = response.data ?? const <String, dynamic>{};

      final totalSubtopics = _readInt(data, ['totalSubtopics', 'total_subtopics', 'total']);
      final completedSubtopics = _readInt(data, ['completedSubtopics', 'completed_subtopics', 'completed']);

      final unitCompletionRaw = _readList(data, ['unitCompletion', 'unit_completion', 'units']);
      final unitCompletion = unitCompletionRaw
          .map((value) => _toDouble(value))
          .whereType<double>()
          .map((value) => (value > 1 ? value / 100 : value).clamp(0.0, 1.0).toDouble())
          .toList();

      final units = _readInt(data, ['units', 'unitCount', 'unit_count']);

      return AnalyticsSummary(
        totalSubtopics: totalSubtopics,
        completedSubtopics: completedSubtopics,
        units: units > 0 ? units : unitCompletion.length,
        unitCompletion: unitCompletion,
      );
    } catch (_) {
      final full = await _courseRepository.getFullCourse(courseId);
      final progressIds = await _courseRepository.getCourseProgress(courseId);
      final done = progressIds.toSet();

      final units = (full['units'] as List?) ?? <dynamic>[];
      var totalSubtopics = 0;
      var completedSubtopics = 0;
      final perUnit = <double>[];

      for (final unitRaw in units.whereType<Map>()) {
        final unit = unitRaw.cast<String, dynamic>();
        final subtopics = (unit['subtopics'] as List?) ?? <dynamic>[];
        final subtopicIds = subtopics.whereType<Map>().map((s) => (s['id'] ?? '').toString()).where((id) => id.isNotEmpty).toList();
        final unitTotal = subtopicIds.length;
        final unitDone = subtopicIds.where(done.contains).length;

        totalSubtopics += unitTotal;
        completedSubtopics += unitDone;
        perUnit.add(unitTotal == 0 ? 0 : unitDone / unitTotal);
      }

      return AnalyticsSummary(
        totalSubtopics: totalSubtopics,
        completedSubtopics: completedSubtopics,
        units: units.length,
        unitCompletion: perUnit,
      );
    }
  }

  static int _readInt(Map<String, dynamic> data, List<String> keys) {
    for (final key in keys) {
      final value = data[key];
      if (value is int) return value;
      if (value is num) return value.toInt();
      if (value is String) {
        final parsed = int.tryParse(value);
        if (parsed != null) return parsed;
      }
    }
    return 0;
  }

  static List<dynamic> _readList(Map<String, dynamic> data, List<String> keys) {
    for (final key in keys) {
      final value = data[key];
      if (value is List) return value;
    }
    return const <dynamic>[];
  }

  static double? _toDouble(dynamic value) {
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value);
    if (value is Map) {
      final percent = value['percent'] ?? value['completion'] ?? value['value'];
      if (percent is num) return percent.toDouble();
      if (percent is String) return double.tryParse(percent);
    }
    return null;
  }
}
