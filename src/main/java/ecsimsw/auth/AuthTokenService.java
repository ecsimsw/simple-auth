package ecsimsw.auth;

import java.util.List;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;

public class AuthTokenService <T> {

    @Value("${ecsimsw.access.token.ttl.sec}")
    private int accessTokenJwtExpireTimeSec;

    @Value("${ecsimsw.refresh.token.ttl.sec}")
    private int refreshTokenJwtExpireTimeSec;

    @Value("${ecsimsw.token.payload.name}")
    private String jwtPayloadName;

    private final Key jwtSecretKey;
    private final Class<T> payloadType;
    private final AuthTokensCacheRepository authTokensCacheRepository;

    private final CookieBuilder accessTokenCookie;
    private final CookieBuilder refreshTokenCookie;

    public AuthTokenService(
        String jwtSecretKey,
        AuthTokensCacheRepository authTokensCacheRepository,
        CookieBuilder accessTokenCookie,
        CookieBuilder refreshTokenCookie
    ) {
        this.jwtSecretKey = JwtUtils.createSecretKey(jwtSecretKey);
        this.authTokensCacheRepository = authTokensCacheRepository;
        this.accessTokenCookie = accessTokenCookie;
        this.refreshTokenCookie = refreshTokenCookie;

        var parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.payloadType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public AuthTokens issue(T payload) {
        final String tokenKey = getTokenKey(payload);
        authTokensCacheRepository.deleteById(tokenKey);

        final String accessToken = createToken(payload, accessTokenJwtExpireTimeSec);
        final String refreshToken = createToken(payload, refreshTokenJwtExpireTimeSec);
        final AuthTokens authTokens = new AuthTokens(tokenKey, accessToken, refreshToken);
        authTokensCacheRepository.save(authTokens);
        return authTokens;
    }

    public AuthTokens validateAndReissue(String accessToken, String refreshToken) {
        JwtUtils.requireExpired(jwtSecretKey, accessToken);
        JwtUtils.requireLived(jwtSecretKey, refreshToken);

        var tokenKeyFromAT = getTokenKey(accessToken);
        var tokenKeyFromRT = getTokenKey(refreshToken);
        if(!tokenKeyFromAT.equals(tokenKeyFromRT)) {
            throw new IllegalArgumentException("Tokens are not from same user");
        }
        final AuthTokens currentAuthToken = authTokensCacheRepository.findById(tokenKeyFromAT).orElseThrow(() -> new IllegalArgumentException("Not valid user"));
        currentAuthToken.checkSameWith(accessToken, refreshToken);

        final T payload = JwtUtils.tokenValue(jwtSecretKey, refreshToken, jwtPayloadName, payloadType, true);
        return issue(payload);
    }

    private String getTokenKey(String token) {
        T payload = JwtUtils.tokenValue(jwtSecretKey, token, jwtPayloadName, payloadType, true);
        return getTokenKey(payload);
    }

    private String getTokenKey(T payload) {
        try {
            final Field field = Arrays.stream(payload.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(TokenKey.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Payload class doesn't have TokenKey."));
            return (String) field.get(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private String createToken(T payload, int expiredTime) {
        return JwtUtils.createToken(jwtSecretKey, Map.of(jwtPayloadName, payload), expiredTime);
    }

    public boolean isValidToken(String token) {
        try {
            getPayloadFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public T getPayloadFromToken(String token) {
        return JwtUtils.tokenValue(jwtSecretKey, token, jwtPayloadName, payloadType);
    }

    public String getAccessToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, accessTokenCookie.getName());
    }

    public String getRefreshToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, refreshTokenCookie.getName());
    }

    public static String getTokenFromCookies(Cookie[] cookies, String tokenCookieKey) {
        if (cookies == null) {
            throw new IllegalArgumentException("Not authorized - No cookie");
        }
        return Arrays.stream(cookies)
            .filter(cookie -> tokenCookieKey.equals(cookie.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not authorized"))
            .getValue();
    }

    public List<Cookie> createAuthCookies(AuthTokens tokens) {
        final Cookie atCookie = new Cookie(accessTokenCookie.getName(), tokens.getAccessToken());
        atCookie.setHttpOnly(accessTokenCookie.isHttpOnly());
        atCookie.setPath(accessTokenCookie.getPath());
        atCookie.setMaxAge(accessTokenCookie.getMaxAge());

        final Cookie rtCookie = new Cookie(refreshTokenCookie.getName(), tokens.getRefreshToken());
        rtCookie.setHttpOnly(refreshTokenCookie.isHttpOnly());
        rtCookie.setPath(refreshTokenCookie.getPath());
        rtCookie.setMaxAge(refreshTokenCookie.getMaxAge());
        return List.of(atCookie, rtCookie);
    }
}
