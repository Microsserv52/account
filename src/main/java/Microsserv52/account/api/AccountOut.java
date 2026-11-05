package Microsserv52.account.api;

import java.util.UUID;

public record AccountOut(UUID id, String name, String email, String roles, boolean enabled) {}
