package com.example.banking.repository;

import com.example.banking.model.Account;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {
    private final Map<UUID, Account> store = new ConcurrentHashMap<>();

    public Account save(Account a) {
        store.put(a.getId(), a);
        return a;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
