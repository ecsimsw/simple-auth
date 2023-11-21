package ecsimsw.auth;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

public class TokenCookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_KEY = "auth_at";
    public static final int ACCESS_TOKEN_COOKIE_TTL_SEC = 60 * 3;
    public static final String REFRESH_TOKEN_COOKIE_KEY = "auth_rt";
    public static final int REFRESH_TOKEN_COOKIE_TTL_SEC = 60 * 3 * 30;

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

    public static List<Cookie> createAuthCookies(TokenCache tokenCache) {
        final Cookie accessTokenCookie = createAccessTokenCookie(tokenCache.getAccessToken());
        final Cookie refreshTokenCookie = createRefreshTokenCookie(tokenCache.getRefreshToken());
        return List.of(accessTokenCookie, refreshTokenCookie);
    }

    public static Cookie createAccessTokenCookie(String accessToken) {
        final Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_COOKIE_TTL_SEC);
        cookie.setSecure(false);
        return cookie;
    }

    public static Cookie createRefreshTokenCookie(String refreshToken) {
        final Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_TTL_SEC);
        cookie.setSecure(false);
        return cookie;
    }
}
