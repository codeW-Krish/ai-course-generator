import 'package:dio/dio.dart';

import '../storage/token_storage.dart';
import 'api_endpoints.dart';

class ApiClient {
  ApiClient({required TokenStorage tokenStorage})
      : _tokenStorage = tokenStorage,
        _dio = Dio(
          BaseOptions(
            baseUrl: ApiEndpoints.baseUrl,
            connectTimeout: const Duration(seconds: 30),
            receiveTimeout: const Duration(seconds: 120),
            sendTimeout: const Duration(seconds: 30),
            headers: {'Content-Type': 'application/json'},
          ),
        ) {
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          if (options.extra['skipAuth'] == true || _isAuthPath(options.path)) {
            handler.next(options);
            return;
          }

          if (options.headers['Authorization'] != null) {
            handler.next(options);
            return;
          }

          final token = await _tokenStorage.getAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          handler.next(options);
        },
        onError: (error, handler) async {
          final status = error.response?.statusCode;
          final request = error.requestOptions;
            final isAuthEndpoint = _isAuthPath(request.path);

          if (status == 401 && !isAuthEndpoint && request.extra['retried'] != true) {
            final refreshed = await _tryRefreshTokens();
            if (refreshed != null) {
              request.headers['Authorization'] = 'Bearer ${refreshed.accessToken}';
              request.extra['retried'] = true;

              final response = await _dio.fetch<dynamic>(request);
              handler.resolve(response);
              return;
            }

            await _tokenStorage.clear();
          } else if (status == 401) {
            await _tokenStorage.clear();
          }

          handler.next(error);
        },
      ),
    );
  }

  final Dio _dio;
  final TokenStorage _tokenStorage;

  Dio get dio => _dio;

  Future<_TokenPair?> _tryRefreshTokens() async {
    final refreshToken = await _tokenStorage.getRefreshToken();
    if (refreshToken == null || refreshToken.isEmpty) {
      return null;
    }

    try {
      final response = await _dio.post<Map<String, dynamic>>(
        ApiEndpoints.refresh,
        data: {
          'refreshToken': refreshToken,
          'refresh_token': refreshToken,
          'token': refreshToken,
        },
        options: Options(
          headers: {'Authorization': 'Bearer $refreshToken'},
          extra: {'skipAuth': true},
        ),
      );

      final payload = response.data ?? <String, dynamic>{};
      final accessToken =
          _findStringByKey(payload, const ['accessToken', 'access_token', 'token', 'jwt']) ??
          _findStringByKey(payload['data'], const ['accessToken', 'access_token', 'token', 'jwt']) ??
          _findStringByKey(payload['tokens'], const ['accessToken', 'access_token', 'token', 'jwt']);

      if (accessToken == null || accessToken.isEmpty) {
        return null;
      }

      final nextRefreshToken =
          _findStringByKey(payload, const ['refreshToken', 'refresh_token', 'refresh']) ??
          _findStringByKey(payload['data'], const ['refreshToken', 'refresh_token', 'refresh']) ??
          _findStringByKey(payload['tokens'], const ['refreshToken', 'refresh_token', 'refresh']) ??
          refreshToken;

      await _tokenStorage.saveTokens(
        accessToken: accessToken,
        refreshToken: nextRefreshToken,
      );

      return _TokenPair(accessToken: accessToken, refreshToken: nextRefreshToken);
    } catch (_) {
      return null;
    }
  }

  String? _findStringByKey(dynamic source, List<String> keys) {
    if (source is! Map) {
      return null;
    }

    for (final key in keys) {
      final value = source[key];
      if (value is String && value.isNotEmpty) {
        return value;
      }
    }

    return null;
  }

  bool _isAuthPath(String path) {
    final normalized = path.split('?').first;
    return normalized.endsWith(ApiEndpoints.login) ||
        normalized.endsWith(ApiEndpoints.register) ||
        normalized.endsWith(ApiEndpoints.refresh);
  }
}

class _TokenPair {
  const _TokenPair({required this.accessToken, required this.refreshToken});

  final String accessToken;
  final String refreshToken;
}
