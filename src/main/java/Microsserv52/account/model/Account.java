package Microsserv52.account.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String roles;
    @Column(nullable = false)
    private boolean enabled;

    protected Account() {}

    public Account(UUID id, String name, String email, String passwordHash, String roles, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.enabled = enabled;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRoles() { return roles; }
    public boolean isEnabled() { return enabled; }
}
