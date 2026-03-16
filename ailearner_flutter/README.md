# AiLearner Flutter Migration (`ailearner_flutter`)

This project is the new root-level Flutter app for migrating the existing Android/Jetpack Compose app.

## Current scope

- Phase 1 migration scaffold (auth + home UX shell)
- Premium design system with non-generic palette
- 60-30-10 color rule embedded in each implemented screen
- Router-first navigation structure aligned with `migration_strategy.md`

## Run locally

1. Install Flutter SDK and ensure `flutter` is available on PATH.
2. From this folder run:
   - `flutter pub get`
   - `flutter run`

## Implemented structure

- `lib/core/theme` for design tokens and theme
- `lib/core/router` for app routing
- `lib/features/auth/presentation/pages` for auth pages
- `lib/features/home/presentation/pages` for home page
- `lib/features/courses/presentation/widgets` for reusable course UI

## Migration intent

The UI mirrors your current product tone while improving visual polish (hierarchy, spacing, component consistency), avoiding generic color presets.
