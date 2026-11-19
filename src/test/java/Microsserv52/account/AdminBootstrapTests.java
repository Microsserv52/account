package Microsserv52.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import Microsserv52.account.model.Account;
import Microsserv52.account.model.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AdminBootstrapTests {

    private static final String EMAIL = "Admin@Example.Test";
    private static final String PASSWORD = "local-dev-password";

    private AccountRepository accounts;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    private AdminBootstrap bootstrap(String email, String password, String env, String appEnv, String profiles) {
        return new AdminBootstrap(accounts, passwordEncoder, email, password, env, appEnv, profiles);
    }

    @Test
    void disabledWhenBothVarsEmpty() throws Exception {
        bootstrap("", "", "", "", "").run();
        verifyNoInteractions(accounts);
    }

    @Test
    void failsFastWhenOnlyOneVarSet() {
        assertThrows(IllegalStateException.class,
            () -> bootstrap(EMAIL, "", "", "", "").run());
        assertThrows(IllegalStateException.class,
            () -> bootstrap("", PASSWORD, "", "", "").run());
        verifyNoInteractions(accounts);
    }

    @Test
    void refusesToSeedWhenEnvMarksProduction() {
        AdminBootstrap bootstrap = bootstrap(EMAIL, PASSWORD, "production", "", "");
        IllegalStateException failure = assertThrows(IllegalStateException.class, bootstrap::run);
        assertTrue(failure.getMessage().contains("production"));
        verifyNoInteractions(accounts);
    }

    @Test
    void refusesToSeedWhenAppEnvMarksProduction() {
        assertThrows(IllegalStateException.class,
            () -> bootstrap(EMAIL, PASSWORD, "", "PROD", "").run());
        verifyNoInteractions(accounts);
    }

    @Test
    void refusesToSeedWhenActiveProfileMarksProduction() {
        assertThrows(IllegalStateException.class,
            () -> bootstrap(EMAIL, PASSWORD, "", "", "live,prod").run());
        verifyNoInteractions(accounts);
    }

    @Test
    void seedsLocalAdminWhenEnabledAndAbsent() throws Exception {
        when(accounts.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.empty());

        bootstrap(EMAIL, PASSWORD, "", "", "").run();

        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accounts).save(saved.capture());
        Account admin = saved.getValue();
        assertEquals("admin@example.test", admin.getEmail());
        assertEquals("ROLE_ADMIN,ROLE_USER", admin.getRoles());
        assertTrue(admin.isEnabled());
        assertTrue(passwordEncoder.matches(PASSWORD, admin.getPasswordHash()));
    }

    @Test
    void promotesExistingUserToAdminWithoutTouchingPassword() throws Exception {
        Account existing = new Account(UUID.randomUUID(), "Regular", "admin@example.test",
            "existing-password-hash", "ROLE_USER", true);
        when(accounts.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(existing));

        bootstrap(EMAIL, PASSWORD, "", "", "").run();

        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accounts).save(saved.capture());
        Account promoted = saved.getValue();
        assertEquals(existing.getId(), promoted.getId());
        assertEquals("existing-password-hash", promoted.getPasswordHash());
        assertEquals("ROLE_USER,ROLE_ADMIN", promoted.getRoles());
    }

    @Test
    void leavesExistingAdminUntouched() throws Exception {
        Account existing = new Account(UUID.randomUUID(), "Admin", "admin@example.test",
            "existing-password-hash", "ROLE_ADMIN,ROLE_USER", true);
        when(accounts.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(existing));

        bootstrap(EMAIL, PASSWORD, "", "", "").run();

        Mockito.verify(accounts, Mockito.never()).save(argThat(a -> true));
    }
}
