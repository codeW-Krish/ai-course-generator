import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/theme/app_colors.dart';
import '../../data/gamification_models.dart';
import '../bloc/gamification_cubit.dart';

class GamificationPage extends StatefulWidget {
  const GamificationPage({super.key});

  @override
  State<GamificationPage> createState() => _GamificationPageState();
}

class _GamificationPageState extends State<GamificationPage> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => context.read<GamificationCubit>().loadMe());
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(title: const Text('Gamification')),
      body: BlocConsumer<GamificationCubit, GamificationState>(
        listener: (context, state) {
          if (state.error != null && state.error!.isNotEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
          }
        },
        builder: (context, state) {
          final snapshot = state.snapshot;
          if (state.loading && snapshot == null) {
            return const Center(child: CircularProgressIndicator());
          }

          return RefreshIndicator(
            onRefresh: () => context.read<GamificationCubit>().loadMe(),
            child: ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              children: [
                _SummaryCard(snapshot: snapshot),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: state.pinging
                            ? null
                            : () => context.read<GamificationCubit>().pingActivity(activityType: 'app_open'),
                        icon: const Icon(Icons.local_fire_department_outlined),
                        label: Text(state.pinging ? 'Syncing...' : 'Ping Activity'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Text(
                  'Achievements',
                  style: textTheme.titleMedium?.copyWith(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 8),
                if ((snapshot?.achievements.isEmpty ?? true))
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: AppColors.divider),
                    ),
                    child: Text(
                      'No achievements yet.',
                      style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                    ),
                  )
                else
                  ...snapshot!.achievements.map((item) => _AchievementTile(item: item)),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({required this.snapshot});

  final GamificationSnapshot? snapshot;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final data = snapshot;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.primary, AppColors.primaryDeep],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Your Progress',
            style: textTheme.titleMedium?.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'XP: ${data?.xp ?? 0}   •   Level: ${data?.level ?? 0}',
            style: textTheme.bodyLarge?.copyWith(color: Colors.white),
          ),
          const SizedBox(height: 4),
          Text(
            'Streak: ${data?.streakDays ?? 0} days   •   Activities: ${data?.totalActivities ?? 0}',
            style: textTheme.bodyMedium?.copyWith(color: Colors.white70),
          ),
          const SizedBox(height: 10),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              minHeight: 8,
              value: data?.levelProgress ?? 0,
              backgroundColor: Colors.white24,
              valueColor: const AlwaysStoppedAnimation<Color>(AppColors.accent),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Next level target: ${data?.nextLevelXp ?? 0} XP',
            style: textTheme.bodySmall?.copyWith(color: Colors.white70),
          ),
        ],
      ),
    );
  }
}

class _AchievementTile extends StatelessWidget {
  const _AchievementTile({required this.item});

  final AchievementItem item;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final badgeColor = item.unlocked ? AppColors.success : AppColors.textSecondary;

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.divider),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 18,
            backgroundColor: AppColors.softPanel,
            child: Icon(
              item.unlocked ? Icons.verified_rounded : Icons.lock_outline,
              color: badgeColor,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.title,
                  style: textTheme.titleSmall?.copyWith(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                if (item.description.isNotEmpty)
                  Text(
                    item.description,
                    style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
                  ),
              ],
            ),
          ),
          Text(
            item.unlocked ? 'Unlocked' : 'Locked',
            style: textTheme.labelMedium?.copyWith(color: badgeColor, fontWeight: FontWeight.w700),
          ),
        ],
      ),
    );
  }
}
