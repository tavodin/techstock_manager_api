package io.github.tavodin.techstock_manager.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDetailsImp implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    private Set<String> roles;

    public UserDetailsImp(Long id, String username, String password, Boolean enabled, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
