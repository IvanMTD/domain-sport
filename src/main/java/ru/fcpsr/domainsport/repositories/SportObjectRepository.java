package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.fcpsr.domainsport.models.SportObject;

public interface SportObjectRepository extends ReactiveCrudRepository<SportObject,Long> {
}
