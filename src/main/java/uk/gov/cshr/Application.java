package uk.gov.cshr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.repository.UserRepository;
import uk.gov.cshr.service.AccessTokenService;
import uk.gov.cshr.service.ClientService;
import uk.gov.cshr.service.UserService;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner demo(UserService userService, UserRepository userRepository, AccessTokenService accessTokenService, ClientService clientService,
                                  RoleRepository roleRepository) {
        return (args) -> {
//            log.error(clientService.createNewClient("test", true).toString());
//
////            Hibernate.initialize();
//            roleRepository.save(new Role("ADMIN", "Admin role"));
//            roleRepository.save(new Role("USER", "User role"));
//
//            // save a couple of customers
//            userService.createNewUser("user@domain.com", "Bauer", true);
//            userService.createNewUser("admin@domain.com", "yarrak", true);
//            log.error(userRepository.findFirstByActiveTrueAndEmailEquals("user@domain.com").toString());
//            log.error(userRepository.findFirstByActiveTrueAndEmailEquals("admin@domain.com").toString());
//            log.error(accessTokenService.generateAccessToken().toString());

        };
    }

}