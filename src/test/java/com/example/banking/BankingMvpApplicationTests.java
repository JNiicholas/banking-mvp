package com.example.banking;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.service.impl.AccountServiceImpl;
import com.example.banking.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BankingMvpApplicationTests {

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @Autowired
    private AccountServiceImpl accountServiceImpl;

    @Test
    void depositAndWithdrawFlow() {
        // create customer
        CreateCustomerRequest cr = new CreateCustomerRequest("Alice", "alice@example.com");
        Customer c = customerServiceImpl.createCustomer(cr);

        // create account
        CreateAccountRequest ar = new CreateAccountRequest(c.getId());
        Account a = accountServiceImpl.createAccount(ar);

        // deposit 100
        accountServiceImpl.deposit(a.getId(), new BigDecimal("100.00"));
        assertEquals(0, accountServiceImpl.getBalance(a.getId())
                .compareTo(new BigDecimal("100.00")));

        // withdraw 50
        accountServiceImpl.withdraw(a.getId(), new BigDecimal("50.00"));
        assertEquals(0, accountServiceImpl.getBalance(a.getId())
                .compareTo(new BigDecimal("50.00")));
    }
}
