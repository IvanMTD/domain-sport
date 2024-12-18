package ru.fcpsr.domainsport.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.DisciplineDTO;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.services.DisciplineService;
import ru.fcpsr.domainsport.services.EkpService;
import ru.fcpsr.domainsport.services.NewsService;
import ru.fcpsr.domainsport.services.SportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EkpService ekpService;
    private final SportService sportService;
    private final DisciplineService disciplineService;

    private final NewsService newsService;

    @GetMapping("/")
    public Mono<Rendering> homePage(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Наша Смена: Предстоящие Спортивные События, ЕКП, спортивные школы и спортивные объекты")
                        .modelAttribute("description","Сервис Наша Смена предоставляет информацию о предстоящих спортивных событиях, спортивных школах и спортобъектах. У нас вы найдете актуальные новости, расписание мероприятий и многое другое. Присоединяйтесь к нам для быстрого и легкого доступа к информации о мире спорта")
                        .modelAttribute("index","home-page")
                        .modelAttribute("ekpList", ekpService.getByDate(LocalDate.now()))
                        .modelAttribute("eventList", getEventList())
                        .modelAttribute("newsList", newsService.getAllSortedById().take(4))
                        .modelAttribute("currentDate", LocalDate.now().format(formatter))
                        .build()
        );
        //return Mono.just(Rendering.redirectTo("/school/list?page=0&size=12&search=all").build());
    }

    @GetMapping("/error")
    public Mono<Rendering> errorPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title", "Error page")
                        .modelAttribute("index","error-page")
                        .build()
        );
    }

    private Flux<EkpDTO> getEventList(){
        return ekpService.getAllSortedByCurrentDate().take(4).flatMap(ekp -> {
            EkpDTO ekpDTO = new EkpDTO(ekp);
            return sportService.getById(ekp.getSportId()).flatMap(sport -> {
                SportDTO sportDTO = new SportDTO(sport);
                return disciplineService.getAllByIds(ekp.getDisciplineIds()).flatMap(discipline -> {
                    DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
                    return Mono.just(disciplineDTO);
                }).collectList().flatMap(dl -> {
                    dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
                    sportDTO.setDisciplines(dl);
                    ekpDTO.setSport(sportDTO);
                    return Mono.just(ekpDTO);
                });
            });
        }).collectList().flatMapMany(ekpList -> {
            ekpList = ekpList.stream().sorted(Comparator.comparing(EkpDTO::getBeginning)).collect(Collectors.toList());
            return Flux.fromIterable(ekpList);
        }).flatMapSequential(Mono::just);
    }
}
