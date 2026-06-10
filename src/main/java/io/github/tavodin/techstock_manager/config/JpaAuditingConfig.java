package io.github.tavodin.techstock_manager.config;

import io.github.tavodin.techstock_manager.entities.User;
import io.github.tavodin.techstock_manager.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<User> auditorAware(UserRepository userRepository) {
        return new AuditorAwareImpl(userRepository);
    }
}