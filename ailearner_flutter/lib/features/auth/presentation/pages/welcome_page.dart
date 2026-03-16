import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../shared/widgets/color_ratio_badge.dart';

class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      body: Column(
        children: [
          Expanded(
            flex: 6,
            child: Container(
              width: double.infinity,
              color: AppColors.canvas,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    width: 88,
                    height: 88,
                    decoration: BoxDecoration(
                      color: AppColors.primaryMuted,
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: const Icon(Icons.school_rounded, color: AppColors.primary, size: 42),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'AiLearner',
                    style: textTheme.headlineSmall?.copyWith(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 10),
                  const ColorRatioBadge(screenName: 'Welcome'),
                ],
              ),
            ),
          ),
          Expanded(
            flex: 4,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(24, 28, 24, 24),
              decoration: const BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.vertical(top: Radius.circular(30)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Learn Anything.\\nFaster. Smarter.',
                    style: textTheme.headlineMedium?.copyWith(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'AI-powered course generation and interactive learning in one premium workspace.',
                    style: textTheme.bodyLarge?.copyWith(color: AppColors.textSecondary),
                  ),
                  const Spacer(),
                  ElevatedButton(
                    onPressed: () => context.push('/signup'),
                    child: const Text('Get Started'),
                  ),
                  const SizedBox(height: 12),
                  OutlinedButton(
                    onPressed: () => context.push('/login'),
                    child: const Text('I Have an Account'),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
