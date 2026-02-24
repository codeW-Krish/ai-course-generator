import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/analytics_repository.dart';

class AnalyticsState extends Equatable {
  const AnalyticsState({
    this.loading = false,
    this.summary,
    this.error,
  });

  final bool loading;
  final AnalyticsSummary? summary;
  final String? error;

  AnalyticsState copyWith({
    bool? loading,
    AnalyticsSummary? summary,
    String? error,
  }) {
    return AnalyticsState(
      loading: loading ?? this.loading,
      summary: summary ?? this.summary,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, summary, error];
}

class AnalyticsCubit extends Cubit<AnalyticsState> {
  AnalyticsCubit(this._repository) : super(const AnalyticsState());

  final AnalyticsRepository _repository;

  Future<void> load(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final summary = await _repository.loadCourseSummary(courseId);
      emit(state.copyWith(loading: false, summary: summary));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }
}
