import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/mindmap_cubit.dart';

class MindMapPage extends StatefulWidget {
  const MindMapPage({super.key, this.initialCourseId});

  final String? initialCourseId;

  @override
  State<MindMapPage> createState() => _MindMapPageState();
}

class _MindMapPageState extends State<MindMapPage> {
  late final TextEditingController _courseCtrl;

  @override
  void initState() {
    super.initState();
    _courseCtrl = TextEditingController(text: widget.initialCourseId ?? '');
    if ((widget.initialCourseId ?? '').isNotEmpty) {
      final cubit = context.read<MindMapCubit>();
      Future.microtask(() => cubit.loadCourse(widget.initialCourseId!));
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
      appBar: AppBar(title: const Text('Mind Map')),
      body: BlocConsumer<MindMapCubit, MindMapState>(
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
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _courseCtrl,
                        decoration: const InputDecoration(labelText: 'Course ID for mind map'),
                      ),
                    ),
                    const SizedBox(width: 10),
                    SizedBox(
                      width: 110,
                      child: ElevatedButton(
                        onPressed: state.loading
                            ? null
                            : () => context.read<MindMapCubit>().loadCourse(_courseCtrl.text.trim()),
                        child: const Text('Load'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                Expanded(
                  child: state.loading
                      ? const Center(child: CircularProgressIndicator())
                      : state.nodes.isEmpty
                          ? Center(
                              child: Text(
                                'No map data yet. Load a course.',
                                style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                              ),
                            )
                          : ListView.builder(
                              itemCount: state.nodes.length,
                              itemBuilder: (context, index) {
                                final unit = state.nodes[index];
                                return Card(
                                  child: ExpansionTile(
                                    title: Text(unit.title),
                                    children: unit.children
                                        .map((sub) => ListTile(
                                              title: Text(sub.title),
                                              leading: const Icon(Icons.adjust_rounded, color: AppColors.accent),
                                            ))
                                        .toList(),
                                  ),
                                );
                              },
                            ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
