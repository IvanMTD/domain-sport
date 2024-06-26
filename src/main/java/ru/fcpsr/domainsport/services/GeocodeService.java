package ru.fcpsr.domainsport.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.geocode.GeocodeResponse;

@Service
@PropertySource("classpath:application.properties")
public class GeocodeService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeocodeService(@Value("${yandex.map.apikey}") String apiKey){
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        webClient = WebClient.builder().baseUrl("https://geocode-maps.yandex.ru/1.x").build();
    }

    public Mono<GeocodeResponse> getResponse(String address){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apikey", apiKey)
                        .queryParam("geocode", address)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class).flatMap(jsonString -> {
                    try {
                        return Mono.just(objectMapper.readValue(jsonString, GeocodeResponse.class));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }
}
