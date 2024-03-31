package ru.fcpsr.domainsport.configurations;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.repositories.AppUserRepository;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.time.LocalDate;

@Configuration
public class ApplicationConfigurations {
    @Bean
    public CommandLineRunner preSetup(AppUserRepository userRepository, SportRepository sportRepository, PasswordEncoder encoder){
        return args -> {
            userRepository.findByUsername("admin")
                    .switchIfEmpty(Mono.just(new AppUser()))
                    .flatMap(user -> {
                        AppUser appUser = (AppUser) user;
                        appUser.setUsername("admin");
                        appUser.setPassword(encoder.encode("Admin2020"));
                        appUser.setFirstname("Master");
                        appUser.setLastname("Admin");
                        appUser.setPlacedAt(LocalDate.now());
                        appUser.setRole(Role.ADMIN);
                        return userRepository.save(appUser);
                    }).subscribe();
        };
    }
}
