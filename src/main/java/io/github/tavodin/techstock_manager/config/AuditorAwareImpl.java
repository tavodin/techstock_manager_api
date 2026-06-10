package io.github.tavodin.techstock_manager.config;

import io.github.tavodin.techstock_manager.entities.User;
import io.github.tavodin.techstock_manager.repositories.UserRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {

    private final UserRepository userRepository;

    public AuditorAwareImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getCurrentAuditor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username);
    }
}