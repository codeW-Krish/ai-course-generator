import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../auth/presentation/bloc/auth_cubit.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

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
          'Profile',
          style: textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          // Avatar
          Center(
            child: CircleAvatar(
              radius: 48,
              backgroundColor: AppColors.primaryMuted,
              child: const Icon(Icons.person, size: 48, color: AppColors.primary),
            ),
          ),
          const SizedBox(height: 24),

          // Settings section
          Container(
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.divider),
            ),
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.analytics_outlined, color: AppColors.primary),
                  title: const Text('Progress Analytics'),
                  trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  onTap: () => context.push('/progress-analytics'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.videocam_outlined, color: AppColors.primary),
                  title: const Text('Explainer Videos'),
                  trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                  onTap: () => context.push('/explainer-video'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.headphones_outlined, color: AppColors.primary),
                  title: const Text('Audio Overview'),
                  trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                  onTap: () => context.push('/audio-overview'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.emoji_events_outlined, color: AppColors.primary),
                  title: const Text('Gamification'),
                  trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                  onTap: () => context.push('/gamification'),
                ),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // Logout
          OutlinedButton.icon(
            onPressed: () {
              context.read<AuthCubit>().logout();
              context.go('/welcome');
            },
            icon: const Icon(Icons.logout, color: Color(0xFFB42318)),
            label: Text(
              'Log Out',
              style: textTheme.labelLarge?.copyWith(color: const Color(0xFFB42318)),
            ),
            style: OutlinedButton.styleFrom(
              side: const BorderSide(color: Color(0xFFB42318)),
              minimumSize: const Size(double.infinity, 52),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
