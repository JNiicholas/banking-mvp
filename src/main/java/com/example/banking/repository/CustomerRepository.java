package com.example.banking.repository;


import com.example.banking.entity.CustomerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    @EntityGraph(attributePaths = "accounts")
    @Query("select distinct c from CustomerEntity c")
    List<CustomerEntity> findAllWithAccounts();
    Optional<CustomerEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<CustomerEntity> findByExternalAuthIdAndExternalAuthRealm(UUID externalAuthId, String externalAuthRealm);
}
