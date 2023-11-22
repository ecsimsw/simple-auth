package ecsimsw.auth;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

public class TokenCookieUtils {

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
}
