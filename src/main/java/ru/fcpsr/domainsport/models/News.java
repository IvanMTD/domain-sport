package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ru.fcpsr.domainsport.dto.NewsDTO;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class News {
    @Id
    private long id;
    private long authorId;

    private String title;
    private String annotation;
    private String content;
    private LocalDate placedAt;

    public News(NewsDTO newsDTO){
        setId(newsDTO.getId());
        setAuthorId(newsDTO.getAuthorId());
        setTitle(newsDTO.getTitle());
        setAnnotation(newsDTO.getAnnotation());
        setContent(newsDTO.getContent());
        setPlacedAt(newsDTO.getPlacedAt());
    }
}
