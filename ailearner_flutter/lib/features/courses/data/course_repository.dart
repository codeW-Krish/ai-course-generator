import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';
import 'course_models.dart';

class CourseRepository {
  CourseRepository(this._apiClient);

  final ApiClient _apiClient;

  Future<List<Course>> getPublicCourses() async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.courses);
    final courses = (response.data?['courses'] as List?) ?? <dynamic>[];
    return courses
        .whereType<Map>()
        .map((e) => Course.fromJson(e.cast<String, dynamic>()))
        .toList();
  }

  Future<List<Course>> getMyCourses() async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.myCourses);
    final courses = (response.data?['myCourses'] as List?) ?? <dynamic>[];
    return courses
        .whereType<Map>()
        .map((e) => Course.fromJson(e.cast<String, dynamic>()))
        .toList();
  }

  Future<List<Course>> getEnrolledCourses() async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.enrolledCourses);
    final courses = (response.data?['enrolledCourses'] as List?) ?? <dynamic>[];
    return courses
        .whereType<Map>()
        .map((e) => Course.fromJson(e.cast<String, dynamic>()))
        .toList();
  }

  Future<void> enrollInCourse(String courseId) async {
    await _apiClient.dio.post(ApiEndpoints.enrollCourse(courseId));
  }

  Future<String> generateOutline(GenerateOutlineRequest request) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      ApiEndpoints.generateOutline,
      data: request.toJson(),
    );
    return response.data?['courseId'] as String? ?? '';
  }

  Future<void> generateContent(String courseId, {String? provider, String? model}) async {
    await _apiClient.dio.post(
      ApiEndpoints.generateContent(courseId),
      data: {
        'provider': ?provider,
        'model': ?model,
      },
    );
  }

  Stream<GenerationStreamEvent> generateContentStream(
    String courseId, {
    String provider = 'Groq',
    String? model,
  }) async* {
    Response<ResponseBody> response;
    try {
      response = await _apiClient.dio.post<ResponseBody>(
        ApiEndpoints.generateContentStream(courseId),
        data: {
          'provider': provider,
          'model': ?model,
        },
        options: Options(
          responseType: ResponseType.stream,
          headers: {'Accept': 'text/event-stream'},
        ),
      );
    } catch (e) {
      yield GenerationStreamEvent(type: 'error', message: 'Failed to connect stream: $e');
      return;
    }

    final body = response.data;
    if (body == null) {
      yield const GenerationStreamEvent(type: 'error', message: 'Empty stream response');
      return;
    }

    final lineStream = body.stream
        .transform(const Utf8Decoder() as StreamTransformer<Uint8List, dynamic>)
        .transform(const LineSplitter());

    final dataLines = <String>[];

    await for (final line in lineStream) {
      if (line.startsWith('data:')) {
        dataLines.add(line.substring(5).trim());
        continue;
      }

      if (line.trim().isEmpty) {
        if (dataLines.isNotEmpty) {
          final payload = dataLines.join('\n').trim();
          dataLines.clear();
          if (payload.isNotEmpty) {
            try {
              final decoded = jsonDecode(payload);
              if (decoded is Map<String, dynamic>) {
                yield GenerationStreamEvent.fromJson(decoded);
              } else {
                yield GenerationStreamEvent(type: 'warning', message: payload);
              }
            } catch (_) {
              yield GenerationStreamEvent(type: 'warning', message: payload);
            }
          }
        }
      }
    }

    if (dataLines.isNotEmpty) {
      final payload = dataLines.join('\n').trim();
      try {
        final decoded = jsonDecode(payload);
        if (decoded is Map<String, dynamic>) {
          yield GenerationStreamEvent.fromJson(decoded);
        }
      } catch (_) {
        yield GenerationStreamEvent(type: 'warning', message: payload);
      }
    }
  }

  Future<GenerationStatus> getGenerationStatus(String courseId, {String? since}) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(
      ApiEndpoints.generationStatus(courseId),
      queryParameters: {
        'since': ?since,
      },
    );
    return GenerationStatus.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<Map<String, dynamic>> getFullCourse(String courseId) async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>(ApiEndpoints.courseFull(courseId));
    return response.data ?? <String, dynamic>{};
  }

  Future<List<String>> getCourseProgress(String courseId) async {
    final response = await _apiClient.dio.get<List<dynamic>>(ApiEndpoints.courseProgress(courseId));
    final list = response.data ?? <dynamic>[];
    return list
        .whereType<Map>()
        .map((e) => (e['subtopic_id'] ?? '').toString())
        .where((id) => id.isNotEmpty)
        .toList();
  }
}
