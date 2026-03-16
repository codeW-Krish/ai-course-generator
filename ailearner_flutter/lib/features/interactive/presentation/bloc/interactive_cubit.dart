import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../gamification/data/gamification_repository.dart';
import '../../data/interactive_models.dart';
import '../../data/interactive_repository.dart';

class InteractiveState extends Equatable {
  const InteractiveState({
    this.loading = false,
    this.session,
    this.currentQuestionIndex = 0,
    this.currentQuestionInput = '',
    this.lastVerify,
    this.generatedNotes,
    this.userNote = '',
    this.flashcards = const [],
    this.dueFlashcards = const [],
    this.chat = const [],
    this.error,
  });

  final bool loading;
  final InteractiveSession? session;
  final int currentQuestionIndex;
  final String currentQuestionInput;
  final VerifyAnswerResult? lastVerify;
  final GeneratedNotes? generatedNotes;
  final String userNote;
  final List<FlashcardItem> flashcards;
  final List<FlashcardItem> dueFlashcards;
  final List<ChatMessage> chat;
  final String? error;

  InteractiveQuestion? get currentQuestion {
    if (session == null || session!.questions.isEmpty) return null;
    if (currentQuestionIndex < 0 || currentQuestionIndex >= session!.questions.length) return null;
    return session!.questions[currentQuestionIndex];
  }

  InteractiveState copyWith({
    bool? loading,
    InteractiveSession? session,
    int? currentQuestionIndex,
    String? currentQuestionInput,
    VerifyAnswerResult? lastVerify,
    GeneratedNotes? generatedNotes,
    String? userNote,
    List<FlashcardItem>? flashcards,
    List<FlashcardItem>? dueFlashcards,
    List<ChatMessage>? chat,
    String? error,
  }) {
    return InteractiveState(
      loading: loading ?? this.loading,
      session: session ?? this.session,
      currentQuestionIndex: currentQuestionIndex ?? this.currentQuestionIndex,
      currentQuestionInput: currentQuestionInput ?? this.currentQuestionInput,
      lastVerify: lastVerify ?? this.lastVerify,
      generatedNotes: generatedNotes ?? this.generatedNotes,
      userNote: userNote ?? this.userNote,
      flashcards: flashcards ?? this.flashcards,
      dueFlashcards: dueFlashcards ?? this.dueFlashcards,
      chat: chat ?? this.chat,
      error: error,
    );
  }

  @override
  List<Object?> get props => [
        loading,
        session,
        currentQuestionIndex,
        currentQuestionInput,
        lastVerify,
        generatedNotes,
        userNote,
        flashcards,
        dueFlashcards,
        chat,
        error,
      ];
}

class InteractiveCubit extends Cubit<InteractiveState> {
  InteractiveCubit(this._repository, this._gamificationRepository)
      : super(const InteractiveState());

  final InteractiveRepository _repository;
  final GamificationRepository _gamificationRepository;

  Future<void> loadNext({required String courseId, String provider = 'Groq'}) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final json = await _repository.getNextSubtopic(courseId, provider: provider);
      final session = InteractiveSession.fromJson(json);
      emit(state.copyWith(loading: false, session: session, currentQuestionIndex: 0, currentQuestionInput: ''));
      final subtopicId = session.subtopic?.id;
      if (subtopicId != null && subtopicId.isNotEmpty) {
        await Future.wait([
          loadGeneratedNotes(subtopicId: subtopicId, provider: provider),
          loadUserNote(subtopicId),
          loadFlashcards(subtopicId: subtopicId, provider: provider),
        ]);
      }
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  void setQuestionInput(String value) {
    emit(state.copyWith(currentQuestionInput: value));
  }

  void selectOption(String option) {
    emit(state.copyWith(currentQuestionInput: option));
  }

  Future<void> verifyCurrentAnswer() async {
    final question = state.currentQuestion;
    final subtopicId = state.session?.subtopic?.id;
    if (question == null || subtopicId == null || subtopicId.isEmpty) return;

    emit(state.copyWith(loading: true, error: null));
    try {
      final json = await _repository.verifyAnswer(
        subtopicId: subtopicId,
        questionId: question.id,
        answer: state.currentQuestionInput,
      );
      final verify = VerifyAnswerResult.fromJson(json);
      await _pingActivity('quiz_verify');
      final canMoveNext = verify.correct && state.currentQuestionIndex < (state.session?.questions.length ?? 1) - 1;
      emit(
        state.copyWith(
          loading: false,
          lastVerify: verify,
          currentQuestionIndex: canMoveNext ? state.currentQuestionIndex + 1 : state.currentQuestionIndex,
          currentQuestionInput: '',
        ),
      );
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> sendChat(String message) async {
    final subtopicId = state.session?.subtopic?.id;
    if (subtopicId == null || subtopicId.isEmpty || message.trim().isEmpty) return;

    final current = List<ChatMessage>.from(state.chat)
      ..add(ChatMessage(text: message, isUser: true));
    emit(state.copyWith(chat: current, error: null));

    try {
      final aiReply = await _repository.sendChat(subtopicId: subtopicId, message: message);
      final updated = List<ChatMessage>.from(state.chat)
        ..add(ChatMessage(text: aiReply, isUser: false));
      emit(state.copyWith(chat: updated));
    } catch (e) {
      emit(state.copyWith(error: e.toString()));
    }
  }

  Future<void> loadGeneratedNotes({required String subtopicId, String provider = 'Groq'}) async {
    try {
      final json = await _repository.getGeneratedNotes(subtopicId: subtopicId, provider: provider);
      emit(state.copyWith(generatedNotes: GeneratedNotes.fromJson(json)));
    } catch (e) {
      emit(state.copyWith(error: e.toString()));
    }
  }

  Future<void> loadUserNote(String subtopicId) async {
    try {
      final note = await _repository.getUserNote(subtopicId);
      emit(state.copyWith(userNote: note));
    } catch (_) {}
  }

  Future<void> saveUserNote(String subtopicId, String note) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      await _repository.saveUserNote(subtopicId: subtopicId, note: note);
      emit(state.copyWith(loading: false, userNote: note));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> loadFlashcards({required String subtopicId, String provider = 'Groq'}) async {
    try {
      final cards = await _repository.getFlashcards(subtopicId: subtopicId, provider: provider);
      emit(state.copyWith(flashcards: cards));
    } catch (e) {
      emit(state.copyWith(error: e.toString()));
    }
  }

  Future<void> reviewFlashcard({required String flashcardId, required int quality}) async {
    try {
      await _repository.reviewFlashcard(flashcardId: flashcardId, quality: quality);
      await _pingActivity('flashcard_review');
    } catch (e) {
      emit(state.copyWith(error: e.toString()));
    }
  }

  Future<void> loadDueFlashcards(String courseId) async {
    try {
      final due = await _repository.getDueFlashcards(courseId);
      emit(state.copyWith(dueFlashcards: due));
    } catch (e) {
      emit(state.copyWith(error: e.toString()));
    }
  }

  Future<void> _pingActivity(String type) async {
    try {
      await _gamificationRepository.pingActivity(activityType: type);
    } catch (_) {}
  }
}
