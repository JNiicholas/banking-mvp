package com.example.banking.repository;

import com.example.banking.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    @Query("""
           select t from TransactionEntity t
           where t.accountId = :accountId
           order by t.timestamp desc, t.id desc
           """)
    List<TransactionEntity> findRecentByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from TransactionEntity t where t.accountId = :accountId")
    int deleteByAccountId(@Param("accountId") UUID accountId);
}