import 'dart:async';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/explainer_video_repository.dart';

class ExplainerVideoState extends Equatable {
  const ExplainerVideoState({
    this.loading = false,
    this.polling = false,
    this.status,
    this.error,
  });

  final bool loading;
  final bool polling;
  final ExplainerVideoStatus? status;
  final String? error;

  ExplainerVideoState copyWith({
    bool? loading,
    bool? polling,
    ExplainerVideoStatus? status,
    String? error,
  }) {
    return ExplainerVideoState(
      loading: loading ?? this.loading,
      polling: polling ?? this.polling,
      status: status ?? this.status,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, polling, status, error];
}

class ExplainerVideoCubit extends Cubit<ExplainerVideoState> {
  ExplainerVideoCubit(this._repository) : super(const ExplainerVideoState());

  final ExplainerVideoRepository _repository;
  Timer? _poller;

  Future<void> trigger(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      await _repository.trigger(courseId);
      emit(state.copyWith(loading: false));
      startPolling(courseId);
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> checkOnce(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final status = await _repository.getStatus(courseId);
      emit(state.copyWith(loading: false, status: status));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  void startPolling(String courseId) {
    _poller?.cancel();
    emit(state.copyWith(polling: true));
    _poller = Timer.periodic(const Duration(seconds: 4), (_) async {
      try {
        final status = await _repository.getStatus(courseId);
        emit(state.copyWith(status: status));
        final isDone = status.status.toLowerCase() == 'completed' || status.status.toLowerCase() == 'failed';
        if (isDone) {
          stopPolling();
          if (status.status.toLowerCase() == 'completed') {
            final video = await _repository.getVideo(courseId);
            emit(state.copyWith(status: video));
          }
        }
      } catch (e) {
        emit(state.copyWith(error: e.toString()));
        stopPolling();
      }
    });
  }

  void stopPolling() {
    _poller?.cancel();
    _poller = null;
    emit(state.copyWith(polling: false));
  }

  @override
  Future<void> close() {
    _poller?.cancel();
    return super.close();
  }
}
