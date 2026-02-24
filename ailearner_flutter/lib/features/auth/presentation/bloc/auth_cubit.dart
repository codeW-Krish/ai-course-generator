import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/auth_models.dart';
import '../../data/auth_repository.dart';

class AuthState extends Equatable {
  const AuthState({
    this.loading = false,
    this.user,
    this.error,
    this.isAuthenticated = false,
  });

  final bool loading;
  final AppUser? user;
  final String? error;
  final bool isAuthenticated;

  AuthState copyWith({
    bool? loading,
    AppUser? user,
    String? error,
    bool? isAuthenticated,
  }) {
    return AuthState(
      loading: loading ?? this.loading,
      user: user ?? this.user,
      error: error,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
    );
  }

  @override
  List<Object?> get props => [loading, user, error, isAuthenticated];
}

class AuthCubit extends Cubit<AuthState> {
  AuthCubit(this._repository) : super(const AuthState());

  final AuthRepository _repository;

  Future<void> bootstrap() async {
    final hasSession = await _repository.hasSession();
    emit(state.copyWith(isAuthenticated: hasSession));
  }

  Future<void> login({required String email, required String password}) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final session = await _repository.login(email: email, password: password);
      emit(state.copyWith(
        loading: false,
        isAuthenticated: true,
        user: session.user,
      ));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> register({required String name, required String email, required String password}) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final session = await _repository.register(name: name, email: email, password: password);
      emit(state.copyWith(
        loading: false,
        isAuthenticated: true,
        user: session.user,
      ));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    emit(const AuthState(isAuthenticated: false));
  }
}
