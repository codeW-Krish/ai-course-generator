import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/analytics_cubit.dart';

class ProgressAnalyticsPage extends StatefulWidget {
  const ProgressAnalyticsPage({super.key});

  @override
  State<ProgressAnalyticsPage> createState() => _ProgressAnalyticsPageState();
}

class _ProgressAnalyticsPageState extends State<ProgressAnalyticsPage> {
  final _courseCtrl = TextEditingController();

  @override
  void dispose() {
    _courseCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(title: const Text('Progress Analytics')),
      body: BlocConsumer<AnalyticsCubit, AnalyticsState>(
        listener: (context, state) {
          if (state.error != null && state.error!.isNotEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
          }
        },
        builder: (context, state) {
          final summary = state.summary;
          return Container(
            color: AppColors.canvas,
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Course Progress Dashboard', style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _courseCtrl,
                        decoration: const InputDecoration(labelText: 'Course ID'),
                      ),
                    ),
                    const SizedBox(width: 10),
                    ElevatedButton(
                      onPressed: state.loading
                          ? null
                          : () => context.read<AnalyticsCubit>().load(_courseCtrl.text.trim()),
                      child: const Text('Load'),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                if (summary != null) ...[
                  Text('Units: ${summary.units}'),
                  Text('Completed: ${summary.completedSubtopics}/${summary.totalSubtopics}'),
                  Text('Progress: ${summary.completionPercent.toStringAsFixed(1)}%'),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(value: summary.completionPercent / 100),
                ],
                const SizedBox(height: 12),
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.divider),
                    ),
                    padding: const EdgeInsets.all(16),
                    child: state.loading
                        ? const Center(child: CircularProgressIndicator())
                        : BarChart(
                            BarChartData(
                              barGroups: [
                                for (var i = 0; i < (summary?.unitCompletion.length ?? 0); i++)
                                  BarChartGroupData(
                                    x: i + 1,
                                    barRods: [
                                      BarChartRodData(
                                        toY: ((summary?.unitCompletion[i] ?? 0) * 100),
                                        color: i.isEven ? AppColors.primary : AppColors.accent,
                                      ),
                                    ],
                                  ),
                              ],
                              maxY: 100,
                            ),
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
}
