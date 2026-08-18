package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank(message = "Username is required!")
        @Size(min = 5, max = 20, message = "Username must contain between {min} and {max} characters")
        String username,

        @NotBlank(message = "Password is required!")
        @Size(min = 5, max = 20, message = "Username must contain between {min} and {max} characters")
        String password
) {
}
