package ecsimsw.auth.sample;

import ecsimsw.auth.interceptor.AuthArgumentResolver;
import ecsimsw.auth.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor loginUserInfoAuthInterceptor;
    private final AuthArgumentResolver authArgumentResolver;

    public WebConfig(
        AuthInterceptor loginUserInfoAuthInterceptor,
        AuthArgumentResolver authArgumentResolver
    ) {
        this.loginUserInfoAuthInterceptor = loginUserInfoAuthInterceptor;
        this.authArgumentResolver = authArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInfoAuthInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }
}
