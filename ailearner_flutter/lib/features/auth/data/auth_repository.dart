import '../../../core/network/api_client.dart';
import '../../../core/network/api_endpoints.dart';
import '../../../core/storage/token_storage.dart';
import 'auth_models.dart';

class AuthRepository {
  AuthRepository({
    required ApiClient apiClient,
    required TokenStorage tokenStorage,
  })  : _apiClient = apiClient,
        _tokenStorage = tokenStorage;

  final ApiClient _apiClient;
  final TokenStorage _tokenStorage;

  Future<AuthSession> login({required String email, required String password}) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      ApiEndpoints.login,
      data: {'email': email, 'password': password},
    );
    final session = AuthSession.fromJson(response.data ?? <String, dynamic>{});
    final accessToken = session.accessToken;
    if (accessToken == null || accessToken.isEmpty) {
      await _tokenStorage.clear();
      throw StateError('Login succeeded but no access token was returned by the server.');
    }

    await _tokenStorage.saveTokens(
      accessToken: accessToken,
      refreshToken: session.refreshToken ?? accessToken,
    );
    return session;
  }

  Future<AuthSession> register({
    required String name,
    required String email,
    required String password,
  }) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      ApiEndpoints.register,
      data: {'username': name, 'email': email, 'password': password},
    );
    final session = AuthSession.fromJson(response.data ?? <String, dynamic>{});
    final accessToken = session.accessToken;
    if (accessToken == null || accessToken.isEmpty) {
      await _tokenStorage.clear();
      throw StateError('Registration succeeded but no access token was returned by the server.');
    }

    await _tokenStorage.saveTokens(
      accessToken: accessToken,
      refreshToken: session.refreshToken ?? accessToken,
    );
    return session;
  }

  Future<void> logout() => _tokenStorage.clear();

  Future<bool> hasSession() async {
    final access = await _tokenStorage.getAccessToken();
    return access?.isNotEmpty ?? false;
  }
}
