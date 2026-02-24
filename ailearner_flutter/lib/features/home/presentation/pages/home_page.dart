import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../courses/presentation/bloc/courses_cubit.dart';
import '../../../gamification/presentation/bloc/gamification_cubit.dart';
import '../../../courses/presentation/widgets/premium_course_card.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  void _onDestinationSelected(int index) {
    switch (index) {
      case 0:
        context.go('/home');
        break;
      case 1:
        context.push('/my-courses');
        break;
      case 2:
        context.push('/enrolled');
        break;
      case 3:
        context.push('/profile');
        break;
    }
  }

  @override
  void initState() {
    super.initState();
    final coursesCubit = context.read<CoursesCubit>();
    final gamificationCubit = context.read<GamificationCubit>();
    Future.microtask(coursesCubit.loadAll);
    Future.microtask(gamificationCubit.loadMe);
    Future.microtask(() => gamificationCubit.pingActivity(activityType: 'app_open', refreshAfter: false));
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(
        title: Text(
          'AiLearner',
          style: textTheme.titleLarge?.copyWith(
            color: AppColors.textPrimary,
            fontWeight: FontWeight.w800,
          ),
        ),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 16),
            child: CircleAvatar(
              backgroundColor: AppColors.primaryMuted,
              child: Icon(Icons.person, color: AppColors.primary),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/create-course'),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add),
        label: const Text('Create Course'),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
        children: [
          const SizedBox(height: 6),
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.primary, AppColors.primaryDeep],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(22),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Icon(Icons.bolt_rounded, color: Colors.white),
                    const SizedBox(width: 8),
                    Text(
                      'Interactive Learning 2.0',
                      style: textTheme.titleMedium?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Text(
                  'Quiz, summary, flashcards, map and audio — all in one learning hub.',
                  style: textTheme.bodyMedium?.copyWith(color: Colors.white70),
                ),
                const SizedBox(height: 12),
                GestureDetector(
                  onTap: () => context.push('/interactive-hub'),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                    decoration: BoxDecoration(
                      color: AppColors.accent,
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      'Try Demo',
                      style: textTheme.labelLarge?.copyWith(color: Colors.white),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          BlocBuilder<GamificationCubit, GamificationState>(
            builder: (context, state) {
              final snapshot = state.snapshot;
              return Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: AppColors.divider),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          'Gamification',
                          style: textTheme.titleMedium?.copyWith(
                            color: AppColors.textPrimary,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        const Spacer(),
                        IconButton(
                          onPressed: state.loading
                              ? null
                              : () => context.read<GamificationCubit>().loadMe(),
                          icon: const Icon(Icons.refresh),
                        ),
                      ],
                    ),
                    if (state.loading && snapshot == null)
                      const Padding(
                        padding: EdgeInsets.symmetric(vertical: 12),
                        child: Center(child: CircularProgressIndicator()),
                      )
                    else ...[
                      Text(
                        'XP ${snapshot?.xp ?? 0} • Level ${snapshot?.level ?? 0} • Streak ${snapshot?.streakDays ?? 0}d',
                        style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                      ),
                      const SizedBox(height: 8),
                      LinearProgressIndicator(value: snapshot?.levelProgress ?? 0),
                      const SizedBox(height: 8),
                      Align(
                        alignment: Alignment.centerLeft,
                        child: OutlinedButton(
                          onPressed: () => context.push('/gamification'),
                          child: const Text('Open Gamification'),
                        ),
                      ),
                    ],
                  ],
                ),
              );
            },
          ),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Discover Courses',
                style: textTheme.titleLarge?.copyWith(
                  color: AppColors.textPrimary,
                  fontWeight: FontWeight.w800,
                ),
              ),
              TextButton(onPressed: () {}, child: const Text('See all')),
            ],
          ),
          const SizedBox(height: 10),
          SizedBox(
            height: 230,
            child: BlocBuilder<CoursesCubit, CoursesState>(
              builder: (context, state) {
                if (state.loading) {
                  return const Center(child: CircularProgressIndicator());
                }
                final courses = state.publicCourses;
                if (courses.isEmpty) {
                  return Center(
                    child: Text(
                      state.error ?? 'No courses yet',
                      style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                    ),
                  );
                }
                return ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemBuilder: (_, index) {
                    final course = courses[index];
                    return PremiumCourseCard(
                      title: course.title,
                      description: course.description ?? 'No description available',
                      level: course.difficulty ?? 'General',
                      onJoin: () => context.read<CoursesCubit>().enroll(course.id),
                    );
                  },
                  separatorBuilder: (_, _) => const SizedBox(width: 12),
                  itemCount: courses.length,
                );
              },
            ),
          ),
          const SizedBox(height: 12),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: 0,
        onDestinationSelected: _onDestinationSelected,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Home'),
          NavigationDestination(icon: Icon(Icons.school_outlined), selectedIcon: Icon(Icons.school), label: 'My Courses'),
          NavigationDestination(icon: Icon(Icons.bookmark_border), selectedIcon: Icon(Icons.bookmark), label: 'Enrolled'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}
