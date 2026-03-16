import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../data/course_models.dart';
import '../bloc/courses_cubit.dart';

class EnrolledCoursesPage extends StatefulWidget {
  const EnrolledCoursesPage({super.key});

  @override
  State<EnrolledCoursesPage> createState() => _EnrolledCoursesPageState();
}

class _EnrolledCoursesPageState extends State<EnrolledCoursesPage> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => context.read<CoursesCubit>().loadAll());
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(
        title: Text(
          'Enrolled Courses',
          style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
      ),
      body: BlocBuilder<CoursesCubit, CoursesState>(
        builder: (context, state) {
          if (state.loading) {
            return const Center(child: CircularProgressIndicator());
          }

          final courses = state.enrolledCourses;

          if (courses.isEmpty) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.bookmark_border, size: 64, color: AppColors.textSecondary.withOpacity(0.5)),
                  const SizedBox(height: 16),
                  Text(
                    'No enrolled courses yet',
                    style: textTheme.titleMedium?.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Browse and join courses from the home page',
                    style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: () => context.read<CoursesCubit>().loadAll(),
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: courses.length,
              itemBuilder: (context, index) {
                final course = courses[index];
                return _EnrolledCourseItem(
                  course: course,
                  onTap: () => context.push('/course-view/${course.id}'),
                );
              },
            ),
          );
        },
      ),
    );
  }
}

class _EnrolledCourseItem extends StatelessWidget {
  const _EnrolledCourseItem({required this.course, required this.onTap});

  final Course course;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.divider),
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text(
          course.title,
          style: textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w600,
            color: AppColors.textPrimary,
          ),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (course.description != null && course.description!.isNotEmpty) ...[
              const SizedBox(height: 4),
              Text(
                course.description!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
              ),
            ],
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 2),
              decoration: BoxDecoration(
                color: AppColors.primaryMuted,
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                course.difficulty ?? 'General',
                style: textTheme.labelSmall?.copyWith(
                  color: AppColors.primary,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
        ),
        trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
        onTap: onTap,
      ),
    );
  }
}
