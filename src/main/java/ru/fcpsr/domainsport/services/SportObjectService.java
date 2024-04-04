package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.fcpsr.domainsport.repositories.SportObjectRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportObjectService {
    private final SportObjectRepository sportObjectRepository;
}
