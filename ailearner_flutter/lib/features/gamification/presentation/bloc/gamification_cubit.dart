import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:dio/dio.dart';

import '../../data/gamification_models.dart';
import '../../data/gamification_repository.dart';

class GamificationState extends Equatable {
  const GamificationState({
    this.loading = false,
    this.pinging = false,
    this.snapshot,
    this.error,
  });

  final bool loading;
  final bool pinging;
  final GamificationSnapshot? snapshot;
  final String? error;

  GamificationState copyWith({
    bool? loading,
    bool? pinging,
    GamificationSnapshot? snapshot,
    String? error,
  }) {
    return GamificationState(
      loading: loading ?? this.loading,
      pinging: pinging ?? this.pinging,
      snapshot: snapshot ?? this.snapshot,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, pinging, snapshot, error];
}

class GamificationCubit extends Cubit<GamificationState> {
  GamificationCubit(this._repository) : super(const GamificationState());

  final GamificationRepository _repository;

  Future<void> loadMe() async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final snapshot = await _repository.loadMe();
      emit(state.copyWith(loading: false, snapshot: snapshot));
    } catch (e) {
      emit(state.copyWith(loading: false, error: _readErrorMessage(e)));
    }
  }

  Future<void> pingActivity({required String activityType, bool refreshAfter = true}) async {
    emit(state.copyWith(pinging: true, error: null));
    try {
      await _repository.pingActivity(activityType: activityType);
      if (refreshAfter) {
        final snapshot = await _repository.loadMe();
        emit(state.copyWith(pinging: false, snapshot: snapshot));
      } else {
        emit(state.copyWith(pinging: false));
      }
    } catch (e) {
      emit(state.copyWith(pinging: false, error: _readErrorMessage(e)));
    }
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
      return 'Unable to load gamification data right now.';
    }
    return 'Something went wrong. Please try again.';
  }
}
