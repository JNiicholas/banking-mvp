package com.example.banking.repository;


import com.example.banking.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id = :id")
    Optional<AccountEntity> findByIdForUpdate(@Param("id") UUID id);

    // For listing a customer’s accounts
    List<AccountEntity> findByCustomerId(UUID customerId);

    boolean existsByIdAndCustomer_Id(UUID id, UUID customerId);

    Optional<AccountEntity> findByIbanNormalized(String ibanNormalized);
    boolean existsByIbanNormalized(String ibanNormalized);
}