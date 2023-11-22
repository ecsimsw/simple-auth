package ecsimsw.auth.anotations;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ComponentScan(basePackages = "ecsimsw.auth")
@EntityScan(basePackages = "ecsimsw.auth")
@EnableRedisRepositories(basePackages = "ecsimsw.auth")
public class SharedConfigurationReference {}
