package ru.fcpsr.domainsport.configurations;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class Security {

    private final ReactiveClientRegistrationRepository registrationRepository;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http){
        return http
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .oauth2Login(Customizer.withDefaults())
                //.oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec.authenticationConverter(authenticationConverter()).authenticationManager(authenticationManager()))
                .build();
    }

    @Bean
    public ServerAuthenticationConverter authenticationConverter() {
        return exchange -> {
            return registrationRepository.findByRegistrationId("yandex").flatMap(clientRegistration  -> {
                String code = exchange.getRequest().getQueryParams().get("code").get(0);
                String state = exchange.getRequest().getQueryParams().get("state").get(0);
                System.out.println(code + " " + state);
                // Создание OAuth2AuthorizationExchange
                OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(
                        OAuth2AuthorizationRequest.authorizationCode()
                                .clientId(clientRegistration.getClientId())
                                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                                .redirectUri(clientRegistration.getRedirectUri())
                                .state(state)
                                .build(),
                        OAuth2AuthorizationResponse.success(code)
                                .state(state)
                                .redirectUri(clientRegistration.getRedirectUri())
                                .build()
                );

                // Создание OAuth2AuthorizationCodeAuthenticationToken
                OAuth2AuthorizationCodeAuthenticationToken authenticationToken =
                        new OAuth2AuthorizationCodeAuthenticationToken(
                                clientRegistration,
                                authorizationExchange
                        );

                return Mono.just(authenticationToken);
            });
        };
    }


    @Bean
    public ReactiveAuthenticationManager authenticationManager(){
        return authentication -> {
            System.out.println("Работает менеджер!");
            System.out.println(authentication.toString());
            return Mono.just(authentication);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
