package ecsimsw.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SampleApplication.class);
        application.run(args);
    }
}
