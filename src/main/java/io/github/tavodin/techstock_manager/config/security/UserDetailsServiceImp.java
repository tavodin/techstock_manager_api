package io.github.tavodin.techstock_manager.config.security;

import io.github.tavodin.techstock_manager.entities.Permission;
import io.github.tavodin.techstock_manager.entities.Role;
import io.github.tavodin.techstock_manager.entities.User;
import io.github.tavodin.techstock_manager.exceptions.IncorrectLogin;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectLogin("Incorrect username or password"));

        Set<String> authorities = user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        authorities.add(user.getRole().getName());

        return new UserDetailsImp(user.getId(), user.getUsername(), user.getPassword(), user.getEnabled(), authorities);
    }
}
