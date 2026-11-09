package Microsserv52.account;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.UUID;
import Microsserv52.account.model.Account;
import Microsserv52.account.model.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AccountAuthorizationTests {
    @Autowired WebApplicationContext context;
    @Autowired AccountRepository repository;
    MockMvc mvc;
    UUID userId;
    UUID adminId;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        repository.deleteAll(); userId = UUID.randomUUID(); adminId = UUID.randomUUID();
        repository.save(new Account(userId, "Fixture Visitor", "fixture@example.test", "not-a-real-password-hash", "ROLE_USER", true));
        repository.save(new Account(adminId, "Fixture Admin", "admin@example.test", "not-a-real-password-hash", "ROLE_ADMIN", true));
    }
    @Test void ownProfileIsAvailableWithoutListingOtherAccounts() throws Exception {
        mvc.perform(get("/accounts/me").with(jwt().jwt(token -> token.subject(userId.toString())))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("fixture@example.test")));
        mvc.perform(get("/accounts").with(jwt().jwt(token -> token.subject(userId.toString())))).andExpect(status().isForbidden());
        mvc.perform(get("/accounts/" + adminId).with(jwt().jwt(token -> token.subject(userId.toString())))).andExpect(status().isForbidden());
    }
    @Test void adminCanReadAccountsButCannotDeleteAnAdministrator() throws Exception {
        mvc.perform(get("/accounts").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk());
        mvc.perform(delete("/accounts/" + adminId).with(jwt().jwt(token -> token.subject(userId.toString())).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isConflict());
    }
    @Test void userCannotDeleteAccounts() throws Exception {
        mvc.perform(delete("/accounts/" + userId).with(jwt().jwt(token -> token.subject(userId.toString())))).andExpect(status().isForbidden());
    }
}
