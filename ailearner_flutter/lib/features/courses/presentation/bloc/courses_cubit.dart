import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:dio/dio.dart';

import '../../data/course_models.dart';
import '../../data/course_repository.dart';

class CoursesState extends Equatable {
  const CoursesState({
    this.loading = false,
    this.publicCourses = const [],
    this.myCourses = const [],
    this.enrolledCourses = const [],
    this.error,
  });

  final bool loading;
  final List<Course> publicCourses;
  final List<Course> myCourses;
  final List<Course> enrolledCourses;
  final String? error;

  CoursesState copyWith({
    bool? loading,
    List<Course>? publicCourses,
    List<Course>? myCourses,
    List<Course>? enrolledCourses,
    String? error,
  }) {
    return CoursesState(
      loading: loading ?? this.loading,
      publicCourses: publicCourses ?? this.publicCourses,
      myCourses: myCourses ?? this.myCourses,
      enrolledCourses: enrolledCourses ?? this.enrolledCourses,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, publicCourses, myCourses, enrolledCourses, error];
}

class CoursesCubit extends Cubit<CoursesState> {
  CoursesCubit(this._repository) : super(const CoursesState());

  final CourseRepository _repository;

  Future<void> loadAll() async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final results = await Future.wait([
        _repository.getPublicCourses(),
        _repository.getMyCourses(),
        _repository.getEnrolledCourses(),
      ]);
      emit(state.copyWith(
        loading: false,
        publicCourses: results[0],
        myCourses: results[1],
        enrolledCourses: results[2],
      ));
    } catch (e) {
      emit(state.copyWith(loading: false, error: _readErrorMessage(e)));
    }
  }

  Future<void> enroll(String courseId) async {
    await _repository.enrollInCourse(courseId);
    await loadAll();
  }

  String _readErrorMessage(Object error) {
    if (error is DioException) {
      if (error.response?.statusCode == 401) {
        return 'Session expired. Please log in again.';
      }
      final message = error.response?.data;
      if (message is Map && message['message'] is String) {
        return message['message'] as String;
      }
      return 'Unable to load courses right now. Please try again.';
    }
    return 'Something went wrong. Please try again.';
  }
}
