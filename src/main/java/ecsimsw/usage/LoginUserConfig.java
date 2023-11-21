package ecsimsw.usage;

import ecsimsw.auth.AuthInterceptor;
import ecsimsw.auth.AuthTokenService;
import ecsimsw.auth.AuthTokensCacheRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginUserConfig implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor<LoginUserInfo> authInterceptor(
        @Value("${token.secret.key}") String jwtSecretKey,
        AuthTokensCacheRepository authTokensCacheRepository
    ) {
        final AuthTokenService<LoginUserInfo> authTokenService = new AuthTokenService<>(
            jwtSecretKey,
            authTokensCacheRepository
        );
        return new AuthInterceptor<>(authTokenService);
    }
}
