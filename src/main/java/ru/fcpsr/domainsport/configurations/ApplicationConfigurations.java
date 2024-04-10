package ru.fcpsr.domainsport.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.repositories.AppUserRepository;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.time.LocalDate;

@Slf4j
@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfigurations {
    @Value("${app.password}")
    private String defaultPassword;
    @Value("${mail.name}")
    private String mail;
    @Bean
    public CommandLineRunner preSetup(AppUserRepository userRepository, SportRepository sportRepository, PasswordEncoder encoder){
        return args -> {
            log.info("*****************environments start*******************");
            for(String key : System.getenv().keySet()){
                String[] p = key.split("\\.");
                if(p.length > 1) {
                    log.info("\"{}\"=\"{}\"", key, System.getenv().get(key));
                }
            }
            log.info("******************environments end********************");
            userRepository.findByUsername("admin").flatMap(user -> {
                log.info("user {} exist", user.getUsername());
                return Mono.just(user);
            }).switchIfEmpty(
                    Mono.just(new AppUser()).flatMap(appUser -> {
                        appUser.setUsername("admin");
                        appUser.setPassword(encoder.encode(defaultPassword));
                        appUser.setFirstname("Master");
                        appUser.setLastname("Admin");
                        appUser.setBirthday(LocalDate.of(1985,10,30));
                        appUser.setEmail(mail);
                        appUser.setPlacedAt(LocalDate.now());
                        appUser.setRole(Role.ADMIN);
                        return userRepository.save(appUser);
                    })
            ).subscribe();
        };
    }
}
