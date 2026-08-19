package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.config.security.UserDetailsImp;
import io.github.tavodin.techstock_manager.dto.MenuItemDTO;
import io.github.tavodin.techstock_manager.dto.MenuProjection;
import io.github.tavodin.techstock_manager.dto.UserMenuDTO;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> me() {
        UserDetailsImp user = getAuthenticatedUser();

        String name = repository.getNameByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Map.of("name", name);
    }

    @Transactional(readOnly = true)
    public List<UserMenuDTO> getMenu() {
        UserDetailsImp user = getAuthenticatedUser();

        Collection<String> permissions = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toList();

        List<MenuProjection> projections = repository.getMenuItemByPermissions(permissions);

        Map<String, UserMenuDTO> menus = new LinkedHashMap<>();

        for (MenuProjection projection : projections) {

            UserMenuDTO menu = menus.computeIfAbsent(
                    projection.getMenu(),
                    key -> new UserMenuDTO(key, new ArrayList<>())
            );

            menu.getMenuItem().add(
                    new MenuItemDTO(
                            projection.getName(),
                            projection.getLink()
                    )
            );
        }

        return new ArrayList<>(menus.values());
    }

    private UserDetailsImp getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return (UserDetailsImp) auth.getPrincipal();
    }
}
