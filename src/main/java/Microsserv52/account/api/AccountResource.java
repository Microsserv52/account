package Microsserv52.account.api;

import java.util.List;
import java.util.UUID;

import Microsserv52.account.model.Account;
import Microsserv52.account.model.AccountRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/accounts")
public class AccountResource {
    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String internalServiceSecret;

    public AccountResource(AccountRepository repository, PasswordEncoder passwordEncoder,
                           @Value("${platform.internal-service-secret}") String internalServiceSecret) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.internalServiceSecret = internalServiceSecret;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountOut create(@Valid @RequestBody AccountIn input) {
        if (repository.findByEmailIgnoreCase(input.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        Account account = new Account(UUID.randomUUID(), input.name(), input.email().toLowerCase(),
            passwordEncoder.encode(input.password()), "ROLE_USER", true);
        return toOutput(repository.save(account));
    }

    @GetMapping
    public List<AccountOut> findAll() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    @GetMapping("/{id}")
    public AccountOut findById(@PathVariable UUID id) {
        return repository.findById(id).map(this::toOutput)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    @GetMapping("/internal/credentials")
    public CredentialsOut credentials(@RequestParam String email,
                                     @RequestHeader(value = "X-Internal-Service", required = false) String secret) {
        if (!internalServiceSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal service access required");
        }
        return repository.findByEmailIgnoreCase(email).map(account ->
            new CredentialsOut(account.getId(), account.getEmail(), account.getPasswordHash(),
                account.getRoles(), account.isEnabled()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AccountOut toOutput(Account account) {
        return new AccountOut(account.getId(), account.getName(), account.getEmail(),
            account.getRoles(), account.isEnabled());
    }
}
