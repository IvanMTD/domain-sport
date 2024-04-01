package ru.fcpsr.domainsport.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.MailMessage;
import ru.fcpsr.domainsport.enums.Status;
import ru.fcpsr.domainsport.services.*;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    private final EkpService ekpService;
    private final GeocodeService geocodeService;
    private final MinioService minioService;
    private final MinioFileService fileService;

    private final EventService eventService;

    @GetMapping("/show")
    public Mono<Rendering> showEventPage(@RequestParam(name = "eventId") long eventId){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Event page")
                        .modelAttribute("index","event-page")
                        .modelAttribute("event", eventService.getEvent(eventId))
                        .build()
        );
    }

    @GetMapping("/all")
    public Mono<Rendering> eventsPage(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size, @RequestParam(name = "search") String search){
        return ekpService.getCount(search).flatMap(count -> {
            int pageControl = page;
            if(pageControl < 0){
                pageControl = 0;
            }
            long lastPage = count / size;
            if(pageControl >= lastPage){
                pageControl = (int)lastPage;
            }
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title", "Events page")
                            .modelAttribute("index", "events-page")
                            .modelAttribute("events", ekpService
                                    .getAllByParam(PageRequest.of(pageControl,size), search)
                                    .flatMap(eventService::completedEvent)
                                    .collectList().flatMapMany(eventList -> {
                                        eventList = eventList.stream().sorted(Comparator.comparing(EkpDTO::getId)).collect(Collectors.toList());
                                        return Flux.fromIterable(eventList);
                                    }).flatMapSequential(Mono::just)
                            )
                            .modelAttribute("lastPage", lastPage)
                            .modelAttribute("page", pageControl)
                            .modelAttribute("size", size)
                            .modelAttribute("search", search)
                            .build()
            );
        });
    }

    @GetMapping("/add")
    public Mono<Rendering> eventAddPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Event add")
                        .modelAttribute("index","event-add-page")
                        .modelAttribute("event",new EkpDTO())
                        .modelAttribute("statusList", Status.values())
                        .build()
        );
    }

    @PostMapping("/add")
    public Mono<Rendering> eventAdd(@ModelAttribute(name = "event") @Valid EkpDTO ekpDTO, Errors errors){
        if(ekpDTO.getEkp() == null && ekpDTO.getNum() == null){
            errors.rejectValue("ekp","","Укажите оба или один из вариантов ЕКП или Иной номер");
        }
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Event add")
                            .modelAttribute("index","event-add-page")
                            .modelAttribute("event", ekpDTO)
                            .modelAttribute("statusList", Status.values())
                            .build()
            );
        }

        return geocodeService.getResponse(ekpDTO).flatMap(response -> {
            String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
            String[] part = pos.split(" ");
            float s = Float.parseFloat(part[1]);
            float d = Float.parseFloat(part[0]);
            ekpDTO.setS(s);
            ekpDTO.setD(d);
            return ekpService.save(ekpDTO).flatMap(ekp -> {
                log.info(ekp.toString());
                if(ekpDTO.getLogo() != null) {
                    return minioService.uploadImage(ekpDTO.getLogo()).flatMap(minioResponse -> {
                        log.info(minioResponse.toString());
                        return fileService.save(minioResponse, ekp.getId()).flatMap(minioFile -> {
                            if (minioFile.getMid() != 0) {
                                ekp.setLogo(minioFile.getId());
                                return ekpService.save(ekp);
                            } else {
                                return Mono.just(ekp);
                            }
                        });
                    });
                }else {
                    return Mono.just(ekp);
                }
            }).flatMap(ekp -> {
                log.info(ekp.toString());
                if(ekpDTO.getImage() != null) {
                    return minioService.uploadImage(ekpDTO.getImage()).flatMap(minioResponse -> {
                        log.info(minioResponse.toString());
                        return fileService.save(minioResponse, ekp.getId()).flatMap(minioFile -> {
                            if (minioFile.getMid() != 0) {
                                ekp.setImage(minioFile.getId());
                                return ekpService.save(ekp);
                            } else {
                                return Mono.just(ekp);
                            }
                        });
                    });
                }else{
                    return Mono.just(ekp);
                }
            }).flatMap(ekp -> {
                log.info("ekp saved " + ekp.toString());
                return Mono.just(Rendering.redirectTo("/event/all?page=0&size=10&search=all").build());
            });
        });
    }

    @GetMapping("/edit")
    public Mono<Rendering> editPage(@RequestParam(name = "eventId") long eventId){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Event edit page")
                        .modelAttribute("index","event-edit-page")
                        .modelAttribute("statusList", Status.values())
                        .modelAttribute("event", eventService.getEvent(eventId))
                        .build()
        );
    }

    @PostMapping("/update")
    public Mono<Rendering> updateEvent(@ModelAttribute(name = "event") @Valid EkpDTO ekpDTO, Errors errors){
        if(ekpDTO.getEkp() == null && ekpDTO.getNum() == null){
            errors.rejectValue("ekp","","Укажите оба или один из вариантов ЕКП или Иной номер");
        }
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Event add")
                            .modelAttribute("index","event-add-page")
                            .modelAttribute("event", ekpDTO)
                            .modelAttribute("statusList", Status.values())
                            .build()
            );
        }

        return geocodeService.getResponse(ekpDTO).flatMap(response -> {
            String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
            String[] part = pos.split(" ");
            float s = Float.parseFloat(part[1]);
            float d = Float.parseFloat(part[0]);
            ekpDTO.setS(s);
            ekpDTO.setD(d);
            return ekpService.update(ekpDTO).flatMap(ekp -> {
                log.info(ekp.toString());
                if(ekpDTO.getLogo() != null) {
                    if(ekp.getLogo() == 0){
                        return minioService.uploadImage(ekpDTO.getLogo()).flatMap(minioResponse -> {
                            log.info("file {} uploaded on minio", minioResponse.getOriginalFileName());
                            return fileService.save(minioResponse, ekp.getId()).flatMap(minioFileNew -> {
                                if (minioFileNew.getMid() != 0) {
                                    ekp.setLogo(minioFileNew.getId());
                                    return ekpService.save(ekp);
                                } else {
                                    return Mono.just(ekp);
                                }
                            });
                        });
                    }else {
                        return fileService.findById(ekp.getLogo()).flatMap(minioFile -> {
                            log.info("found minio file " + minioFile.toString());
                            return minioService.delete(minioFile).then(Mono.just(minioFile)).flatMap(mfn -> {
                                log.info("file {} deleted from minio", mfn.getName());
                                return fileService.deleteById(mfn.getId()).flatMap(mfd -> {
                                    log.info("file {} deleted from database", mfd.getName());
                                    return minioService.uploadImage(ekpDTO.getLogo()).flatMap(minioResponse -> {
                                        log.info("file {} uploaded on minio", minioResponse.getOriginalFileName());
                                        return fileService.save(minioResponse, ekp.getId()).flatMap(minioFileNew -> {
                                            if (minioFileNew.getMid() != 0) {
                                                ekp.setLogo(minioFileNew.getId());
                                                return ekpService.save(ekp);
                                            } else {
                                                return Mono.just(ekp);
                                            }
                                        });
                                    });
                                });
                            });
                        });
                    }
                }else {
                    return Mono.just(ekp);
                }
            }).flatMap(ekp -> {
                log.info(ekp.toString());
                if(ekpDTO.getImage() != null) {
                    if(ekp.getImage() == 0){
                        return minioService.uploadImage(ekpDTO.getImage()).flatMap(minioResponse -> {
                            log.info("file {} uploaded on minio", minioResponse.getOriginalFileName());
                            return fileService.save(minioResponse, ekp.getId()).flatMap(minioFileNew -> {
                                if (minioFileNew.getMid() != 0) {
                                    ekp.setImage(minioFileNew.getId());
                                    return ekpService.save(ekp);
                                } else {
                                    return Mono.just(ekp);
                                }
                            });
                        });
                    }else {
                        return fileService.findById(ekp.getImage()).flatMap(minioFile -> {
                            log.info("found minio file " + minioFile.toString());
                            return minioService.delete(minioFile).then(Mono.just(minioFile)).flatMap(mfn -> {
                                log.info("file {} deleted from minio", mfn.getName());
                                return fileService.deleteById(mfn.getId()).flatMap(mfd -> {
                                    log.info("file {} deleted from database", mfd.getName());
                                    return minioService.uploadImage(ekpDTO.getImage()).flatMap(minioResponse -> {
                                        log.info("file {} uploaded on minio", minioResponse.getOriginalFileName());
                                        return fileService.save(minioResponse, ekp.getId()).flatMap(minioFileNew -> {
                                            if (minioFileNew.getMid() != 0) {
                                                ekp.setImage(minioFileNew.getId());
                                                return ekpService.save(ekp);
                                            } else {
                                                return Mono.just(ekp);
                                            }
                                        });
                                    });
                                });
                            });
                        });
                    }
                }else{
                    return Mono.just(ekp);
                }
            }).flatMap(ekp -> {
                log.info("ekp updated " + ekp.toString());
                return Mono.just(Rendering.redirectTo("/event/show?eventId=" + ekp.getId()).build());
            });
        });
    }

    @GetMapping("/delete")
    public Mono<Rendering> deleteEvent(@RequestParam(name = "eventId") long eventId){
        return ekpService.getById(eventId).flatMap(ekoOriginal -> {
            log.info("found ekp with id = {}", ekoOriginal.getId());
            if(ekoOriginal.getLogo() != 0) {
                return fileService.findById(ekoOriginal.getLogo()).flatMap(minioFileOriginal -> {
                    log.info("found minio file {}", minioFileOriginal.getName());
                    return minioService.delete(minioFileOriginal).then(Mono.just(minioFileOriginal)).flatMap(minioFile -> {
                        log.info("file {} deleted from minio", minioFile.getName());
                        return fileService.deleteById(minioFile.getId()).flatMap(f -> {
                            log.info("file {} deleted from database", f.getName());
                            return Mono.just(ekoOriginal);
                        });
                    });
                });
            }
            return Mono.just(ekoOriginal);
        }).flatMap(ekoOriginal -> {
            if(ekoOriginal.getImage() != 0) {
                return fileService.findById(ekoOriginal.getImage()).flatMap(minioFileOriginal -> {
                    log.info("found minio file {}", minioFileOriginal.getName());
                    return minioService.delete(minioFileOriginal).then(Mono.just(minioFileOriginal)).flatMap(minioFile -> {
                        log.info("file {} deleted from minio", minioFile.getName());
                        return fileService.deleteById(minioFile.getId()).flatMap(f -> {
                            log.info("file {} deleted from database", f.getName());
                            return Mono.just(ekoOriginal);
                        });
                    });
                });
            }
            return Mono.just(ekoOriginal);
        }).flatMap(ekpOriginal -> {
            log.info("all images were deleted or there were none");
            return ekpService.delete(ekpOriginal).flatMap(ekp -> {
                log.info("ekp id = {}, has been totally deleted", ekp.getId());
                return Mono.just(Rendering.redirectTo("/event/all?page=0&size=10&search=all").build());
            });
        });
    }
}
