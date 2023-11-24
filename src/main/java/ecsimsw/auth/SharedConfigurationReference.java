package ecsimsw.auth;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ComponentScan(basePackages = "ecsimsw.auth")
@EnableRedisRepositories(basePackages = "ecsimsw.auth")
public class SharedConfigurationReference {

    public static void main(String[] args) {

    }
}
