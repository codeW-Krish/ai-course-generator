class InteractiveQuestion {
  const InteractiveQuestion({
    required this.id,
    required this.questionText,
    required this.type,
    this.options = const [],
    this.hint,
  });

  final String id;
  final String questionText;
  final String type;
  final List<String> options;
  final String? hint;

  factory InteractiveQuestion.fromJson(Map<String, dynamic> json) {
    final rawOptions = (json['options'] as List?) ?? <dynamic>[];
    return InteractiveQuestion(
      id: json['id'] as String? ?? '',
      questionText: json['question_text'] as String? ?? '',
      type: (json['type'] as String? ?? 'mcq').toLowerCase(),
      options: rawOptions.map((e) => e.toString()).toList(),
      hint: json['hint'] as String?,
    );
  }
}

class InteractiveSubtopic {
  const InteractiveSubtopic({
    required this.id,
    required this.title,
    required this.content,
    required this.courseId,
  });

  final String id;
  final String title;
  final String content;
  final String courseId;

  factory InteractiveSubtopic.fromJson(Map<String, dynamic> json) {
    return InteractiveSubtopic(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      content: json['content'] as String? ?? '',
      courseId: json['course_id'] as String? ?? '',
    );
  }
}

class InteractiveSession {
  const InteractiveSession({
    required this.subtopic,
    required this.questions,
    required this.heartsRemaining,
    required this.attempts,
    required this.isCompleted,
    required this.courseCompleted,
    this.message,
  });

  final InteractiveSubtopic? subtopic;
  final List<InteractiveQuestion> questions;
  final int heartsRemaining;
  final int attempts;
  final bool isCompleted;
  final bool courseCompleted;
  final String? message;

  factory InteractiveSession.fromJson(Map<String, dynamic> json) {
    final rawQuestions = (json['questions'] as List?) ?? <dynamic>[];
    return InteractiveSession(
      subtopic: json['subtopic'] is Map<String, dynamic>
          ? InteractiveSubtopic.fromJson(json['subtopic'] as Map<String, dynamic>)
          : null,
      questions: rawQuestions
          .whereType<Map>()
          .map((e) => InteractiveQuestion.fromJson(e.cast<String, dynamic>()))
          .toList(),
      heartsRemaining: (json['hearts_remaining'] as num?)?.toInt() ?? 3,
      attempts: (json['attempts'] as num?)?.toInt() ?? 0,
      isCompleted: json['is_completed'] as bool? ?? false,
      courseCompleted: json['course_completed'] as bool? ?? false,
      message: json['message'] as String?,
    );
  }
}

class VerifyAnswerResult {
  const VerifyAnswerResult({
    required this.correct,
    this.correctAnswer,
    this.hint,
    required this.heartsRemaining,
    required this.gameOver,
    required this.isSubtopicCompleted,
    this.result,
  });

  final bool correct;
  final String? correctAnswer;
  final String? hint;
  final int heartsRemaining;
  final bool gameOver;
  final bool isSubtopicCompleted;
  final String? result;

  factory VerifyAnswerResult.fromJson(Map<String, dynamic> json) {
    return VerifyAnswerResult(
      correct: json['correct'] as bool? ?? false,
      correctAnswer: json['correct_answer'] as String?,
      hint: json['hint'] as String?,
      heartsRemaining: (json['hearts_remaining'] as num?)?.toInt() ?? 0,
      gameOver: json['game_over'] as bool? ?? false,
      isSubtopicCompleted: json['is_subtopic_completed'] as bool? ?? false,
      result: json['result'] as String?,
    );
  }
}

class GeneratedNotes {
  const GeneratedNotes({
    required this.summary,
    required this.keyPoints,
    required this.raw,
  });

  final String summary;
  final List<String> keyPoints;
  final Map<String, dynamic> raw;

  factory GeneratedNotes.fromJson(Map<String, dynamic> json) {
    final notes = json['notes'] is Map<String, dynamic>
        ? json['notes'] as Map<String, dynamic>
        : json;
    final points = (notes['key_points'] as List?)?.map((e) => e.toString()).toList() ?? <String>[];
    return GeneratedNotes(
      summary: notes['summary'] as String? ?? '',
      keyPoints: points,
      raw: notes,
    );
  }
}

class FlashcardItem {
  const FlashcardItem({
    required this.id,
    required this.front,
    required this.back,
    required this.cardType,
    required this.position,
  });

  final String id;
  final String front;
  final String back;
  final String cardType;
  final int position;

  factory FlashcardItem.fromJson(Map<String, dynamic> json) {
    return FlashcardItem(
      id: json['id'] as String? ?? '',
      front: json['front'] as String? ?? '',
      back: json['back'] as String? ?? '',
      cardType: json['card_type'] as String? ?? 'concept',
      position: (json['position'] as num?)?.toInt() ?? 0,
    );
  }
}

class ChatMessage {
  const ChatMessage({
    required this.text,
    required this.isUser,
  });

  final String text;
  final bool isUser;
}
