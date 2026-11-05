package Microsserv52.account.api;

import java.util.UUID;

public record CredentialsOut(UUID id, String email, String passwordHash, String roles, boolean enabled) {}
