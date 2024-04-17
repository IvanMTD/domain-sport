package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.NewsDTO;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.models.News;
import ru.fcpsr.domainsport.repositories.NewsRepository;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    @CacheEvict(cacheNames = "news", allEntries = true)
    public Mono<News> createNews(NewsDTO newsDTO, AppUser user) {
        return Mono.just(new News(newsDTO)).flatMap(news -> {
            news.setPlacedAt(LocalDate.now());
            news.setAuthorId(user.getId());
            return newsRepository.save(news);
        }).flatMap(savedNews -> {
            log.info("NEWS HAS BEEN CREATED WITH ID {}", savedNews.getId());
            return Mono.just(savedNews);
        });
    }

    @Cacheable(cacheNames = "news")
    public Flux<News> getAllSortedById() {
        return newsRepository.findAllByOrderByIdDesc();
    }

    @Cacheable(cacheNames = "news")
    public Flux<News> getAllSortedById(Pageable pageable) {
        return newsRepository.findAllByOrderByIdDesc(pageable);
    }

    @Cacheable(cacheNames = "news")
    public Mono<News> getById(long newsId) {
        return newsRepository.findById(newsId);
    }

    @CacheEvict(cacheNames = "news", allEntries = true)
    public Mono<News> deleteById(long newsId) {
        return newsRepository.findById(newsId).flatMap(news -> newsRepository.deleteById(news.getId()).then(Mono.just(news)));
    }

    @CacheEvict(cacheNames = "news", allEntries = true)
    public Mono<News> updatedNews(NewsDTO newsDTO) {
        return newsRepository.findById(newsDTO.getId()).flatMap(news -> {
            news.setTitle(newsDTO.getTitle());
            news.setAnnotation(newsDTO.getAnnotation());
            news.setContent(newsDTO.getContent());
            return newsRepository.save(news);
        });
    }

    public Mono<Long> getCount() {
        return newsRepository.count();
    }
}
