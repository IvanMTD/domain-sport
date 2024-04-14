package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.models.News;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class NewsDTO {
    private long id;
    private long authorId;

    @NotBlank(message = "Новостной заголовок не может быть пустым")
    @Size(min = 8, max = 128, message = "Заголовок должно состоять минимум из 8 знаков, а как максимум из 128 знаков")
    private String title;
    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 64, max = 256, message = "Аннотация должна состоять минимум из 64 знаков, а как максимум из 256 знаков")
    private String annotation;
    @NotBlank(message = "Новостное поле не может быть пустым")
    private String content;
    private LocalDate placedAt;

    public NewsDTO(News news){
        setId(news.getId());
        setAuthorId(news.getAuthorId());
        setTitle(news.getTitle());
        setAnnotation(news.getAnnotation());
        setContent(news.getContent());
        setPlacedAt(news.getPlacedAt());
    }

    public String getData(){
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(placedAt);
    }
}
