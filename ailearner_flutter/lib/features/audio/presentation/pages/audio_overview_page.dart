import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:just_audio/just_audio.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/audio_cubit.dart';

class AudioOverviewPage extends StatefulWidget {
  const AudioOverviewPage({super.key});

  @override
  State<AudioOverviewPage> createState() => _AudioOverviewPageState();
}

class _AudioOverviewPageState extends State<AudioOverviewPage> {
  final _idCtrl = TextEditingController();
  final AudioPlayer _player = AudioPlayer();
  bool _courseMode = false;

  @override
  void dispose() {
    _idCtrl.dispose();
    _player.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(title: const Text('Audio Overview')),
      body: BlocConsumer<AudioCubit, AudioState>(
        listener: (context, state) {
          if (state.error != null && state.error!.isNotEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
          }
        },
        builder: (context, state) {
          final payload = state.payload;
          return Container(
            color: AppColors.canvas,
            padding: const EdgeInsets.all(16),
            child: ListView(
              children: [
                Text(
                  'Phase 3 — Course and subtopic narration',
                  style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 8),
                Text(
                  'Connected to /api/audio/:subtopicId and /api/audio/course/:courseId',
                  style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                ),
                const SizedBox(height: 14),
                SwitchListTile(
                  value: _courseMode,
                  onChanged: (v) => setState(() => _courseMode = v),
                  title: Text(_courseMode ? 'Course audio mode' : 'Subtopic audio mode'),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _idCtrl,
                  decoration: InputDecoration(
                    labelText: _courseMode ? 'Course ID' : 'Subtopic ID',
                  ),
                ),
                const SizedBox(height: 10),
                ElevatedButton(
                  onPressed: state.loading
                      ? null
                      : () {
                          final id = _idCtrl.text.trim();
                          if (id.isEmpty) return;
                          if (_courseMode) {
                            context.read<AudioCubit>().loadCourse(id);
                          } else {
                            context.read<AudioCubit>().loadSubtopic(id);
                          }
                        },
                  child: state.loading
                      ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                      : const Text('Generate / Load Audio'),
                ),
                if (payload != null) ...[
                  const SizedBox(height: 12),
                  Text('Type: ${payload.type} • Est. ${payload.durationSec}s'),
                  const SizedBox(height: 8),
                  OutlinedButton(
                    onPressed: () async {
                      await _player.setUrl(payload.audioUrl);
                      await _player.play();
                    },
                    child: const Text('Play'),
                  ),
                  const SizedBox(height: 8),
                  OutlinedButton(
                    onPressed: () => _player.pause(),
                    child: const Text('Pause'),
                  ),
                  const SizedBox(height: 10),
                  Text(
                    payload.script,
                    style: textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
                  ),
                ],
              ],
            ),
          );
        },
      ),
    );
  }
}
