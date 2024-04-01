package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.AppUserDTO;
import ru.fcpsr.domainsport.dto.ObjectAccessDTO;
import ru.fcpsr.domainsport.dto.RoleAccessDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.enums.Permission;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.models.AuthToken;
import ru.fcpsr.domainsport.models.RoleAccess;
import ru.fcpsr.domainsport.repositories.AppUserRepository;
import ru.fcpsr.domainsport.repositories.ObjectAccessRepository;
import ru.fcpsr.domainsport.repositories.RoleAccessRepository;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.util.List;

@Slf4j
@Service("AccessService")
@RequiredArgsConstructor
public class AccessService {
    private final SportRepository sportRepository;
    private final AppUserRepository userRepository;
    private final RoleAccessRepository roleAccessRepository;
    private final ObjectAccessRepository objectAccessRepository;

    public Mono<Boolean> getAccess(){
        return Mono.empty();
    }

    public Mono<Boolean> isAuthenticate(Authentication authentication){
        return Mono.just(authentication != null);
    }

    public Mono<Boolean> isAdmin(Authentication authentication){
        if(authentication != null){
            AppUser appUser = (AppUser) authentication.getPrincipal();
            if(appUser.getRole().equals(Role.ADMIN)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    public Mono<AppUserDTO> getCompletedUser(AppUser user) {
        return Mono.just(new AppUserDTO(user)).flatMap(userDTO -> roleAccessRepository.findById(user.getRoleAccessId()).flatMap(roleAccess -> {
            RoleAccessDTO roleAccessDTO = new RoleAccessDTO(roleAccess);
            return objectAccessRepository.findAllByIdIn(roleAccess.getObjectAccessIds()).flatMap(objectAccess -> {
                ObjectAccessDTO objectAccessDTO = new ObjectAccessDTO(objectAccess);
                return Mono.just(objectAccessDTO);
            }).collectList().flatMap(oal -> {
                roleAccessDTO.setObjectAccess(oal);
                return sportRepository.findById(roleAccess.getGroupId()).flatMap(sport -> {
                    SportDTO sportDTO = new SportDTO(sport);
                    roleAccessDTO.setSport(sportDTO);
                    userDTO.setRoleAccess(roleAccessDTO);
                    return Mono.just(userDTO);
                });
            });
        }).switchIfEmpty(Mono.just(userDTO)));
    }

    public Mono<RoleAccess> createAccess(AuthToken token, long userId, List<Permission> permissions) {
        return Mono.just(new RoleAccess()).flatMap(roleAccess -> {
            roleAccess.setUserId(userId);
            roleAccess.setGroupId(token.getSportId());
            roleAccess.setPermissions(permissions);
            return roleAccessRepository.save(roleAccess);
        });
    }
}
