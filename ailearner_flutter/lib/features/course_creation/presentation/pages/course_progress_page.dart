import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../courses/data/course_repository.dart';

class CourseProgressPage extends StatefulWidget {
  const CourseProgressPage({required this.courseId, super.key});

  final String courseId;

  @override
  State<CourseProgressPage> createState() => _CourseProgressPageState();
}

class _CourseProgressPageState extends State<CourseProgressPage> {
  Timer? _pollTimer;
  String _statusText = 'Preparing generation...';
  String _statusKey = 'generating';
  int _generatedCount = 0;
  bool _completed = false;

  @override
  void initState() {
    super.initState();
    _startPolling();
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  void _startPolling() {
    _checkOnce();
    _pollTimer = Timer.periodic(const Duration(seconds: 3), (_) => _checkOnce());
  }

  Future<void> _checkOnce() async {
    try {
      final repository = context.read<CourseRepository>();
      final status = await repository.getGenerationStatus(widget.courseId);

      if (!mounted) return;

      final normalized = status.status.trim().toLowerCase();
      setState(() {
        _generatedCount = status.generatedSubtopics;
        _statusKey = normalized;
        if (normalized == 'completed') {
          _statusText = 'Course content generated successfully!';
          _completed = true;
          _pollTimer?.cancel();
        } else if (normalized == 'failed') {
          _statusText = 'Generation failed. Please try again.';
          _pollTimer?.cancel();
        } else {
          _statusText = 'Generating: ${status.generatedSubtopics} subtopics created...';
        }
      });
    } catch (_) {
      // Keep polling
    }
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        title: Text(
          'Course Content',
          style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
        actions: [
          PopupMenuButton<String>(
            onSelected: (_) {},
            itemBuilder: (context) => [
              const PopupMenuItem(value: 'status', child: Text('Check Status')),
            ],
          ),
        ],
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (!_completed && _statusKey != 'failed') ...[
                const SizedBox(
                  width: 64,
                  height: 64,
                  child: CircularProgressIndicator(strokeWidth: 3),
                ),
                const SizedBox(height: 28),
                Text(
                  'Generating Course',
                  style: textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  _statusText,
                  style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                  textAlign: TextAlign.center,
                ),
                if (_generatedCount > 0) ...[
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      color: AppColors.primaryMuted,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      '$_generatedCount subtopics generated',
                      style: textTheme.labelLarge?.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ] else if (_completed) ...[
                Container(
                  width: 72,
                  height: 72,
                  decoration: const BoxDecoration(
                    color: Color(0xFFD1FAE5),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.check_rounded, color: AppColors.success, size: 40),
                ),
                const SizedBox(height: 24),
                Text(
                  'Course Ready!',
                  style: textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '$_generatedCount subtopics generated successfully',
                  style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 28),
                ElevatedButton.icon(
                  onPressed: () => context.push('/course-view/${widget.courseId}'),
                  icon: const Icon(Icons.menu_book),
                  label: const Text('View Course Content'),
                ),
                const SizedBox(height: 12),
                OutlinedButton(
                  onPressed: () => context.go('/home'),
                  child: const Text('Back to Home'),
                ),
              ] else ...[
                // Failed state
                Container(
                  width: 72,
                  height: 72,
                  decoration: const BoxDecoration(
                    color: Color(0xFFFEE2E2),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.error_outline, color: Color(0xFFB42318), size: 40),
                ),
                const SizedBox(height: 24),
                Text(
                  'Generation Failed',
                  style: textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  _statusText,
                  style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 28),
                ElevatedButton(
                  onPressed: () => context.pop(),
                  child: const Text('Go Back & Try Again'),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
