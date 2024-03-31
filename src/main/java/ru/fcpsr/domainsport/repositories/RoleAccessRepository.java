package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.fcpsr.domainsport.models.RoleAccess;

public interface RoleAccessRepository extends ReactiveCrudRepository<RoleAccess,Long> {
}
