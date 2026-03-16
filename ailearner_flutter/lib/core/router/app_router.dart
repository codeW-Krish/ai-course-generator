import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/auth/presentation/pages/signup_page.dart';
import '../../features/auth/presentation/pages/welcome_page.dart';
import '../../features/auth/presentation/pages/profile_page.dart';
import '../../features/audio/presentation/pages/audio_overview_page.dart';
import '../../features/course_creation/presentation/pages/create_course_page.dart';
import '../../features/course_creation/presentation/pages/review_outline_page.dart';
import '../../features/course_creation/presentation/pages/course_progress_page.dart';
import '../../features/course_creation/presentation/pages/course_view_page.dart';
import '../../features/courses/presentation/pages/mind_map_page.dart';
import '../../features/courses/presentation/pages/my_courses_page.dart';
import '../../features/courses/presentation/pages/enrolled_courses_page.dart';
import '../../features/explainer_video/presentation/pages/explainer_video_page.dart';
import '../../features/gamification/presentation/pages/gamification_page.dart';
import '../../features/home/presentation/pages/home_page.dart';
import '../../features/interactive/presentation/pages/due_flashcards_page.dart';
import '../../features/interactive/presentation/pages/interactive_hub_page.dart';
import '../../features/analytics/presentation/pages/progress_analytics_page.dart';

final GoRouter appRouter = GoRouter(
  initialLocation: '/welcome',
  routes: [
    GoRoute(
      path: '/welcome',
      builder: (context, state) => const WelcomePage(),
    ),
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginPage(),
    ),
    GoRoute(
      path: '/signup',
      builder: (context, state) => const SignUpPage(),
    ),
    GoRoute(
      path: '/home',
      builder: (context, state) => const HomePage(),
    ),
    GoRoute(
      path: '/my-courses',
      builder: (context, state) => const MyCoursesPage(),
    ),
    GoRoute(
      path: '/enrolled',
      builder: (context, state) => const EnrolledCoursesPage(),
    ),
    GoRoute(
      path: '/profile',
      builder: (context, state) => const ProfilePage(),
    ),
    GoRoute(
      path: '/create-course',
      builder: (context, state) => const CreateCoursePage(),
    ),
    GoRoute(
      path: '/review-outline/:courseId',
      builder: (context, state) => ReviewOutlinePage(
        courseId: state.pathParameters['courseId']!,
      ),
    ),
    GoRoute(
      path: '/course-progress/:courseId',
      builder: (context, state) => CourseProgressPage(
        courseId: state.pathParameters['courseId']!,
      ),
    ),
    GoRoute(
      path: '/course-view/:courseId',
      builder: (context, state) => CourseViewPage(
        courseId: state.pathParameters['courseId']!,
      ),
    ),
    GoRoute(
      path: '/interactive-hub',
      builder: (context, state) => const InteractiveHubPage(),
    ),
    GoRoute(
      path: '/audio-overview',
      builder: (context, state) => const AudioOverviewPage(),
    ),
    GoRoute(
      path: '/progress-analytics',
      builder: (context, state) => const ProgressAnalyticsPage(),
    ),
    GoRoute(
      path: '/progress-analytics/:courseId',
      builder: (context, state) => const ProgressAnalyticsPage(),
    ),
    GoRoute(
      path: '/gamification',
      builder: (context, state) => const GamificationPage(),
    ),
    GoRoute(
      path: '/explainer-video',
      builder: (context, state) => const ExplainerVideoPage(),
    ),
    GoRoute(
      path: '/mind-map',
      builder: (context, state) => const MindMapPage(),
    ),
    GoRoute(
      path: '/mind-map/:courseId',
      builder: (context, state) => MindMapPage(initialCourseId: state.pathParameters['courseId']),
    ),
    GoRoute(
      path: '/due-flashcards',
      builder: (context, state) => const DueFlashcardsPage(),
    ),
    GoRoute(
      path: '/due-flashcards/:courseId',
      builder: (context, state) => DueFlashcardsPage(initialCourseId: state.pathParameters['courseId']),
    ),
  ],
);
