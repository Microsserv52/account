package Microsserv52.account;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import Microsserv52.account.model.Account;
import Microsserv52.account.model.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * LOCAL-ONLY administrator bootstrap. Seeded only when both ADMIN_EMAIL and
 * ADMIN_PASSWORD are set; never enables itself, and refuses to run in any
 * environment flagged as production (ENV/APP_ENV or a *prod SPRING_PROFILES_ACTIVE).
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final Set<String> PRODUCTION_MARKERS = Set.of("prod", "production");
    private static final String LOCAL_ADMIN_NAME = "Local Administrator";
    private static final String LOCAL_ADMIN_ROLES = "ROLE_ADMIN,ROLE_USER";

    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final Map<String, String> markers;

    public AdminBootstrap(AccountRepository accounts, PasswordEncoder passwordEncoder,
            @Value("${platform.admin-email:}") String adminEmail,
            @Value("${platform.admin-password:}") String adminPassword,
            @Value("${ENV:}") String env,
            @Value("${APP_ENV:}") String appEnv,
            @Value("${SPRING_PROFILES_ACTIVE:}") String activeProfiles) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim();
        this.adminPassword = adminPassword == null ? "" : adminPassword;
        this.markers = Map.of(
            "ENV", env == null ? "" : env,
            "APP_ENV", appEnv == null ? "" : appEnv,
            "SPRING_PROFILES_ACTIVE", activeProfiles == null ? "" : activeProfiles);
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) && !StringUtils.hasText(adminPassword)) {
            log.info("admin bootstrap disabled (ADMIN_EMAIL/ADMIN_PASSWORD unset); no administrator is seeded");
            return;
        }
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                "refusing to seed admin: ADMIN_EMAIL and ADMIN_PASSWORD must both be set or both be empty");
        }
        String production = productionMarker();
        if (production != null) {
            throw new IllegalStateException(
                "refusing to seed admin: " + production + " marks a production environment "
                    + "and the admin bootstrap is local-only");
        }
        String email = adminEmail.toLowerCase();
        accounts.findByEmailIgnoreCase(email).ifPresentOrElse(this::promote, () -> seed(email));
    }

    private String productionMarker() {
        for (Map.Entry<String, String> marker : markers.entrySet()) {
            boolean prod = Arrays.stream(marker.getValue().toLowerCase().split(","))
                .map(String::trim)
                .anyMatch(PRODUCTION_MARKERS::contains);
            if (prod) {
                return marker.getKey() + "='" + marker.getValue().trim() + "'";
            }
        }
        return null;
    }

    private void seed(String email) {
        Account created = new Account(UUID.randomUUID(), LOCAL_ADMIN_NAME, email,
            passwordEncoder.encode(adminPassword), LOCAL_ADMIN_ROLES, true);
        accounts.save(created);
        log.warn("admin bootstrap: created LOCAL-ONLY administrator '{}' (roles {}). This account exists for "
            + "local development only; production deployments must leave ADMIN_EMAIL/ADMIN_PASSWORD empty "
            + "and promote real accounts manually", email, LOCAL_ADMIN_ROLES);
    }

    private void promote(Account existing) {
        LinkedHashSet<String> roles = StringUtils.hasText(existing.getRoles())
            ? Arrays.stream(existing.getRoles().split(","))
                .map(String::trim).filter(r -> !r.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new))
            : new LinkedHashSet<>();
        if (roles.contains("ROLE_ADMIN")) {
            log.info("admin bootstrap: account '{}' already has ROLE_ADMIN; leaving it unchanged (password not modified)",
                existing.getEmail());
            return;
        }
        roles.add("ROLE_ADMIN");
        Account promoted = new Account(existing.getId(), existing.getName(), existing.getEmail(),
            existing.getPasswordHash(), String.join(",", roles), existing.isEnabled());
        accounts.save(promoted);
        log.warn("admin bootstrap: promoted existing account '{}' to ROLE_ADMIN (password left unchanged)",
            existing.getEmail());
    }
}
