package ecsimsw.usage.sample;

import ecsimsw.auth.AuthInterceptor;
import ecsimsw.auth.AuthTokenService;
import ecsimsw.auth.AuthTokensCacheRepository;

import ecsimsw.auth.CookieBuilder;
import ecsimsw.auth.LoginUserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginUserConfig implements WebMvcConfigurer {

    @Bean
    public AuthTokenService<LoginUserInfo> authTokenService(
        @Value("${ecsimsw.token.secret.key}") String jwtSecretKey,
        @Autowired AuthTokensCacheRepository authTokensCacheRepository
    ) {
        return new AuthTokenService<>(
            jwtSecretKey,
            authTokensCacheRepository,
            new CookieBuilder("accessTokenCookie", 10, "/", true, false),
            new CookieBuilder("refreshTokenCookie", 100000, "/", true, false)
        );
    }

    @Bean
    public AuthInterceptor<LoginUserInfo> authInterceptor(
        @Autowired AuthTokenService<LoginUserInfo> authTokenService
    ) {
        return new AuthInterceptor<>(authTokenService);
    }

    @Bean
    public LoginUserArgumentResolver<LoginUserInfo> argumentResolver(
        @Autowired AuthTokenService<LoginUserInfo> authTokenService
    ) {
        return new LoginUserArgumentResolver<>(authTokenService);
    }
}
