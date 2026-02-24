import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'core/router/app_router.dart';
import 'core/network/api_client.dart';
import 'core/storage/token_storage.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/data/auth_repository.dart';
import 'features/auth/presentation/bloc/auth_cubit.dart';
import 'features/analytics/data/analytics_repository.dart';
import 'features/analytics/presentation/bloc/analytics_cubit.dart';
import 'features/audio/data/audio_repository.dart';
import 'features/audio/presentation/bloc/audio_cubit.dart';
import 'features/courses/data/course_repository.dart';
import 'features/courses/presentation/bloc/courses_cubit.dart';
import 'features/courses/presentation/bloc/mindmap_cubit.dart';
import 'features/explainer_video/data/explainer_video_repository.dart';
import 'features/explainer_video/presentation/bloc/explainer_video_cubit.dart';
import 'features/gamification/data/gamification_repository.dart';
import 'features/gamification/presentation/bloc/gamification_cubit.dart';
import 'features/interactive/data/interactive_repository.dart';
import 'features/interactive/presentation/bloc/due_flashcards_cubit.dart';
import 'features/interactive/presentation/bloc/interactive_cubit.dart';

class AiLearnerApp extends StatelessWidget {
  const AiLearnerApp({super.key});

  @override
  Widget build(BuildContext context) {
    final tokenStorage = TokenStorage();
    final apiClient = ApiClient(tokenStorage: tokenStorage);

    return MultiRepositoryProvider(
      providers: [
        RepositoryProvider<TokenStorage>.value(value: tokenStorage),
        RepositoryProvider<ApiClient>.value(value: apiClient),
        RepositoryProvider<AuthRepository>(
          create: (_) => AuthRepository(
            apiClient: apiClient,
            tokenStorage: tokenStorage,
          ),
        ),
        RepositoryProvider<CourseRepository>(
          create: (_) => CourseRepository(apiClient),
        ),
        RepositoryProvider<InteractiveRepository>(
          create: (_) => InteractiveRepository(apiClient),
        ),
        RepositoryProvider<AudioRepository>(
          create: (_) => AudioRepository(apiClient),
        ),
        RepositoryProvider<ExplainerVideoRepository>(
          create: (_) => ExplainerVideoRepository(apiClient),
        ),
        RepositoryProvider<GamificationRepository>(
          create: (_) => GamificationRepository(apiClient),
        ),
        RepositoryProvider<AnalyticsRepository>(
          create: (context) => AnalyticsRepository(
            context.read<ApiClient>(),
            context.read<CourseRepository>(),
          ),
        ),
      ],
      child: MultiBlocProvider(
        providers: [
          BlocProvider<AuthCubit>(
            create: (context) => AuthCubit(context.read<AuthRepository>())..bootstrap(),
          ),
          BlocProvider<CoursesCubit>(
            create: (context) => CoursesCubit(context.read<CourseRepository>()),
          ),
          BlocProvider<InteractiveCubit>(
            create: (context) => InteractiveCubit(
              context.read<InteractiveRepository>(),
              context.read<GamificationRepository>(),
            ),
          ),
          BlocProvider<AudioCubit>(
            create: (context) => AudioCubit(context.read<AudioRepository>()),
          ),
          BlocProvider<MindMapCubit>(
            create: (context) => MindMapCubit(context.read<CourseRepository>()),
          ),
          BlocProvider<DueFlashcardsCubit>(
            create: (context) => DueFlashcardsCubit(
              context.read<InteractiveRepository>(),
              context.read<GamificationRepository>(),
            ),
          ),
          BlocProvider<ExplainerVideoCubit>(
            create: (context) => ExplainerVideoCubit(context.read<ExplainerVideoRepository>()),
          ),
          BlocProvider<AnalyticsCubit>(
            create: (context) => AnalyticsCubit(context.read<AnalyticsRepository>()),
          ),
          BlocProvider<GamificationCubit>(
            create: (context) => GamificationCubit(context.read<GamificationRepository>()),
          ),
        ],
        child: MaterialApp.router(
          title: 'AiLearner',
          debugShowCheckedModeBanner: false,
          routerConfig: appRouter,
          theme: AppTheme.light,
        ),
      ),
    );
  }
}
