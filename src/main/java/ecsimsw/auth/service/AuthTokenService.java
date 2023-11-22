package ecsimsw.auth.service;

import ecsimsw.auth.anotations.TokenKey;
import ecsimsw.auth.domain.AuthTokens;
import ecsimsw.auth.domain.AuthTokensCacheRepository;
import ecsimsw.auth.domain.TokenCookieHolder;
import ecsimsw.auth.exception.AuthenticateFailedException;
import ecsimsw.auth.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AuthTokenService<T> {

    private final Logger LOGGER = LoggerFactory.getLogger(AuthTokenService.class);

    private final Key jwtSecretKey;
    private final Class<T> payloadType;
    private final AuthTokensCacheRepository authTokensCacheRepository;
    private final TokenCookieHolder accessTokenCookie;
    private final TokenCookieHolder refreshTokenCookie;

    @Value("${ecsimsw.access.token.ttl.sec}")
    private int accessTokenJwtExpireTime;

    @Value("${ecsimsw.refresh.token.ttl.sec}")
    private int refreshTokenJwtExpireTime;

    @Value("${ecsimsw.token.payload.name}")
    private String jwtPayloadName;

    public AuthTokenService(
        String jwtSecretKey,
        AuthTokensCacheRepository authTokensCacheRepository,
        TokenCookieHolder accessTokenCookie,
        TokenCookieHolder refreshTokenCookie,
        Class<T> payloadType
    ) {
        this.jwtSecretKey = JwtUtils.createSecretKey(jwtSecretKey);
        this.authTokensCacheRepository = authTokensCacheRepository;
        this.accessTokenCookie = accessTokenCookie;
        this.refreshTokenCookie = refreshTokenCookie;
        this.payloadType = payloadType;
    }

    public void issue(HttpServletResponse response, T payload) {
        var authTokens = issueAuthTokens(payload);
        createAuthCookies(authTokens).forEach(response::addCookie);
    }

    public AuthTokens issueAuthTokens(T payload) {
        var tokenKey = getTokenKey(payload);
        if (tokenKey == null) {
            throw new InvalidTokenException("token key must not be null");
        }
        authTokensCacheRepository.deleteById(tokenKey);

        var accessToken = createToken(payload, accessTokenJwtExpireTime);
        var refreshToken = createToken(payload, refreshTokenJwtExpireTime);
        var authTokens = new AuthTokens(tokenKey, accessToken, refreshToken);
        authTokensCacheRepository.save(authTokens);
        return authTokens;
    }

    public void authenticate(HttpServletRequest request) {
        var cookies = request.getCookies();
        var accessToken = getTokenFromCookies(cookies, accessTokenCookie.getName());
        if (accessToken.isEmpty() || !isValidToken(accessToken.get())) {
            throw new AuthenticateFailedException("Access token is not available");
        }
    }

    private String getTokenKey(T payload) {
        try {
            var field = Arrays.stream(payload.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(TokenKey.class))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("Payload class doesn't have TokenKey."));
            field.setAccessible(true);
            return (String) field.get(payload);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token", e);
        }
    }

    private String createToken(T payload, int expiredTime) {
        return JwtUtils.createToken(jwtSecretKey, Map.of(jwtPayloadName, payload), expiredTime);
    }

    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        var cookies = request.getCookies();
        var refreshToken = getTokenFromCookies(cookies, refreshTokenCookie.getName());
        if (refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token not exists");
        }
        var reissue = reissueAuthTokens(refreshToken.get());
        createAuthCookies(reissue).forEach(response::addCookie);
        response.setHeader("Location", request.getRequestURI());
        response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());

        LOGGER.info("simple auth : Token reissued with refresh token");
    }

    private AuthTokens reissueAuthTokens(String refreshToken) {
        if (!isValidToken(refreshToken)) {
            throw new InvalidTokenException("Is not valid token");
        }
        var payload = JwtUtils.tokenValue(jwtSecretKey, refreshToken, jwtPayloadName, payloadType);
        var tokenKey = getTokenKey(payload);
        if (!authTokensCacheRepository.existsById(tokenKey)) {
            throw new InvalidTokenException("Not registered refresh token");
        }
        return issueAuthTokens(payload);
    }

    public Optional<String> getTokenFromCookies(Cookie[] cookies, String tokenCookieName) {
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> tokenCookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private boolean isValidToken(String token) {
        try {
            getPayloadFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public T getAccessTokenPayload(HttpServletRequest request) {
        final String token = getTokenFromCookies(request.getCookies(), accessTokenCookie.getName())
            .orElseThrow(() -> new InvalidTokenException("Token cookie not exists"));
        return getPayloadFromToken(token);
    }

    public T getPayloadFromToken(String token) {
        return JwtUtils.tokenValue(jwtSecretKey, token, jwtPayloadName, payloadType);
    }

    public List<Cookie> createAuthCookies(AuthTokens tokens) {
        var atCookie = accessTokenCookie.toCookie(tokens.getAccessToken());
        var rtCookie = refreshTokenCookie.toCookie(tokens.getRefreshToken());
        return List.of(atCookie, rtCookie);
    }
}
