package ecsimsw.auth.interceptor;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.auth.exception.InvalidAccessTokenException;
import ecsimsw.auth.service.AuthTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

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
            try {
                authTokenService.authenticate(request);
                return true;
            } catch (InvalidAccessTokenException e) {
                authTokenService.reissue(request, response);
                return false;
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new IllegalArgumentException("Unauthorized request");
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(JwtPayload.class));
    }
}
