import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/interactive_cubit.dart';

class InteractiveHubPage extends StatefulWidget {
  const InteractiveHubPage({super.key});

  @override
  State<InteractiveHubPage> createState() => _InteractiveHubPageState();
}

class _InteractiveHubPageState extends State<InteractiveHubPage> {
  final _courseIdCtrl = TextEditingController(text: '');
  final _chatCtrl = TextEditingController();
  final _noteCtrl = TextEditingController();
  int _selectedTab = 0;
  final Set<String> _revealedCards = <String>{};

  @override
  void dispose() {
    _courseIdCtrl.dispose();
    _chatCtrl.dispose();
    _noteCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 6,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Interactive Hub'),
          bottom: TabBar(
            isScrollable: true,
            onTap: (index) => setState(() => _selectedTab = index),
            tabs: const [
              Tab(text: 'Read'),
              Tab(text: 'Quiz'),
              Tab(text: 'Summary'),
              Tab(text: 'Cards'),
              Tab(text: 'Map'),
              Tab(text: 'Audio'),
            ],
          ),
        ),
        body: BlocConsumer<InteractiveCubit, InteractiveState>(
          listener: (context, state) {
            if (state.error != null && state.error!.isNotEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
            }
            if (_noteCtrl.text != state.userNote) {
              _noteCtrl.text = state.userNote;
            }
          },
          builder: (context, state) {
            final subtopic = state.session?.subtopic;
            final currentQuestion = state.currentQuestion;

            return Container(
              color: AppColors.canvas,
              child: Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 250),
                      curve: Curves.easeOut,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.surface,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: _selectedTab == 1 ? AppColors.primary : AppColors.divider,
                          width: _selectedTab == 1 ? 1.3 : 1,
                        ),
                      ),
                      child: Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: _courseIdCtrl,
                              decoration: const InputDecoration(
                                labelText: 'Course ID',
                                hintText: 'Enter course id and load session',
                                prefixIcon: Icon(Icons.rocket_launch_outlined),
                              ),
                            ),
                          ),
                          const SizedBox(width: 10),
                          ElevatedButton.icon(
                            onPressed: state.loading
                                ? null
                                : () => context.read<InteractiveCubit>().loadNext(courseId: _courseIdCtrl.text.trim()),
                            icon: state.loading
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(strokeWidth: 2),
                                  )
                                : const Icon(Icons.play_arrow_rounded),
                            label: const Text('Load'),
                          ),
                        ],
                      ),
                    ),
                  ),
                  if (subtopic != null)
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 250),
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        decoration: BoxDecoration(
                          color: AppColors.primaryMuted,
                          borderRadius: BorderRadius.circular(999),
                          border: Border.all(color: AppColors.divider),
                        ),
                        child: Row(
                          children: [
                            const Icon(Icons.favorite, size: 16, color: AppColors.accent),
                            const SizedBox(width: 6),
                            Expanded(
                              child: Text(
                                'Subtopic: ${subtopic.title} • Hearts: ${state.lastVerify?.heartsRemaining ?? state.session?.heartsRemaining ?? 3}',
                                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  const SizedBox(height: 8),
                  Expanded(
                    child: TabBarView(
                      children: [
                        _readTab(context, subtopic?.content ?? 'Load a course session to view content.'),
                        _quizTab(context, state, currentQuestion),
                        _summaryTab(context, state),
                        _cardsTab(context, state),
                        _mapTab(context),
                        _audioTab(context),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _readTab(BuildContext context, String markdown) {
    return _panel(
      context,
      title: 'Read',
      child: Markdown(
        data: markdown,
        selectable: true,
      ),
    );
  }

  Widget _quizTab(BuildContext context, InteractiveState state, dynamic currentQuestion) {
    final cubit = context.read<InteractiveCubit>();
    final question = currentQuestion;
    if (question == null) {
      return _panel(
        context,
        title: 'Quiz',
        child: const Text('No active question. Load an interactive session first.'),
      );
    }

    return _panel(
      context,
      title: 'Quiz',
      child: ListView(
        children: [
          TweenAnimationBuilder<double>(
            tween: Tween(begin: 0, end: ((state.currentQuestionIndex + 1) / ((state.session?.questions.isEmpty ?? true) ? 1 : state.session!.questions.length)).clamp(0, 1)),
            duration: const Duration(milliseconds: 280),
            builder: (context, value, _) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  LinearProgressIndicator(value: value),
                  const SizedBox(height: 6),
                  Text(
                    'Question ${state.currentQuestionIndex + 1}/${state.session?.questions.length ?? 1}',
                    style: Theme.of(context).textTheme.labelMedium?.copyWith(color: AppColors.textSecondary),
                  ),
                ],
              );
            },
          ),
          const SizedBox(height: 10),
          Text(question.questionText, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 10),
          if (question.options.isNotEmpty)
            ...question.options.map<Widget>(
              (option) {
                final selected = state.currentQuestionInput == option;
                return GestureDetector(
                  onTap: () => cubit.selectOption(option),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 180),
                    margin: const EdgeInsets.only(bottom: 8),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: selected ? AppColors.primaryMuted : AppColors.surface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: selected ? AppColors.primary : AppColors.divider),
                    ),
                    child: Row(
                      children: [
                        Icon(selected ? Icons.radio_button_checked : Icons.radio_button_off, color: selected ? AppColors.primary : AppColors.textSecondary),
                        const SizedBox(width: 8),
                        Expanded(child: Text(option)),
                      ],
                    ),
                  ),
                );
              },
            ),
          if (question.options.isEmpty)
            TextField(
              onChanged: cubit.setQuestionInput,
              decoration: const InputDecoration(labelText: 'Your answer'),
            ),
          const SizedBox(height: 12),
          ElevatedButton(
            onPressed: state.loading ? null : cubit.verifyCurrentAnswer,
            child: const Text('Verify Answer'),
          ),
          if (state.lastVerify != null) ...[
            const SizedBox(height: 10),
            AnimatedSwitcher(
              duration: const Duration(milliseconds: 220),
              child: Container(
                key: ValueKey(state.lastVerify!.correct),
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: state.lastVerify!.correct ? AppColors.primaryMuted : AppColors.accentSoft,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.divider),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(state.lastVerify!.correct ? '✅ Correct' : '❌ Incorrect'),
                    if (!state.lastVerify!.correctAnswer.isNullOrEmpty)
                      Text('Correct: ${state.lastVerify!.correctAnswer}'),
                    if (!state.lastVerify!.hint.isNullOrEmpty) Text('Hint: ${state.lastVerify!.hint}'),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _summaryTab(BuildContext context, InteractiveState state) {
    final cubit = context.read<InteractiveCubit>();
    final subtopicId = state.session?.subtopic?.id;
    return _panel(
      context,
      title: 'Summary',
      child: ListView(
        children: [
          Text(state.generatedNotes?.summary ?? 'No generated notes yet.'),
          const SizedBox(height: 10),
          ...state.generatedNotes?.keyPoints
                  .asMap()
                  .entries
                  .map(
                    (entry) => TweenAnimationBuilder<double>(
                      tween: Tween(begin: 0, end: 1),
                      duration: Duration(milliseconds: 160 + (entry.key * 70)),
                      builder: (context, t, child) => Opacity(opacity: t, child: child),
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 4),
                        child: Text('• ${entry.value}'),
                      ),
                    ),
                  )
                  .toList() ??
              [const SizedBox.shrink()],
          const SizedBox(height: 12),
          TextField(
            controller: _noteCtrl,
            minLines: 4,
            maxLines: 8,
            decoration: const InputDecoration(labelText: 'Your notes (saved to backend)'),
          ),
          const SizedBox(height: 10),
          ElevatedButton(
            onPressed: subtopicId == null
                ? null
                : () => cubit.saveUserNote(subtopicId, _noteCtrl.text),
            child: const Text('Save Note'),
          ),
        ],
      ),
    );
  }

  Widget _cardsTab(BuildContext context, InteractiveState state) {
    final cubit = context.read<InteractiveCubit>();
    return _panel(
      context,
      title: 'Cards',
      child: ListView.separated(
        itemCount: state.flashcards.length,
        separatorBuilder: (_, _) => const SizedBox(height: 10),
        itemBuilder: (context, index) {
          final card = state.flashcards[index];
          final revealed = _revealedCards.contains(card.id);
          return Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: revealed ? AppColors.primaryMuted : AppColors.surface,
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: AppColors.divider),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text('Card ${index + 1}', style: Theme.of(context).textTheme.labelMedium?.copyWith(color: AppColors.primary)),
                    const Spacer(),
                    IconButton(
                      onPressed: () {
                        setState(() {
                          if (revealed) {
                            _revealedCards.remove(card.id);
                          } else {
                            _revealedCards.add(card.id);
                          }
                        });
                      },
                      icon: Icon(revealed ? Icons.visibility : Icons.visibility_outlined),
                    ),
                  ],
                ),
                AnimatedSwitcher(
                  duration: const Duration(milliseconds: 200),
                  child: Text(
                    revealed ? card.back : card.front,
                    key: ValueKey(revealed),
                    style: Theme.of(context).textTheme.titleSmall,
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    OutlinedButton(onPressed: () => cubit.reviewFlashcard(flashcardId: card.id, quality: 2), child: const Text('Hard')),
                    const SizedBox(width: 8),
                    OutlinedButton(onPressed: () => cubit.reviewFlashcard(flashcardId: card.id, quality: 4), child: const Text('Good')),
                    const SizedBox(width: 8),
                    OutlinedButton(onPressed: () => cubit.reviewFlashcard(flashcardId: card.id, quality: 5), child: const Text('Easy')),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _mapTab(BuildContext context) {
    final courseId = context.select((InteractiveCubit cubit) => cubit.state.session?.subtopic?.courseId ?? '');
    return _panel(
      context,
      title: 'Map',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Mind map rendering now loads from /api/courses/:id/full.'),
          const SizedBox(height: 10),
          OutlinedButton(
            onPressed: () => context.push(courseId.isNotEmpty ? '/mind-map/$courseId' : '/mind-map'),
            child: Text(courseId.isNotEmpty ? 'Open Mind Map for Current Course' : 'Open Mind Map'),
          ),
        ],
      ),
    );
  }

  Widget _audioTab(BuildContext context) {
    return _panel(
      context,
      title: 'Audio',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Audio overview endpoint is integrated; open full player module from Home.'),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: () => context.push('/audio-overview'),
            child: const Text('Open Audio Module'),
          ),
          const SizedBox(height: 16),
          const Divider(),
          const SizedBox(height: 8),
          const Text('Context Chat'),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _chatCtrl,
                  decoration: const InputDecoration(labelText: 'Ask AI about this subtopic'),
                ),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: () {
                  context.read<InteractiveCubit>().sendChat(_chatCtrl.text.trim());
                  _chatCtrl.clear();
                },
                child: const Text('Send'),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Expanded(
            child: BlocBuilder<InteractiveCubit, InteractiveState>(
              builder: (context, state) {
                return ListView.builder(
                  itemCount: state.chat.length,
                  itemBuilder: (context, index) {
                    final m = state.chat[index];
                    return TweenAnimationBuilder<double>(
                      tween: Tween(begin: 0, end: 1),
                      duration: const Duration(milliseconds: 180),
                      builder: (context, t, child) {
                        return Opacity(
                          opacity: t,
                          child: Transform.translate(offset: Offset((1 - t) * (m.isUser ? 14 : -14), 0), child: child),
                        );
                      },
                      child: Align(
                        alignment: m.isUser ? Alignment.centerRight : Alignment.centerLeft,
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 8),
                          padding: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                            color: m.isUser ? AppColors.primaryMuted : AppColors.surface,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(color: AppColors.divider),
                          ),
                          child: Text(m.text),
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _panel(BuildContext context, {required String title, required Widget child}) {
    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: AppColors.divider),
        ),
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
            const SizedBox(height: 8),
            Expanded(child: child),
          ],
        ),
      ),
    );
  }
}

extension _NullOrEmpty on String? {
  bool get isNullOrEmpty => this == null || this!.trim().isEmpty;
}
