package com.example.banking.repository;

import com.example.banking.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepository {
    private final Map<UUID, Customer> store = new ConcurrentHashMap<>();

    public Customer save(Customer c) {
        store.put(c.getId(), c);
        return c;
    }

    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
