package ecsimsw.auth.sample;

import ecsimsw.auth.domain.AuthTokensCacheRepository;
import ecsimsw.auth.domain.CookieBuilder;
import ecsimsw.auth.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthTokenConfig implements WebMvcConfigurer {

    @Bean
    public AuthTokenService authTokenService(
        @Value("${ecsimsw.token.secret.key}") String jwtSecretKey,
        @Autowired AuthTokensCacheRepository authTokensCacheRepository
    ) {
        var atCookieBuilder = new CookieBuilder("at", 180, "/", true, false);
        var rtCookieBuilder = new CookieBuilder("rt", 3600, "/", true, false);
        return new AuthTokenService<>(
            jwtSecretKey,
            authTokensCacheRepository,
            atCookieBuilder,
            rtCookieBuilder,
            MyLoginPayload.class
        );
    }
}
