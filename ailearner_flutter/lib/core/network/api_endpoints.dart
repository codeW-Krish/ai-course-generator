import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiEndpoints {
  const ApiEndpoints._();

  static String get baseUrl {
    final value = dotenv.env['API_BASE_URL']?.trim();
    if (value == null || value.isEmpty) {
      throw StateError(
        'API_BASE_URL is not configured. Set it in .env (example: http://192.168.1.10:3030).',
      );
    }
    return value;
  }

  static const String register = '/api/auth/register';
  static const String login = '/api/auth/login';
  static const String refresh = '/api/auth/refresh';

  static const String courses = '/api/courses';
  static const String myCourses = '/api/courses/me';
  static const String enrolledCourses = '/api/courses/me/enrolled';
  static String enrollCourse(String id) => '/api/courses/$id/enroll';
  static String generateOutline = '/api/courses/generate-outline';
  static String regenerateOutline(String id) => '/api/courses/$id/outline/regenerate';
  static String generateContent(String id) => '/api/courses/$id/generate-content';
  static String generateContentStream(String id) => '/api/courses/$id/generate-content-stream';
  static String generationStatus(String id) => '/api/courses/$id/generation-status';
  static String courseFull(String id) => '/api/courses/$id/full';

  static String interactiveNext(String courseId) => '/api/interactive/course/$courseId/next';
  static String interactiveSession(String subtopicId) => '/api/interactive/$subtopicId';
  static String interactiveVerify(String subtopicId) => '/api/interactive/$subtopicId/verify';
  static String interactiveChat(String subtopicId) => '/api/interactive/$subtopicId/chat';

  static String subtopicNote(String subtopicId) => '/api/courses/subtopics/$subtopicId/notes';
  static String generatedNotes(String subtopicId) => '/api/notes/$subtopicId/generated';
  static String exportSubtopicNotes(String subtopicId) => '/api/notes/$subtopicId/export';
  static String exportCourseNotes(String courseId) => '/api/notes/course/$courseId/export';

  static String flashcards(String subtopicId) => '/api/flashcards/$subtopicId';
  static String flashcardsReview(String flashcardId) => '/api/flashcards/$flashcardId/review';
  static String dueFlashcards(String courseId) => '/api/flashcards/course/$courseId/due';

  static String sectionAudio(String subtopicId) => '/api/audio/$subtopicId';
  static String courseAudio(String courseId) => '/api/audio/course/$courseId';

  static String explainerVideo(String courseId) => '/api/courses/$courseId/explainer-video';
  static String explainerVideoStatus(String courseId) => '/api/courses/$courseId/explainer-video/status';

  static String completeSubtopic(String subtopicId) => '/api/courses/subtopics/$subtopicId/complete';
  static String courseProgress(String courseId) => '/api/courses/$courseId/progress';

  static const String gamificationMe = '/api/gamification/me';
  static const String gamificationActivityPing = '/api/gamification/activity/ping';

  static const String analyticsSummary = '/api/analytics/summary';
  static String analyticsCourse(String courseId) => '/api/analytics/course/$courseId';
  static const String analyticsWeekly = '/api/analytics/weekly';
}
