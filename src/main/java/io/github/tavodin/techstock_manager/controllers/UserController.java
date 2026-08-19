package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.MenuItemDTO;
import io.github.tavodin.techstock_manager.dto.UserMenuDTO;
import io.github.tavodin.techstock_manager.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public Map<String, String> me() {
        return service.me();
    }

    @GetMapping("/menu")
    public List<UserMenuDTO> getMenu() {
        return service.getMenu();
    }
}
