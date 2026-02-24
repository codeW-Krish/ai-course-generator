import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/audio_repository.dart';

class AudioState extends Equatable {
  const AudioState({
    this.loading = false,
    this.payload,
    this.error,
  });

  final bool loading;
  final AudioPayload? payload;
  final String? error;

  AudioState copyWith({
    bool? loading,
    AudioPayload? payload,
    String? error,
  }) {
    return AudioState(
      loading: loading ?? this.loading,
      payload: payload ?? this.payload,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, payload, error];
}

class AudioCubit extends Cubit<AudioState> {
  AudioCubit(this._repository) : super(const AudioState());

  final AudioRepository _repository;

  Future<void> loadSubtopic(String subtopicId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final payload = await _repository.getSubtopicAudio(subtopicId: subtopicId);
      emit(state.copyWith(loading: false, payload: payload));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> loadCourse(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final payload = await _repository.getCourseAudio(courseId: courseId);
      emit(state.copyWith(loading: false, payload: payload));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }
}
