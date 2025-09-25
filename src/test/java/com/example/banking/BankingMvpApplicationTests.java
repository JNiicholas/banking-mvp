package com.example.banking;

import com.example.banking.dto.AmountRequest;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BankingMvpApplicationTests {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Test
    void depositAndWithdrawFlow() {
        // create customer
        CreateCustomerRequest cr = new CreateCustomerRequest();
        cr.setName("Alice");
        cr.setEmail("alice@example.com");
        Customer c = customerService.createCustomer(cr);

        // create account
        CreateAccountRequest ar = new CreateAccountRequest();
        ar.setCustomerId(c.getId());
        Account a = accountService.createAccount(ar);

        // deposit 100
        accountService.deposit(a.getId(), new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), accountService.getBalance(a.getId()));

        // withdraw 50
        accountService.withdraw(a.getId(), new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), accountService.getBalance(a.getId()));
    }
}
