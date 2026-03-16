import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/due_flashcards_cubit.dart';

class DueFlashcardsPage extends StatefulWidget {
  const DueFlashcardsPage({super.key, this.initialCourseId});

  final String? initialCourseId;

  @override
  State<DueFlashcardsPage> createState() => _DueFlashcardsPageState();
}

class _DueFlashcardsPageState extends State<DueFlashcardsPage> {
  late final TextEditingController _courseCtrl;
  final Set<String> _reviewingIds = <String>{};

  @override
  void initState() {
    super.initState();
    _courseCtrl = TextEditingController(text: widget.initialCourseId ?? '');
    if ((widget.initialCourseId ?? '').isNotEmpty) {
      final cubit = context.read<DueFlashcardsCubit>();
      Future.microtask(() => cubit.load(widget.initialCourseId!));
    }
  }

  @override
  void dispose() {
    _courseCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Due Flashcards')),
      body: BlocConsumer<DueFlashcardsCubit, DueFlashcardsState>(
        listener: (context, state) {
          if (state.error != null && state.error!.isNotEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
          }
        },
        builder: (context, state) {
          return Container(
            color: AppColors.canvas,
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: AppColors.divider),
                  ),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: _courseCtrl,
                              decoration: const InputDecoration(
                                labelText: 'Course ID',
                                prefixIcon: Icon(Icons.school_outlined),
                              ),
                            ),
                          ),
                          const SizedBox(width: 10),
                          ElevatedButton.icon(
                            onPressed: state.loading
                                ? null
                                : () => context.read<DueFlashcardsCubit>().load(_courseCtrl.text.trim()),
                            icon: const Icon(Icons.sync),
                            label: const Text('Load'),
                          ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          _statChip(
                            context,
                            icon: Icons.timer_outlined,
                            label: 'Due now',
                            value: '${state.cards.length}',
                            color: AppColors.accent,
                          ),
                          const SizedBox(width: 10),
                          _statChip(
                            context,
                            icon: Icons.auto_awesome,
                            label: 'Mode',
                            value: state.loading ? 'Loading' : 'Ready',
                            color: AppColors.primary,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 14),
                Expanded(
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 280),
                    switchInCurve: Curves.easeOutCubic,
                    child: state.loading
                        ? const Center(
                            key: ValueKey('loading'),
                            child: CircularProgressIndicator(),
                          )
                        : state.cards.isEmpty
                            ? Center(
                                key: const ValueKey('empty'),
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    const Icon(Icons.inbox_outlined, size: 42, color: AppColors.textSecondary),
                                    const SizedBox(height: 8),
                                    Text(
                                      'No due cards for this course.',
                                      style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                                    ),
                                  ],
                                ),
                              )
                            : ListView.separated(
                                key: const ValueKey('list'),
                                itemCount: state.cards.length,
                                separatorBuilder: (_, _) => const SizedBox(height: 10),
                                itemBuilder: (context, index) {
                                  final card = state.cards[index];
                                  return TweenAnimationBuilder<double>(
                                    tween: Tween(begin: 0, end: 1),
                                    duration: Duration(milliseconds: 220 + (index * 45).clamp(0, 240)),
                                    curve: Curves.easeOut,
                                    builder: (context, t, child) {
                                      return Opacity(
                                        opacity: t,
                                        child: Transform.translate(
                                          offset: Offset(0, (1 - t) * 16),
                                          child: child,
                                        ),
                                      );
                                    },
                                    child: _DueCard(
                                      cardId: card.id,
                                      front: card.front,
                                      back: card.back,
                                      isBusy: _reviewingIds.contains(card.id),
                                      onHard: () async {
                                        setState(() => _reviewingIds.add(card.id));
                                        await context.read<DueFlashcardsCubit>().review(
                                              flashcardId: card.id,
                                              quality: 2,
                                              courseId: _courseCtrl.text.trim(),
                                            );
                                        if (mounted) setState(() => _reviewingIds.remove(card.id));
                                      },
                                      onGood: () async {
                                        setState(() => _reviewingIds.add(card.id));
                                        await context.read<DueFlashcardsCubit>().review(
                                              flashcardId: card.id,
                                              quality: 4,
                                              courseId: _courseCtrl.text.trim(),
                                            );
                                        if (mounted) setState(() => _reviewingIds.remove(card.id));
                                      },
                                    ),
                                  );
                                },
                              ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _statChip(
    BuildContext context, {
    required IconData icon,
    required String label,
    required String value,
    required Color color,
  }) {
    final textTheme = Theme.of(context).textTheme;
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withValues(alpha: 0.26)),
        ),
        child: Row(
          children: [
            Icon(icon, size: 16, color: color),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                '$label · $value',
                style: textTheme.labelMedium?.copyWith(color: color, fontWeight: FontWeight.w700),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DueCard extends StatefulWidget {
  const _DueCard({
    required this.cardId,
    required this.front,
    required this.back,
    required this.isBusy,
    required this.onHard,
    required this.onGood,
  });

  final String cardId;
  final String front;
  final String back;
  final bool isBusy;
  final Future<void> Function() onHard;
  final Future<void> Function() onGood;

  @override
  State<_DueCard> createState() => _DueCardState();
}

class _DueCardState extends State<_DueCard> {
  bool _revealed = false;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return GestureDetector(
      onTap: () => setState(() => _revealed = !_revealed),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 260),
        curve: Curves.easeOutCubic,
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: _revealed ? AppColors.primaryMuted : AppColors.surface,
          border: Border.all(color: AppColors.divider),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(_revealed ? Icons.visibility : Icons.visibility_outlined, size: 18, color: AppColors.primary),
                const SizedBox(width: 6),
                Text(
                  _revealed ? 'Answer' : 'Question',
                  style: textTheme.labelMedium?.copyWith(color: AppColors.primary, fontWeight: FontWeight.w700),
                ),
              ],
            ),
            const SizedBox(height: 8),
            AnimatedSwitcher(
              duration: const Duration(milliseconds: 220),
              transitionBuilder: (child, animation) => FadeTransition(opacity: animation, child: child),
              child: Text(
                _revealed ? widget.back : widget.front,
                key: ValueKey(_revealed),
                style: textTheme.bodyLarge?.copyWith(color: AppColors.textPrimary, fontWeight: FontWeight.w600),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                OutlinedButton.icon(
                  onPressed: widget.isBusy ? null : widget.onHard,
                  icon: const Icon(Icons.trending_down, size: 16),
                  label: const Text('Hard'),
                ),
                const SizedBox(width: 8),
                FilledButton.icon(
                  onPressed: widget.isBusy ? null : widget.onGood,
                  icon: widget.isBusy
                      ? const SizedBox(
                          width: 14,
                          height: 14,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check, size: 16),
                  label: const Text('Good'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
