import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';
import 'interactive_models.dart';

class InteractiveRepository {
  InteractiveRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<Map<String, dynamic>> getNextSubtopic(String courseId, {String provider = 'Groq', String? model}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.interactiveNext(courseId),
      queryParameters: {
        'provider': provider,
        'model': ?model,
      },
    );
    return response.data ?? <String, dynamic>{};
  }

  Future<Map<String, dynamic>> getSession(String subtopicId, {String provider = 'Groq', String? model}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.interactiveSession(subtopicId),
      queryParameters: {
        'provider': provider,
        'model': ?model,
      },
    );
    return response.data ?? <String, dynamic>{};
  }

  Future<Map<String, dynamic>> verifyAnswer({
    required String subtopicId,
    required String questionId,
    required String answer,
  }) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      ApiEndpoints.interactiveVerify(subtopicId),
      data: {'questionId': questionId, 'answer': answer},
    );
    return response.data ?? <String, dynamic>{};
  }

  Future<String> sendChat({required String subtopicId, required String message}) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      ApiEndpoints.interactiveChat(subtopicId),
      data: {'message': message},
    );
    return response.data?['ai_response'] as String? ?? response.data?['response'] as String? ?? '';
  }

  Future<Map<String, dynamic>> getGeneratedNotes({required String subtopicId, String provider = 'Groq', String? model}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.generatedNotes(subtopicId),
      queryParameters: {
        'provider': provider,
        'model': ?model,
      },
    );
    return response.data ?? <String, dynamic>{};
  }

  Future<String> getUserNote(String subtopicId) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.subtopicNote(subtopicId));
    return response.data?['note'] as String? ?? '';
  }

  Future<void> saveUserNote({required String subtopicId, required String note}) async {
    await _apiClient.dio.post(
      ApiEndpoints.subtopicNote(subtopicId),
      data: {'note': note},
    );
  }

  Future<List<FlashcardItem>> getFlashcards({required String subtopicId, String provider = 'Groq', String? model}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.flashcards(subtopicId),
      queryParameters: {
        'provider': provider,
        'model': ?model,
      },
    );
    final cards = (response.data?['flashcards'] as List?) ?? <dynamic>[];
    return cards
        .whereType<Map>()
        .map((e) => FlashcardItem.fromJson(e.cast<String, dynamic>()))
        .toList();
  }

  Future<void> reviewFlashcard({required String flashcardId, required int quality}) async {
    await _apiClient.dio.post(
      ApiEndpoints.flashcardsReview(flashcardId),
      data: {'quality': quality},
    );
  }

  Future<List<FlashcardItem>> getDueFlashcards(String courseId) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.dueFlashcards(courseId));
    final cards = (response.data?['dueCards'] as List?) ?? <dynamic>[];
    return cards
        .whereType<Map>()
        .map((e) => FlashcardItem.fromJson(e.cast<String, dynamic>()))
        .toList();
  }
}
