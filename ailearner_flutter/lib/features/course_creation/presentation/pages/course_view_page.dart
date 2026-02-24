import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../courses/data/course_repository.dart';

class CourseViewPage extends StatefulWidget {
  const CourseViewPage({required this.courseId, super.key});

  final String courseId;

  @override
  State<CourseViewPage> createState() => _CourseViewPageState();
}

class _CourseViewPageState extends State<CourseViewPage> {
  bool _loading = true;
  String? _error;
  String _courseTitle = '';
  String _difficulty = '';
  List<Map<String, dynamic>> _units = [];

  // Currently selected subtopic
  int? _selectedUnitIndex;
  int? _selectedSubtopicIndex;

  @override
  void initState() {
    super.initState();
    _loadCourse();
  }

  Future<void> _loadCourse() async {
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
        _courseTitle = (course['title'] as String?) ?? 'Untitled Course';
        _difficulty = (course['difficulty'] as String?) ?? 'General';
        _units = units;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Failed to load course content.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    // If a subtopic is selected, show its content
    if (_selectedUnitIndex != null && _selectedSubtopicIndex != null) {
      return _buildSubtopicView(textTheme);
    }

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
              const PopupMenuItem(value: 'refresh', child: Text('Refresh')),
            ],
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildErrorView(textTheme)
              : _buildUnitList(textTheme),
    );
  }

  Widget _buildErrorView(TextTheme textTheme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.error_outline, size: 48, color: AppColors.textSecondary),
            const SizedBox(height: 16),
            Text(_error!, style: textTheme.bodyLarge, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: _loadCourse, child: const Text('Retry')),
          ],
        ),
      ),
    );
  }

  Widget _buildUnitList(TextTheme textTheme) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 40),
      children: [
        // Course header
        Text(
          _courseTitle,
          style: textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.w700,
            color: AppColors.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
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
            ),
          ],
        ),
        const SizedBox(height: 24),

        // Units with subtopics
        for (int u = 0; u < _units.length; u++) ...[
          _buildUnitSection(u, textTheme),
          const SizedBox(height: 12),
        ],
      ],
    );
  }

  Widget _buildUnitSection(int unitIndex, TextTheme textTheme) {
    final unit = _units[unitIndex];
    final title = (unit['title'] as String?) ?? 'Unit ${unitIndex + 1}';
    final subtopics = (unit['subtopics'] as List?) ?? [];

    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.divider),
      ),
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          initiallyExpanded: unitIndex == 0,
          tilePadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          childrenPadding: const EdgeInsets.fromLTRB(8, 0, 8, 8),
          title: Text(
            'Unit ${unitIndex + 1}: $title',
            style: textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
          children: [
            for (int s = 0; s < subtopics.length; s++)
              _buildSubtopicTile(unitIndex, s, subtopics[s], textTheme),
          ],
        ),
      ),
    );
  }

  Widget _buildSubtopicTile(
      int unitIndex, int subtopicIndex, dynamic subtopic, TextTheme textTheme) {
    final title = subtopic is Map
        ? (subtopic['title'] as String?) ?? 'Subtopic ${subtopicIndex + 1}'
        : subtopic?.toString() ?? 'Subtopic ${subtopicIndex + 1}';

    final hasContent = subtopic is Map &&
        (subtopic['content'] != null &&
            (subtopic['content'] as String).isNotEmpty);

    return ListTile(
      dense: true,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      leading: Container(
        width: 32,
        height: 32,
        decoration: BoxDecoration(
          color: hasContent ? AppColors.primaryMuted : AppColors.softPanel,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Center(
          child: Icon(
            hasContent ? Icons.check_circle : Icons.circle_outlined,
            size: 18,
            color: hasContent ? AppColors.primary : AppColors.textSecondary,
          ),
        ),
      ),
      title: Text(
        title,
        style: textTheme.bodyMedium?.copyWith(
          color: AppColors.textPrimary,
          fontWeight: FontWeight.w500,
        ),
      ),
      trailing: hasContent
          ? const Icon(Icons.chevron_right, color: AppColors.textSecondary)
          : null,
      onTap: hasContent
          ? () {
              setState(() {
                _selectedUnitIndex = unitIndex;
                _selectedSubtopicIndex = subtopicIndex;
              });
            }
          : null,
    );
  }

  Widget _buildSubtopicView(TextTheme textTheme) {
    final unit = _units[_selectedUnitIndex!];
    final subtopics = (unit['subtopics'] as List?) ?? [];
    final subtopic = subtopics[_selectedSubtopicIndex!];

    final title = subtopic is Map
        ? (subtopic['title'] as String?) ?? 'Subtopic'
        : subtopic?.toString() ?? 'Subtopic';
    final content = subtopic is Map
        ? (subtopic['content'] as String?) ?? 'No content available.'
        : 'No content available.';

    // Estimate reading time (~200 wpm)
    final wordCount = content.split(RegExp(r'\s+')).length;
    final readingMinutes = (wordCount / 200).ceil();

    return Scaffold(
      backgroundColor: AppColors.canvas,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            setState(() {
              _selectedUnitIndex = null;
              _selectedSubtopicIndex = null;
            });
          },
        ),
        title: Text(
          title,
          style: textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        actions: [
          PopupMenuButton<String>(
            onSelected: (_) {},
            itemBuilder: (context) => [
              const PopupMenuItem(value: 'bookmark', child: Text('Bookmark')),
            ],
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 40),
        children: [
          // Title
          Text(
            title,
            style: textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Icon(Icons.schedule, size: 16, color: AppColors.textSecondary),
              const SizedBox(width: 4),
              Text(
                '$readingMinutes min read',
                style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
              ),
            ],
          ),
          const SizedBox(height: 20),
          const Divider(),
          const SizedBox(height: 16),

          // Content — render simple markdown-like formatting
          _RichContentWidget(content: content),
        ],
      ),
    );
  }
}

