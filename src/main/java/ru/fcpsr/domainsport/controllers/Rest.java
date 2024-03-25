package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.services.EkpService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class Rest {
    private final EkpService ekpService;
    @GetMapping("/get/date")
    public Flux<EkpDTO> findByDate(@RequestParam(name = "query") String query){
        return ekpService.getByDate(query);
    }
}
