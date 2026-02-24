import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../courses/data/course_repository.dart';

class ReviewOutlinePage extends StatefulWidget {
  const ReviewOutlinePage({required this.courseId, super.key});

  final String courseId;

  @override
  State<ReviewOutlinePage> createState() => _ReviewOutlinePageState();
}

class _ReviewOutlinePageState extends State<ReviewOutlinePage> {
  bool _loading = true;
  bool _generatingContent = false;
  String? _error;
  Map<String, dynamic> _courseData = {};
  List<Map<String, dynamic>> _units = [];
  String _courseTitle = '';
  String _difficulty = '';

  @override
  void initState() {
    super.initState();
    _loadOutline();
  }

  Future<void> _loadOutline() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final repository = context.read<CourseRepository>();
      final data = await repository.getFullCourse(widget.courseId);

      if (!mounted) return;

      final course = data['course'] as Map<String, dynamic>? ?? data;
      final unitsList = (course['units'] as List?) ?? [];
      final units = unitsList
          .whereType<Map>()
          .map((u) => u.cast<String, dynamic>())
          .toList();

      setState(() {
        _courseData = course;
        _courseTitle = (course['title'] as String?) ?? 'Untitled Course';
        _difficulty = (course['difficulty'] as String?) ?? 'General';
        _units = units;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Failed to load course outline. Please try again.';
      });
    }
  }

  Future<void> _generateContent() async {
    setState(() {
      _generatingContent = true;
      _error = null;
    });

    try {
      final repository = context.read<CourseRepository>();
      await repository.generateContent(widget.courseId);

      if (!mounted) return;
      setState(() => _generatingContent = false);

      context.push('/course-progress/${widget.courseId}');
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _generatingContent = false;
        _error = 'Failed to start content generation.';
      });
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
          'Review Course Outline',
          style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
        actions: [
          TextButton(
            onPressed: _loading ? null : _loadOutline,
            child: Text('Edit',
                style: textTheme.labelLarge?.copyWith(color: AppColors.primary)),
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildError(textTheme)
              : _buildOutline(textTheme),
    );
  }

  Widget _buildError(TextTheme textTheme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.error_outline, size: 48, color: AppColors.textSecondary),
            const SizedBox(height: 16),
            Text(_error!, style: textTheme.bodyLarge?.copyWith(color: AppColors.textSecondary),
                textAlign: TextAlign.center),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: _loadOutline, child: const Text('Retry')),
          ],
        ),
      ),
    );
  }

  Widget _buildOutline(TextTheme textTheme) {
    return Column(
      children: [
        Expanded(
          child: ListView(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 20),
            children: [
              // Course title & difficulty badge
              Text(
                _courseTitle,
                style: textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.accentSoft,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  _difficulty,
                  style: textTheme.labelMedium?.copyWith(
                    color: AppColors.accent,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ).wrapInRow(),
              const SizedBox(height: 20),

              // Units
              for (int i = 0; i < _units.length; i++) _buildUnitTile(i, textTheme),
            ],
          ),
        ),

        // Bottom action button
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          child: ElevatedButton.icon(
            onPressed: _generatingContent ? null : _generateContent,
            icon: _generatingContent
                ? const SizedBox(
                    width: 20, height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : const Icon(Icons.auto_fix_high),
            label: Text(
              _generatingContent ? 'Generating...' : 'Generate Course Content',
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildUnitTile(int index, TextTheme textTheme) {
    final unit = _units[index];
    final title = (unit['title'] as String?) ?? 'Unit ${index + 1}';
    final subtopics = (unit['subtopics'] as List?) ?? [];

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.divider),
      ),
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          initiallyExpanded: index == 0,
          tilePadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          childrenPadding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
          title: Text(
            'Unit ${index + 1}: $title',
            style: textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
          children: [
            for (final sub in subtopics)
              Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.only(top: 6, right: 10),
                      child: Container(
                        width: 6,
                        height: 6,
                        decoration: const BoxDecoration(
                          color: AppColors.primary,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                    Expanded(
                      child: Text(
                        (sub is Map ? sub['title'] as String? : sub?.toString()) ?? '',
                        style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

extension _WidgetX on Widget {
  Widget wrapInRow() {
    return Row(mainAxisSize: MainAxisSize.min, children: [this]);
  }
}