/// Simple widget that renders course content with basic formatting.
class _RichContentWidget extends StatelessWidget {
  const _RichContentWidget({required this.content});

  final String content;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final lines = content.split('\n');
    final widgets = <Widget>[];

    for (final line in lines) {
      final trimmed = line.trim();

      if (trimmed.isEmpty) {
        widgets.add(const SizedBox(height: 8));
        continue;
      }

      // Headings
      if (trimmed.startsWith('### ')) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(top: 16, bottom: 4),
          child: Text(
            trimmed.substring(4),
            style: textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
        ));
      } else if (trimmed.startsWith('## ')) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(top: 20, bottom: 6),
          child: Text(
            trimmed.substring(3),
            style: textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
        ));
      } else if (trimmed.startsWith('# ')) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(top: 20, bottom: 8),
          child: Text(
            trimmed.substring(2),
            style: textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
        ));
      }
      // Bullet points
      else if (trimmed.startsWith('- ') || trimmed.startsWith('• ') || trimmed.startsWith('* ')) {
        final bullet = trimmed.substring(2);
        widgets.add(Padding(
          padding: const EdgeInsets.only(left: 8, bottom: 4),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.only(top: 8, right: 8),
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
                  _cleanBold(bullet),
                  style: textTheme.bodyMedium?.copyWith(
                    color: AppColors.textSecondary,
                    height: 1.5,
                  ),
                ),
              ),
            ],
          ),
        ));
      }
      // Bold section headers like **Why this matters:**
      else if (trimmed.startsWith('**') && trimmed.endsWith('**')) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(top: 14, bottom: 4),
          child: Text(
            trimmed.replaceAll('**', ''),
            style: textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
        ));
      }
      // Regular paragraph
      else {
        widgets.add(Padding(
          padding: const EdgeInsets.only(bottom: 6),
          child: Text(
            _cleanBold(trimmed),
            style: textTheme.bodyMedium?.copyWith(
              color: AppColors.textSecondary,
              height: 1.6,
            ),
          ),
        ));
      }
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: widgets,
    );
  }

  String _cleanBold(String text) {
    // Simple cleaning of markdown bold markers for display
    return text.replaceAll('**', '');
  }
}
