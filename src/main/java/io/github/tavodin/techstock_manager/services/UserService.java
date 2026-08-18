package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.config.security.UserDetailsImp;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.UserRepository;
import io.github.tavodin.techstock_manager.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImp user = (UserDetailsImp) auth.getPrincipal();

        String name = repository.getNameByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Map.of("name", name);
    }
}
