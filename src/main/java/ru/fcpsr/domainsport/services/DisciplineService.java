package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fcpsr.domainsport.repositories.DisciplineRepository;

@Service
@RequiredArgsConstructor
public class DisciplineService {
    private final DisciplineRepository repository;
}
