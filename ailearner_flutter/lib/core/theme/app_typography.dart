import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTypography {
  const AppTypography._();

  static TextTheme textTheme() {
    final base = GoogleFonts.interTextTheme();
    return base.copyWith(
      headlineLarge: base.headlineLarge?.copyWith(
        fontWeight: FontWeight.w800,
        letterSpacing: -0.8,
      ),
      headlineMedium: base.headlineMedium?.copyWith(
        fontWeight: FontWeight.w700,
        letterSpacing: -0.4,
      ),
      titleLarge: base.titleLarge?.copyWith(fontWeight: FontWeight.w700),
      titleMedium: base.titleMedium?.copyWith(fontWeight: FontWeight.w600),
      bodyLarge: base.bodyLarge?.copyWith(height: 1.35),
      bodyMedium: base.bodyMedium?.copyWith(height: 1.3),
      labelLarge: base.labelLarge?.copyWith(fontWeight: FontWeight.w700),
    );
  }
}
