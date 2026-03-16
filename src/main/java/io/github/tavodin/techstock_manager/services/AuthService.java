package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.dto.LoginDTO;
import io.github.tavodin.techstock_manager.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, String> login(LoginDTO login) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.username(), login.password()));

        UserDetails user = (UserDetails) authentication.getPrincipal();

        List<String> roles = user.getAuthorities().stream().map(role -> role.getAuthority()).toList();

        String token = jwtUtil.generateToken(user.getUsername(), Map.of("roles", roles));
        return Map.of("token", token);
    }
}
