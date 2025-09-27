package com.example.banking;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.banking.keycloak.service.KeycloakProvisioningService;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.service.api.AccountService;
import com.example.banking.service.api.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "keycloak.base-url=http://localhost:8081",
        "keycloak.realm=BankingApp",
        "keycloak.admin.client-id=BankingAppBackend",
        "keycloak.admin.client-secret=test-secret"
})
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
class BankingMvpApplicationTests {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @MockitoBean
    private KeycloakProvisioningService keycloakProvisioningService;

    @Test
    void depositAndWithdrawFlow() {
        // create customer
        CreateCustomerRequest cr = CreateCustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .build();

        String kcUserId = UUID.randomUUID().toString();
        when(keycloakProvisioningService.createUser(any())).thenReturn(kcUserId);
        doNothing().when(keycloakProvisioningService).setUserPassword(eq(kcUserId), anyString(), anyBoolean());


        Customer c = customerService.createCustomer(cr);


        // create account
        CreateAccountRequest ar = new CreateAccountRequest(c.getId());
        Account a = accountService.createAccount(ar);

        // caller identity for new AccountService methods
        UUID callerExternalId = c.getExternalAuthId();
        String callerRealm = "BankingApp";

        // deposit 100
        accountService.deposit(a.getId(), new BigDecimal("100.00"), callerExternalId, callerRealm);
        assertEquals(0, accountService.getBalance(a.getId(), callerExternalId, callerRealm)
                .compareTo(new BigDecimal("100.00")));

        // withdraw 50
        accountService.withdraw(a.getId(), new BigDecimal("50.00"), callerExternalId, callerRealm);
        assertEquals(0, accountService.getBalance(a.getId(), callerExternalId, callerRealm)
                .compareTo(new BigDecimal("50.00")));
    }
}
