import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:video_player/video_player.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/explainer_video_cubit.dart';

class ExplainerVideoPage extends StatefulWidget {
  const ExplainerVideoPage({super.key});

  @override
  State<ExplainerVideoPage> createState() => _ExplainerVideoPageState();
}

class _ExplainerVideoPageState extends State<ExplainerVideoPage> {
  final _courseCtrl = TextEditingController();
  VideoPlayerController? _videoController;
  String? _videoUrl;

  Future<void> _setupVideo(String url) async {
    if (_videoUrl == url && _videoController != null) {
      return;
    }
    await _videoController?.dispose();
    final controller = VideoPlayerController.networkUrl(Uri.parse(url));
    await controller.initialize();
    setState(() {
      _videoUrl = url;
      _videoController = controller;
    });
  }

  @override
  void dispose() {
    _courseCtrl.dispose();
    _videoController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(title: const Text('Explainer Video')),
      body: BlocConsumer<ExplainerVideoCubit, ExplainerVideoState>(
        listener: (context, state) {
          if (state.error != null && state.error!.isNotEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(state.error!)));
          }
          final url = state.status?.videoUrl;
          if (url != null && url.isNotEmpty) {
            _setupVideo(url);
          }
        },
        builder: (context, state) {
          final status = state.status;
          return Container(
            color: AppColors.canvas,
            padding: const EdgeInsets.all(16),
            child: ListView(
              children: [
                Text(
                  'Phase 4 — NotebookLM-style pipeline',
                  style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 8),
                Text(
                  'Trigger generation and poll status until completed.',
                  style: textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
                ),
                const SizedBox(height: 14),
                TextField(
                  controller: _courseCtrl,
                  decoration: const InputDecoration(labelText: 'Course ID'),
                ),
                const SizedBox(height: 12),
                ElevatedButton(
                  onPressed: state.loading
                      ? null
                      : () => context.read<ExplainerVideoCubit>().trigger(_courseCtrl.text.trim()),
                  child: const Text('Generate Video'),
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => context.read<ExplainerVideoCubit>().checkOnce(_courseCtrl.text.trim()),
                        child: const Text('Check Progress'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton(
                        onPressed: state.polling
                            ? () => context.read<ExplainerVideoCubit>().stopPolling()
                            : () => context.read<ExplainerVideoCubit>().startPolling(_courseCtrl.text.trim()),
                        child: Text(state.polling ? 'Stop Polling' : 'Start Polling'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                if (status != null) ...[
                  Text('Status: ${status.status}'),
                  if (status.progress != null) ...[
                    const SizedBox(height: 8),
                    LinearProgressIndicator(value: (status.progress!.clamp(0, 100)) / 100),
                    const SizedBox(height: 4),
                    Text('${status.progress}%'),
                  ],
                  if ((status.videoUrl ?? '').isNotEmpty) ...[
                    const SizedBox(height: 10),
                    SelectableText('Video URL: ${status.videoUrl}'),
                    const SizedBox(height: 10),
                    if (_videoController != null && _videoController!.value.isInitialized)
                      AspectRatio(
                        aspectRatio: _videoController!.value.aspectRatio,
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(12),
                          child: VideoPlayer(_videoController!),
                        ),
                      ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        OutlinedButton(
                          onPressed: _videoController == null ? null : () => _videoController!.play(),
                          child: const Text('Play'),
                        ),
                        const SizedBox(width: 8),
                        OutlinedButton(
                          onPressed: _videoController == null ? null : () => _videoController!.pause(),
                          child: const Text('Pause'),
                        ),
                      ],
                    ),
                  ],
                  if ((status.message ?? '').isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text(status.message!),
                  ],
                ],
              ],
            ),
          );
        },
      ),
    );
  }
}
