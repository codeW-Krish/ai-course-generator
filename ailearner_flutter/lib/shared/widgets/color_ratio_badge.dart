import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class ColorRatioBadge extends StatelessWidget {
  const ColorRatioBadge({
    required this.screenName,
    super.key,
  });

  final String screenName;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: Border.all(color: AppColors.divider),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _dot(AppColors.canvas),
          _dot(AppColors.primary),
          _dot(AppColors.accent),
          const SizedBox(width: 8),
          Text(
            '$screenName · 60/30/10',
            style: textTheme.labelMedium?.copyWith(
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  Widget _dot(Color color) {
    return Container(
      margin: const EdgeInsets.only(right: 4),
      width: 10,
      height: 10,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.divider),
      ),
    );
  }
}
