package ecsimsw.auth.anotations;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("ecsimsw.auth")
@EntityScan("ecsimsw.auth")
public class SharedConfigurationReference {}
