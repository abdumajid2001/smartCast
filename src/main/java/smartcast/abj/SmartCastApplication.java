package smartcast.abj;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import smartcast.abj.dto.auth.AuthenticationRequest;
import smartcast.abj.service.AuthenticationService;

@SpringBootApplication
public class SmartCastApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCastApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(AuthenticationService service) {
//        return (args -> service.register(new AuthenticationRequest("username", "123")));
//    }

}
