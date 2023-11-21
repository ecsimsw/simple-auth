package ecsimsw.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static ecsimsw.auth.TokenCookieUtils.*;

@Component
public class AuthInterceptor<T> implements HandlerInterceptor {

    private final AuthTokenService<T> authTokenService;

    public AuthInterceptor(AuthTokenService<T> authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod) || !isLoginNeeded((HandlerMethod) handler)) {
            return true;
        }
        try {
            var cookies = request.getCookies();
            var accessToken = accessToken(cookies);
            if (authTokenService.isValidToken(accessToken)) {
                return true;
            }
            var refreshToken = refreshToken(cookies);
            if (authTokenService.isValidToken(refreshToken)) {
                var reissued = authTokenService.validateAndReissue(accessToken, refreshToken);
                var newAuthCookies = createAuthCookies(reissued);
                newAuthCookies.forEach(response::addCookie);
                response.setHeader("Location", request.getRequestURI());
                response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
                return false;
            }
            throw new IllegalArgumentException("Invalid token");
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new IllegalArgumentException("Unauthorized request");
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(LoginUser.class));
    }

    private String accessToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
    }

    private String refreshToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, REFRESH_TOKEN_COOKIE_KEY);
    }
}
