import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';

class ExplainerVideoStatus {
  const ExplainerVideoStatus({
    required this.status,
    this.progress,
    this.videoUrl,
    this.message,
  });

  final String status;
  final int? progress;
  final String? videoUrl;
  final String? message;

  factory ExplainerVideoStatus.fromJson(Map<String, dynamic> json) {
    return ExplainerVideoStatus(
      status: json['status'] as String? ?? 'unknown',
      progress: (json['progress'] as num?)?.toInt(),
      videoUrl: (json['video_url'] as String?) ?? (json['url'] as String?),
      message: json['message'] as String?,
    );
  }
}

class ExplainerVideoRepository {
  ExplainerVideoRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<void> trigger(String courseId) async {
    await _apiClient.dio.post(ApiEndpoints.explainerVideo(courseId));
  }

  Future<ExplainerVideoStatus> getStatus(String courseId) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.explainerVideoStatus(courseId));
    return ExplainerVideoStatus.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<ExplainerVideoStatus> getVideo(String courseId) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.explainerVideo(courseId));
    return ExplainerVideoStatus.fromJson(response.data ?? <String, dynamic>{});
  }
}
