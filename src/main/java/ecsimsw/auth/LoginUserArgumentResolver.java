package ecsimsw.auth;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static ecsimsw.auth.TokenCookieUtils.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.auth.TokenCookieUtils.getTokenFromCookies;

@Component
public class LoginUserArgumentResolver<T> implements HandlerMethodArgumentResolver {

    private final AuthTokenService<T> authTokenService;

    public LoginUserArgumentResolver(AuthTokenService<T> authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public T resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final Cookie[] cookies = request.getCookies();
        final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
        if (authTokenService.isValidToken(accessToken)) {
            return authTokenService.getPayloadFromToken(accessToken);
        }
        throw new IllegalArgumentException("Unauthorized user request");
    }
}
