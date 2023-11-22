package ecsimsw.usage;

import ecsimsw.auth.AuthTokensCacheRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SampleApplication.class);
        ConfigurableApplicationContext run = application.run(args);

        AuthTokensCacheRepository bean = run.getBean(AuthTokensCacheRepository.class);
        System.out.println(bean);
    }
}
