package ecsimsw.auth.resolver;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.auth.service.AuthTokenService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthArgumentResolver<T> implements HandlerMethodArgumentResolver {

    private final AuthTokenService<T> authTokenService;

    public AuthArgumentResolver(AuthTokenService<T> authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JwtPayload.class);
    }

    @Override
    public T resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        return authTokenService.getAccessTokenPayload(request);
    }
}
