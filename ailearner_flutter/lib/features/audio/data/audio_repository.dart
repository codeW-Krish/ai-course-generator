import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';

class AudioPayload {
  const AudioPayload({
    required this.audioUrl,
    required this.script,
    required this.type,
    required this.durationSec,
  });

  final String audioUrl;
  final String script;
  final String type;
  final int durationSec;

  factory AudioPayload.fromJson(Map<String, dynamic> json) {
    final audio = json['audio'] is Map<String, dynamic>
        ? json['audio'] as Map<String, dynamic>
        : json;
    return AudioPayload(
      audioUrl: audio['audio_url'] as String? ?? '',
      script: audio['script'] as String? ?? '',
      type: audio['type'] as String? ?? 'subtopic',
      durationSec: (audio['estimated_duration'] as num?)?.toInt() ?? 0,
    );
  }
}

class AudioRepository {
  AudioRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<AudioPayload> getSubtopicAudio({required String subtopicId, String ttsProvider = 'Groq', String voice = 'tara'}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.sectionAudio(subtopicId),
      queryParameters: {
        'tts_provider': ttsProvider,
        'voice': voice,
        'llm_provider': 'Groq',
      },
    );
    return AudioPayload.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<AudioPayload> getCourseAudio({required String courseId, String ttsProvider = 'Groq', String voice = 'tara'}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.courseAudio(courseId),
      queryParameters: {
        'tts_provider': ttsProvider,
        'voice': voice,
        'llm_provider': 'Groq',
      },
    );
    return AudioPayload.fromJson(response.data ?? <String, dynamic>{});
  }
}
