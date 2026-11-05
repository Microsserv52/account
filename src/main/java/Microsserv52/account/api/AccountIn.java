package Microsserv52.account.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountIn(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password
) {}
