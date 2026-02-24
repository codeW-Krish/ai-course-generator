import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';
import 'gamification_models.dart';

class GamificationRepository {
  GamificationRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<GamificationSnapshot> loadMe() async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.gamificationMe);
    return GamificationSnapshot.fromJson(response.data ?? const <String, dynamic>{});
  }

  Future<void> pingActivity({required String activityType, Map<String, dynamic>? metadata}) async {
    await _apiClient.dio.post(
      ApiEndpoints.gamificationActivityPing,
      data: {
        'activityType': activityType,
        'type': activityType,
        if (metadata != null) 'metadata': metadata,
      },
    );
  }
}
