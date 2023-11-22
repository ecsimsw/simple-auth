package ecsimsw.auth;

import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var accessToken = authTokenService.getAccessToken(request.getCookies());
        if (authTokenService.isValidToken(accessToken)) {
            return authTokenService.getPayloadFromToken(accessToken);
        }
        throw new IllegalArgumentException("Unauthorized user request");
    }
}
