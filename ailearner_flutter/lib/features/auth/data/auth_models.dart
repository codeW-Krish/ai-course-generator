class AppUser {
  const AppUser({
    required this.id,
    required this.email,
    required this.username,
    required this.role,
  });

  final String id;
  final String email;
  final String username;
  final String role;

  factory AppUser.fromJson(Map<String, dynamic> json) {
    return AppUser(
      id: json['id'] as String? ?? '',
      email: json['email'] as String? ?? '',
      username: json['username'] as String? ?? '',
      role: json['role'] as String? ?? 'user',
    );
  }
}

class AuthSession {
  const AuthSession({
    required this.user,
    this.accessToken,
    this.refreshToken,
  });

  final AppUser user;
  final String? accessToken;
  final String? refreshToken;

  factory AuthSession.fromJson(Map<String, dynamic> json) {
    final data = _readMap(json['data']) ?? json;
    final userMap = _readMap(data['user']) ?? _readMap(json['user']) ?? <String, dynamic>{};
    return AuthSession(
      user: AppUser.fromJson(userMap),
      accessToken: _readToken(data) ?? _readToken(json),
      refreshToken: _readRefreshToken(data) ?? _readRefreshToken(json),
    );
  }

  static Map<String, dynamic>? _readMap(dynamic value) {
    if (value is Map<String, dynamic>) {
      return value;
    }
    if (value is Map) {
      return value.cast<String, dynamic>();
    }
    return null;
  }

  static String? _readToken(Map<String, dynamic> source) {
    final tokens = _readMap(source['tokens']);
    return (source['accessToken'] as String?) ??
      (source['access_token'] as String?) ??
        (source['token'] as String?) ??
        (source['jwt'] as String?) ??
        (tokens?['accessToken'] as String?) ??
      (tokens?['access_token'] as String?) ??
        (tokens?['token'] as String?) ??
        (tokens?['jwt'] as String?);
  }

  static String? _readRefreshToken(Map<String, dynamic> source) {
    final tokens = _readMap(source['tokens']);
    return (source['refreshToken'] as String?) ??
        (source['refresh_token'] as String?) ??
        (source['refresh'] as String?) ??
        (tokens?['refreshToken'] as String?) ??
        (tokens?['refresh_token'] as String?) ??
        (tokens?['refresh'] as String?);
  }
}
