package ecsimsw.usage.sample;

import ecsimsw.auth.AuthInterceptor;
import ecsimsw.auth.LoginUserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    private final AuthInterceptor<LoginUserInfo> loginUserInfoAuthInterceptor;
    private final LoginUserArgumentResolver<LoginUserInfo> loginUserArgumentResolver;

    public WebConfig(
        AuthInterceptor<LoginUserInfo> loginUserInfoAuthInterceptor,
        LoginUserArgumentResolver<LoginUserInfo> loginUserArgumentResolver
    ) {
        this.loginUserInfoAuthInterceptor = loginUserInfoAuthInterceptor;
        this.loginUserArgumentResolver = loginUserArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInfoAuthInterceptor)
            .addPathPatterns("**")
            .excludePathPatterns();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
