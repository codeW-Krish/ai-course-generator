import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../courses/data/course_models.dart';
import '../../../courses/data/course_repository.dart';

class CreateCoursePage extends StatefulWidget {
  const CreateCoursePage({super.key});

  @override
  State<CreateCoursePage> createState() => _CreateCoursePageState();
}

class _CreateCoursePageState extends State<CreateCoursePage> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _descriptionCtrl = TextEditingController();

  int _numUnits = 4;
  String _difficulty = 'Beginner';
  bool _includeVideos = false;

  bool _generatingOutline = false;
  bool _generatingContent = false;
  String? _courseId;
  String? _errorMessage;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descriptionCtrl.dispose();
    super.dispose();
  }

  Future<void> _generateOutline() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _generatingOutline = true;
      _errorMessage = null;
    });

    try {
      final repository = context.read<CourseRepository>();
      final courseId = await repository.generateOutline(
        GenerateOutlineRequest(
          title: _titleCtrl.text.trim(),
          description: _descriptionCtrl.text.trim(),
          numUnits: _numUnits,
          difficulty: _difficulty,
          includeVideos: _includeVideos,
        ),
      );

      if (!mounted) return;

      setState(() {
        _courseId = courseId;
        _generatingOutline = false;
      });

      if (courseId.isNotEmpty) {
        context.push('/review-outline/$courseId');
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _generatingOutline = false;
        _errorMessage = 'Failed to generate outline. Please try again.';
      });
    }
  }

  Future<void> _generateContent() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _generatingContent = true;
      _errorMessage = null;
    });

    try {
      final repository = context.read<CourseRepository>();
      String courseId = _courseId ?? '';

      if (courseId.isEmpty) {
        courseId = await repository.generateOutline(
          GenerateOutlineRequest(
            title: _titleCtrl.text.trim(),
            description: _descriptionCtrl.text.trim(),
            numUnits: _numUnits,
            difficulty: _difficulty,
            includeVideos: _includeVideos,
          ),
        );
        if (!mounted) return;
        setState(() => _courseId = courseId);
      }

      if (courseId.isEmpty) {
        setState(() {
          _generatingContent = false;
          _errorMessage = 'Failed to create course.';
        });
        return;
      }

      await repository.generateContent(courseId);

      if (!mounted) return;
      setState(() => _generatingContent = false);

      context.push('/course-progress/$courseId');
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _generatingContent = false;
        _errorMessage = 'Failed to generate content. Please try again.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final isLoading = _generatingOutline || _generatingContent;

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        title: Text(
          'Create Course',
          style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
        actions: [
          PopupMenuButton<String>(
            onSelected: (_) {},
            itemBuilder: (context) => [
              const PopupMenuItem(value: 'help', child: Text('Help')),
            ],
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 40),
          children: [
            // ── Course Title ──
            TextFormField(
              controller: _titleCtrl,
              decoration: const InputDecoration(
                labelText: 'Course Title',
                hintText: 'Enter your course title',
              ),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Please enter a course title' : null,
            ),
            const SizedBox(height: 6),
            Text(
              'Give your course a clear and descriptive title',
              style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 20),

            // ── Brief Description ──
            TextFormField(
              controller: _descriptionCtrl,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Brief Description',
                hintText: 'Describe what your course will cover...',
                alignLabelWithHint: true,
              ),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Please enter a description' : null,
            ),
            const SizedBox(height: 6),
            Text(
              'Provide a brief overview of the course content and objectives',
              style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 20),

            // ── Number of Units ──
            DropdownButtonFormField<int>(
              value: _numUnits,
              decoration: const InputDecoration(labelText: 'Number of Units'),
              items: List.generate(8, (i) => i + 1)
                  .map((n) => DropdownMenuItem(
                        value: n,
                        child: Text('$n Unit${n == 1 ? '' : 's'}'),
                      ))
                  .toList(),
              onChanged: isLoading ? null : (v) {
                if (v != null) setState(() => _numUnits = v);
              },
            ),
            const SizedBox(height: 6),
            Text(
              'Choose how many units your course should have',
              style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 20),

            // ── Difficulty Level ──
            DropdownButtonFormField<String>(
              value: _difficulty,
              decoration: const InputDecoration(labelText: 'Difficulty Level'),
              items: const [
                DropdownMenuItem(value: 'Beginner', child: Text('Beginner')),
                DropdownMenuItem(value: 'Intermediate', child: Text('Intermediate')),
                DropdownMenuItem(value: 'Advanced', child: Text('Advanced')),
              ],
              onChanged: isLoading ? null : (v) {
                if (v != null) setState(() => _difficulty = v);
              },
            ),
            const SizedBox(height: 6),
            Text(
              'Set the appropriate difficulty level for your target audience',
              style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 20),

            // ── YouTube Videos toggle ──
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.divider),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Include YouTube Videos?',
                      style: textTheme.bodyLarge?.copyWith(color: AppColors.textPrimary)),
                  Switch(
                    value: _includeVideos,
                    onChanged: isLoading ? null : (v) => setState(() => _includeVideos = v),
                    activeColor: AppColors.primary,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),

            // ── Error ──
            if (_errorMessage != null) ...[
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFFEE2E2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline, color: Color(0xFFB42318), size: 20),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(_errorMessage!,
                          style: textTheme.bodyMedium?.copyWith(color: const Color(0xFFB42318))),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
            ],

            // ── Generate Outline ──
            ElevatedButton.icon(
              onPressed: isLoading ? null : _generateOutline,
              icon: _generatingOutline
                  ? const SizedBox(
                      width: 20, height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Icon(Icons.auto_fix_high),
              label: Text(
                _generatingOutline ? 'Generating Outline...' : 'Generate Outline',
                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
            ),
            const SizedBox(height: 12),

            // ── Generate Course Content ──
            ElevatedButton.icon(
              onPressed: isLoading ? null : _generateContent,
              icon: _generatingContent
                  ? const SizedBox(
                      width: 20, height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Icon(Icons.auto_fix_high),
              label: Text(
                _generatingContent ? 'Generating Content...' : 'Generate Course Content',
                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
