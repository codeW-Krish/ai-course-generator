class Course {
  const Course({
    required this.id,
    required this.title,
    this.description,
    this.difficulty,
  });

  final String id;
  final String title;
  final String? description;
  final String? difficulty;

  factory Course.fromJson(Map<String, dynamic> json) {
    return Course(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? 'Untitled',
      description: json['description'] as String?,
      difficulty: json['difficulty'] as String?,
    );
  }
}

class GenerateOutlineRequest {
  const GenerateOutlineRequest({
    required this.title,
    required this.description,
    required this.numUnits,
    required this.difficulty,
    required this.includeVideos,
    this.provider = 'Gemini',
    this.model,
  });

  final String title;
  final String description;
  final int numUnits;
  final String difficulty;
  final bool includeVideos;
  final String provider;
  final String? model;

  Map<String, dynamic> toJson() => {
      'title': title,
      'course_title': title,
        'description': description,
      'numUnits': numUnits,
        'num_units': numUnits,
        'difficulty': difficulty,
      'includeVideos': includeVideos,
        'include_youtube': includeVideos,
        'provider': provider,
        'model': model,
      };
}

class GenerationStatus {
  const GenerationStatus({
    required this.status,
    required this.generatedSubtopics,
    required this.lastUpdated,
  });

  final String status;
  final int generatedSubtopics;
  final String lastUpdated;

  factory GenerationStatus.fromJson(Map<String, dynamic> json) {
    return GenerationStatus(
      status: json['status'] as String? ?? 'unknown',
      generatedSubtopics: (json['generatedSubtopics'] as num?)?.toInt() ??
          (json['generated_subtopics'] as num?)?.toInt() ??
          0,
      lastUpdated: json['lastUpdated'] as String? ?? '',
    );
  }
}

class GenerationStreamEvent {
  const GenerationStreamEvent({
    required this.type,
    this.message,
    this.progress,
    this.generated,
    this.total,
    this.subtopic,
    this.unit,
    this.raw,
  });

  final String type;
  final String? message;
  final int? progress;
  final int? generated;
  final int? total;
  final String? subtopic;
  final String? unit;
  final Map<String, dynamic>? raw;

  factory GenerationStreamEvent.fromJson(Map<String, dynamic> json) {
    return GenerationStreamEvent(
      type: (json['type'] as String? ?? 'unknown').toLowerCase(),
      message: json['message'] as String?,
      progress: (json['progress'] as num?)?.toInt(),
      generated: (json['generated'] as num?)?.toInt(),
      total: (json['total'] as num?)?.toInt(),
      subtopic: json['subtopic'] as String?,
      unit: json['unit'] as String?,
      raw: json,
    );
  }
}
