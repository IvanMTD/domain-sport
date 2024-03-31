package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fcpsr.domainsport.repositories.AuthTokenRepository;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private final AuthTokenRepository authTokenRepository;
}
