import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../gamification/data/gamification_repository.dart';
import '../../data/interactive_models.dart';
import '../../data/interactive_repository.dart';

class DueFlashcardsState extends Equatable {
  const DueFlashcardsState({
    this.loading = false,
    this.cards = const [],
    this.error,
  });

  final bool loading;
  final List<FlashcardItem> cards;
  final String? error;

  DueFlashcardsState copyWith({
    bool? loading,
    List<FlashcardItem>? cards,
    String? error,
  }) {
    return DueFlashcardsState(
      loading: loading ?? this.loading,
      cards: cards ?? this.cards,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, cards, error];
}

class DueFlashcardsCubit extends Cubit<DueFlashcardsState> {
  DueFlashcardsCubit(this._repository, this._gamificationRepository)
      : super(const DueFlashcardsState());

  final InteractiveRepository _repository;
  final GamificationRepository _gamificationRepository;

  Future<void> load(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final cards = await _repository.getDueFlashcards(courseId);
      emit(state.copyWith(loading: false, cards: cards));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> review({required String flashcardId, required int quality, required String courseId}) async {
    await _repository.reviewFlashcard(flashcardId: flashcardId, quality: quality);
    try {
      await _gamificationRepository.pingActivity(activityType: 'flashcard_review');
    } catch (_) {}
    await load(courseId);
  }
}
