package ru.fcpsr.domainsport.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.SchoolDTO;
import ru.fcpsr.domainsport.services.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/school")
public class SchoolController {
    private final SchoolService schoolService;
    private final AccessService accessService;
    private final GeocodeService geocodeService;
    private final MinioService minioService;
    private final MinioFileService fileService;

    @GetMapping("/list")
    public Mono<Rendering> schoolListPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size, @RequestParam(name = "search") String search){
        return schoolService.getCountBySubject(search).flatMap(count -> {
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
                            .modelAttribute("title","School list")
                            .modelAttribute("index", "school-list-page")
                            .modelAttribute("schools",schoolService.getAllBySearch(PageRequest.of(pageControl,size),search).flatMap(school -> {
                                SchoolDTO schoolDTO = new SchoolDTO(school);
                                return accessService.getAccess(authentication, "SCHOOL", "UPDATE").flatMap(update -> {
                                    schoolDTO.setUpdate(update);
                                    return accessService.getAccess(authentication, "SCHOOL", "DELETE").flatMap(delete -> {
                                        schoolDTO.setDelete(delete);
                                        return Mono.just(schoolDTO);
                                    });
                                });
                            }))
                            .modelAttribute("search",search)
                            .modelAttribute("lastPage",lastPage)
                            .modelAttribute("page",pageControl)
                            .modelAttribute("size", size)
                            .modelAttribute("accessCreate", accessService.getAccess(authentication, "SCHOOL", "CREATE"))
                            .build()
            );
        });
    }

    @GetMapping("/show")
    public Mono<Rendering> showSchoolPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "school") long schoolId){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","School page")
                        .modelAttribute("index","school-page")
                        .modelAttribute("school", schoolService.getById(schoolId))
                        .modelAttribute("accessUpdate", accessService.getAccess(authentication,"SCHOOL","UPDATE"))
                        .modelAttribute("accessDelete", accessService.getAccess(authentication,"SCHOOL","DELETE"))
                        .build()
        );
    }

    @GetMapping("/add")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','CREATE')")
    public Mono<Rendering> addSchoolPage(@AuthenticationPrincipal Authentication authentication){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Добавить спортивную школу")
                        .modelAttribute("index","school-add-page")
                        .modelAttribute("school", new SchoolDTO())
                        .build()
        );
    }

    @PostMapping("/add")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','CREATE')")
    public Mono<Rendering> addSchool(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "school") @Valid SchoolDTO schoolDTO, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Добавить спортивную школу")
                            .modelAttribute("index","school-add-page")
                            .modelAttribute("school", schoolDTO)
                            .build()
            );
        }else{
            return schoolService.createSchool(schoolDTO).flatMap(firstStepSchool -> {
                log.info("school has been pre saved: {}", firstStepSchool);
                return geocodeService.getResponse(firstStepSchool.getAddress()).flatMap(response -> {
                    String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
                    String[] part = pos.split(" ");
                    float s = Float.parseFloat(part[1]);
                    float d = Float.parseFloat(part[0]);
                    firstStepSchool.setS(s);
                    firstStepSchool.setD(d);
                    return Mono.just(firstStepSchool);
                }).switchIfEmpty(Mono.just(firstStepSchool));
            }).flatMap(secondStepSchool -> {
                log.info("second step add image");
                if(schoolDTO.getImage() != null){
                    return minioService.uploadImage(schoolDTO.getImage()).flatMap(minioResponse -> {
                        log.info("image saved in minio with data {}", minioResponse.getResponse().etag());
                        return fileService.save(minioResponse, secondStepSchool.getId()).flatMap(minioFile -> {
                            log.info("information saved in database with data {}", minioFile.getId());
                            secondStepSchool.setPhotoId(minioFile.getId());
                            return Mono.just(secondStepSchool);
                        });
                    });
                }else{
                    log.info("image not found");
                    return Mono.just(secondStepSchool);
                }
            }).flatMap(lastStepSchool -> {
                log.info("last step try add logo");
                if(schoolDTO.getLogo() != null){
                    return minioService.uploadImage(schoolDTO.getLogo()).flatMap(minioResponse -> {
                        log.info("logo saved in minio with data {}", minioResponse.getResponse().etag());
                        return fileService.save(minioResponse, lastStepSchool.getId()).flatMap(minioFile -> {
                            log.info("information saved in database with data {}", minioFile.getId());
                            lastStepSchool.setLogoId(minioFile.getId());
                            return schoolService.save(lastStepSchool);
                        });
                    });
                }else{
                    log.info("logo not found");
                    return schoolService.save(lastStepSchool);
                }
            }).flatMap(school -> {
                log.info("school totally saved with data {}", school);
                return Mono.just(
                        Rendering.redirectTo("/school/list?page=0&size=12&search=all").build()
                );
            });
        }
    }

    @GetMapping("/edit")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','UPDATE')")
    public Mono<Rendering> schoolEditPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "school") long id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Страница редактирования школы")
                        .modelAttribute("index","school-edit-page")
                        .modelAttribute("school", schoolService.getById(id).flatMap(school -> Mono.just(new SchoolDTO(school))))
                        .build()
        );
    }

    @PostMapping("/edit")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','UPDATE')")
    public Mono<Rendering> schoolEdit(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "school") @Valid SchoolDTO schoolDTO, Errors errors){
        log.info("incoming data [{}]", schoolDTO);
        //return Mono.just(Rendering.redirectTo("/").build());

        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Страница редактирования школы")
                            .modelAttribute("index","school-edit-page")
                            .modelAttribute("school", schoolDTO)
                            .build()
            );
        }

        return schoolService.getById(schoolDTO.getId()).flatMap(school -> {
            log.info("first step - try update logo");
            if(schoolDTO.getLogo() != null){
                return fileService.deleteById(school.getLogoId()).flatMap(minioFile -> {
                    log.info("found logo");
                    return minioService.delete(minioFile).then(Mono.just(schoolDTO.getLogo()).flatMap(logo -> {
                        log.info("init updated logo");
                        return minioService.uploadImage(logo).flatMap(response -> {
                            log.info("save logo data in db");
                            return fileService.save(response, school.getId()).flatMap(mf -> {
                                school.setLogoId(mf.getId());
                                return Mono.just(school);
                            });
                        });
                    }));
                }).switchIfEmpty(Mono.just(schoolDTO.getLogo()).flatMap(logo -> {
                    log.info("logo not found in db try upload new");
                    return minioService.uploadImage(logo).flatMap(response -> {
                        log.info("save logo data in db");
                        return fileService.save(response, school.getId()).flatMap(mf -> {
                            school.setLogoId(mf.getId());
                            return Mono.just(school);
                        });
                    });
                }));
            }else{
                return Mono.just(school);
            }
        }).flatMap(school -> {
            log.info("second step - try update photo");
            if(schoolDTO.getImage() != null){
                return fileService.deleteById(school.getPhotoId()).flatMap(minioFile -> {
                    log.info("found photo");
                    return minioService.delete(minioFile).then(Mono.just(schoolDTO.getImage()).flatMap(image -> {
                        log.info("init updated photo");
                        return minioService.uploadImage(image).flatMap(response -> {
                            log.info("save photo data in db");
                            return fileService.save(response, school.getId()).flatMap(mf -> {
                                school.setPhotoId(mf.getId());
                                return Mono.just(school);
                            });
                        });
                    }));
                }).switchIfEmpty(Mono.just(schoolDTO.getImage()).flatMap(image -> {
                    log.info("photo not found in db try upload new");
                    return minioService.uploadImage(image).flatMap(response -> {
                        log.info("save photo data in db");
                        return fileService.save(response, school.getId()).flatMap(mf -> {
                            school.setPhotoId(mf.getId());
                            return Mono.just(school);
                        });
                    });
                }));
            }else{
                return Mono.just(school);
            }
        }).flatMap(school -> {
            log.info("next step - try update coords");
            if(!school.getAddress().equals(schoolDTO.getAddress())){
                return geocodeService.getResponse(schoolDTO.getAddress()).flatMap(response -> {
                    String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
                    String[] part = pos.split(" ");
                    float s = Float.parseFloat(part[1]);
                    float d = Float.parseFloat(part[0]);
                    school.setAddress(schoolDTO.getAddress());
                    school.setS(s);
                    school.setD(d);
                    return Mono.just(school);
                }).switchIfEmpty(Mono.just(school));
            }else{
                return Mono.just(school);
            }
        }).flatMap(school -> {
            school.setName(schoolDTO.getName());
            school.setDescription(schoolDTO.getDescription());
            school.setOgrn(schoolDTO.getOgrn());
            school.setIndex(schoolDTO.getIndex());
            school.setSubject(schoolDTO.getSubject());
            school.setUrl(schoolDTO.getUrl());
            return schoolService.save(school).flatMap(updated -> {
                log.info("update completed [{}]",updated);
                return Mono.just(Rendering.redirectTo("/school/show?school=" + school.getId()).build());
            });
        });
    }

    @GetMapping("/delete")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','DELETE')")
    public Mono<Rendering> deleteSchool(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "school") long id){
        return schoolService.deleteById(id).flatMap(school -> {
            log.info("first step - delete logo");
            return fileService.deleteById(school.getLogoId()).flatMap(minioFile -> {
                log.info("found logo");
                return minioService.delete(minioFile).then(Mono.just(school));
            }).switchIfEmpty(Mono.just(school));
        }).flatMap(school -> {
            log.info("first step - delete photo");
            return fileService.deleteById(school.getPhotoId()).flatMap(minioFile -> {
                log.info("found photo");
                return minioService.delete(minioFile).then(Mono.just(school));
            }).switchIfEmpty(Mono.just(school));
        }).flatMap(school -> {
            log.info("school has been deleted [{}]",school);
            return Mono.just(
                    Rendering.redirectTo("/school/list?page=0&size=12&search=all").build()
            );
        });
    }
}
