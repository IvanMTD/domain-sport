package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.AppUserDTO;
import ru.fcpsr.domainsport.dto.ObjectAccessDTO;
import ru.fcpsr.domainsport.dto.RoleAccessDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.enums.Access;
import ru.fcpsr.domainsport.enums.Permission;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.models.AuthToken;
import ru.fcpsr.domainsport.models.RoleAccess;
import ru.fcpsr.domainsport.repositories.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Service("AccessService")
@RequiredArgsConstructor
public class AccessService {
    private final SportRepository sportRepository;
    private final AppUserRepository userRepository;
    private final RoleAccessRepository roleAccessRepository;
    private final ObjectAccessRepository objectAccessRepository;
    private final EkpRepository ekpRepository;

    public Mono<Boolean> getAccess(Authentication authentication, String accessString, String permissionString){
        if(authentication != null) {
            Permission permission = Permission.valueOf(permissionString);
            Access access = Access.valueOf(accessString);
            AppUser appUser = (AppUser) authentication.getPrincipal();
            return userRepository.findById(appUser.getId()).flatMap(user -> {
                if (user.getRole().equals(Role.ADMIN)) {
                    return Mono.just(true);
                } else {
                    if(user.getRole().equals(Role.MODERATOR)){
                        return roleAccessRepository.findAllByIdIn(user.getRoleAccessIds()).collectList().flatMap(list -> {
                            if(list.stream().anyMatch(roleAccess -> roleAccess.getAccess().equals(access))){
                                return Mono.just(true);
                            }else{
                                return Mono.just(false);
                            }
                        }).switchIfEmpty(Mono.just(false));
                    }else{
                        return roleAccessRepository.findAllByIdIn(user.getRoleAccessIds()).collectList().flatMap(list -> {
                            if(list.stream().anyMatch(roleAccess -> roleAccess.getAccess().equals(access))){
                                if(list.stream().anyMatch(roleAccess -> roleAccess.getPermissionList().stream().anyMatch(userPermission -> userPermission.equals(permission)))){
                                    return Mono.just(true);
                                }else{
                                    return Mono.just(false);
                                }
                            }else{
                                return Mono.just(false);
                            }
                        }).switchIfEmpty(Mono.just(false));
                    }
                }
            });
        }else{
            return Mono.just(false);
        }
    }

    public Mono<Boolean> getEkpAccess(Authentication authentication, String permissionString, long ekpId){
        if(authentication != null) {
            AppUser appUser = (AppUser) authentication.getPrincipal();
            Permission permission = Permission.valueOf(permissionString);

            return userRepository.findById(appUser.getId()).flatMap(user -> {
                if(user.getRole().equals(Role.ADMIN)){
                    return Mono.just(true);
                }else{
                    if(user.getRole().equals(Role.MODERATOR)){
                        return roleAccessRepository.findAllByIdIn(user.getRoleAccessIds()).collectList().flatMap(list -> {
                            if(list.stream().anyMatch(roleAccess -> roleAccess.getAccess().equals(Access.EVENT))){
                                return Mono.just(true);
                            }else{
                                return Mono.just(false);
                            }
                        }).switchIfEmpty(Mono.just(false));
                    }else{
                        return roleAccessRepository.findAllByIdIn(user.getRoleAccessIds()).collectList().flatMap(list -> {
                            return ekpRepository.findById(ekpId).flatMap(ekp -> {
                                if(list.stream().anyMatch(roleAccess -> roleAccess.getGroupId() == ekp.getSportId())){
                                    if(list.stream().anyMatch(roleAccess -> roleAccess.getPermissionList().stream().anyMatch(p -> p.equals(permission)))){
                                        return Mono.just(true);
                                    }else{
                                        return Mono.just(false);
                                    }
                                }else{
                                    return Mono.just(false);
                                }
                            });
                        }).switchIfEmpty(Mono.just(false));
                    }
                }
            });
        }else{
            return Mono.just(false);
        }
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
        return Mono.just(new AppUserDTO(user)).flatMap(userDTO -> {
            log.info("A transfer object has been received: [{}]", userDTO);
            return roleAccessRepository.findAllByIdIn(user.getRoleAccessIds()).flatMap(roleAccess -> {
                RoleAccessDTO roleAccessDTO = new RoleAccessDTO(roleAccess);
                return objectAccessRepository.findAllByIdIn(roleAccess.getObjectAccessIds()).flatMap(objectAccess -> {
                    ObjectAccessDTO objectAccessDTO = new ObjectAccessDTO(objectAccess);
                    return Mono.just(objectAccessDTO);
                }).collectList().flatMap(list -> {
                    roleAccessDTO.setObjectAccess(list);
                    return Mono.just(roleAccessDTO);
                }).switchIfEmpty(Mono.empty());
            }).flatMap(roleAccessDTO -> sportRepository.findById(roleAccessDTO.getGroupId()).flatMap(sport -> {
                SportDTO sportDTO = new SportDTO(sport);
                roleAccessDTO.setSport(sportDTO);
                return Mono.just(roleAccessDTO);
            }).switchIfEmpty(Mono.just(roleAccessDTO))).collectList().flatMap(list -> {
                userDTO.setRoleAccessList(list);
                return Mono.just(userDTO);
            }).switchIfEmpty(Mono.empty());
        });
    }

    public Mono<RoleAccess> createAccess(AuthToken token, long userId, List<Permission> permissions) {
        return Mono.just(new RoleAccess()).flatMap(roleAccess -> {
            roleAccess.setUserId(userId);
            roleAccess.setGroupId(token.getSportId());
            roleAccess.setAccess(token.getAccess());
            roleAccess.setPermissions(permissions);
            return roleAccessRepository.save(roleAccess);
        });
    }

    public Flux<RoleAccessDTO> getAllRoleAccessDTO(Set<Long> ids){
        return roleAccessRepository.findAllByIdIn(ids).flatMap(this::getRoleAccessDTO).switchIfEmpty(Mono.empty());
    }

    private Mono<RoleAccessDTO> getRoleAccessDTO(RoleAccess roleAccess) {
        return Mono.just(roleAccess).flatMap(ra -> {
            RoleAccessDTO roleAccessDTO = new RoleAccessDTO(ra);
            return objectAccessRepository.findAllByIdIn(roleAccess.getObjectAccessIds()).flatMap(objectAccess -> {
                ObjectAccessDTO objectAccessDTO = new ObjectAccessDTO(objectAccess);
                return Mono.just(objectAccessDTO);
            }).collectList().flatMap(oal -> {
                roleAccessDTO.setObjectAccess(oal);
                return sportRepository.findById(roleAccess.getGroupId()).flatMap(sport -> {
                    SportDTO sportDTO = new SportDTO(sport);
                    roleAccessDTO.setSport(sportDTO);
                    return Mono.just(roleAccessDTO);
                });
            }).defaultIfEmpty(roleAccessDTO);
        });
    }

    public Mono<RoleAccess> getRoleAccess(long roleAccessId) {
        return roleAccessRepository.findById(roleAccessId);
    }

    public Flux<RoleAccess> getRoleAccessList(Set<Long> ids){
        return roleAccessRepository.findAllByIdIn(ids);
    }
}
