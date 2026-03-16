import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/app_colors.dart';
import '../bloc/auth_cubit.dart';
import '../../../../shared/widgets/color_ratio_badge.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _obscure = true;

  @override
  void dispose() {
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(),
      body: Container(
        color: AppColors.canvas,
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 460),
              child: ListView(
                padding: const EdgeInsets.fromLTRB(24, 24, 24, 30),
                children: [
                  BlocListener<AuthCubit, AuthState>(
                    listener: (context, state) {
                      if (state.isAuthenticated) {
                        context.go('/home');
                      }
                      if (state.error != null && state.error!.isNotEmpty) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(state.error!)),
                        );
                      }
                    },
                    child: const SizedBox.shrink(),
                  ),
                  Text(
                    'Welcome Back',
                    style: textTheme.headlineMedium?.copyWith(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Log in to continue your learning journey.',
                    style: textTheme.bodyLarge?.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 12),
                  const ColorRatioBadge(screenName: 'Login'),
                  const SizedBox(height: 20),
                  Container(
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.divider),
                    ),
                    child: Column(
                      children: [
                        TextField(
                          controller: _emailCtrl,
                          keyboardType: TextInputType.emailAddress,
                          decoration: const InputDecoration(labelText: 'Email'),
                        ),
                        const SizedBox(height: 14),
                        TextField(
                          controller: _passwordCtrl,
                          obscureText: _obscure,
                          decoration: InputDecoration(
                            labelText: 'Password',
                            suffixIcon: IconButton(
                              onPressed: () => setState(() => _obscure = !_obscure),
                              icon: Icon(_obscure ? Icons.visibility_off : Icons.visibility),
                            ),
                          ),
                        ),
                        const SizedBox(height: 18),
                        BlocBuilder<AuthCubit, AuthState>(
                          builder: (context, state) {
                            return ElevatedButton(
                              onPressed: state.loading
                                  ? null
                                  : () => context.read<AuthCubit>().login(
                                        email: _emailCtrl.text.trim(),
                                        password: _passwordCtrl.text,
                                      ),
                              child: state.loading
                                  ? const SizedBox(
                                      width: 20,
                                      height: 20,
                                      child: CircularProgressIndicator(strokeWidth: 2),
                                    )
                                  : const Text('Log In'),
                            );
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextButton(
                    onPressed: () => context.push('/signup'),
                    child: const Text('Don\'t have an account? Sign Up'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
