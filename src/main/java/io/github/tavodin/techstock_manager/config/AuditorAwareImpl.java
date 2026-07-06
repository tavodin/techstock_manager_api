package io.github.tavodin.techstock_manager.config;

import io.github.tavodin.techstock_manager.config.security.UserDetailsImp;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        UserDetailsImp user =
                (UserDetailsImp) authentication.getPrincipal();

        return Optional.of(user.getId());
    }
}