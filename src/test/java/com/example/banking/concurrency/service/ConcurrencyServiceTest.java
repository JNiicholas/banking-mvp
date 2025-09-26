package com.example.banking.concurrency.service;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.exception.BadRequestException;
import com.example.banking.exception.NotFoundException;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.service.api.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrencyServiceTest {


    @Autowired private AccountService accountService;  // your service impl via interface
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;

    private UUID accountId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        // Create a customer
        var customer = customerRepository.save(
                CustomerEntity.builder()
                        .name("Concurrency Test")
                        .email("concurrency+" + UUID.randomUUID() + "@test.local")
                        .build());
        customerId = customer.getId();

        // Create an account via service (exercise business path)
        Account created = accountService.createAccount(new CreateAccountRequest(customer.getId()));
        accountId = created.getId();

        // Seed with a known balance (optional; here keep 0.0000)
    }

    // --- Helper to run tasks in parallel and wait for them ---
    private static void runConcurrently(int threads, Runnable barrierTask) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS); // synchronize start
                    barrierTask.run();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS), "Tasks did not complete in time");
        pool.shutdownNow();
    }

    @Test
    @DisplayName("Two concurrent deposits serialize and both succeed (pessimistic locking)")
    void concurrentDeposits_oneSuccessOneConflict() throws InterruptedException {
        BigDecimal delta = new BigDecimal("100.00");

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<Throwable> others = new CopyOnWriteArrayList<>();

        runConcurrently(2, () -> {
            try {
                accountService.deposit(accountId, delta);
                successes.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                conflicts.incrementAndGet();
            } catch (Throwable t) {
                others.add(t);
            }
        });

        // With pessimistic locking (findByIdForUpdate), both deposits serialize and succeed
        assertEquals(2, successes.get(), "Expected both deposits to succeed under pessimistic locking");
        assertEquals(0, conflicts.get(), "Did not expect optimistic lock conflicts with row-level locking");
        assertTrue(others.isEmpty(), "Unexpected exceptions: " + others);

        // Final balance must equal the sum of successful deltas (2 * 100.00 = 200.0000)
        BigDecimal balance = accountService.getBalance(accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("200.0000")),
                "Final balance must reflect both successful writes under locking");
    }

    @Test
    @DisplayName("Many parallel deposits: total equals count of successes (no retry)")
    void manyParallelDeposits_totalsMatchSuccessCount() throws InterruptedException {
        int threads = 10;
        BigDecimal delta = new BigDecimal("10.00");

        AtomicInteger successes = new AtomicInteger();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        runConcurrently(threads, () -> {
            try {
                accountService.deposit(accountId, delta);
                successes.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                // expected under contention
            } catch (Throwable t) {
                failures.add(t);
            }
        });

        assertTrue(failures.isEmpty(), "Unexpected failures: " + failures);
        BigDecimal expected = delta.multiply(BigDecimal.valueOf(successes.get()))
                .setScale(4); // match your DB scale
        BigDecimal actual = accountService.getBalance(accountId);
        assertEquals(0, expected.compareTo(actual),
                "Final balance must equal sum of successful deposits");
    }

    @Test
    @DisplayName("Concurrent withdraw from just-enough balance: one succeeds, one fails business rule")
    void concurrentWithdrawals_withInsufficientAfterFirst() {
        // Seed: deposit 100.00 first (single-thread)
        accountService.deposit(accountId, new BigDecimal("100.00"));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        Runnable withdraw100 = () -> {
            try {
                accountService.withdraw(accountId, new BigDecimal("100.00"));
                success.incrementAndGet();
            } catch (BadRequestException e) {
                // “insufficient funds” (business rule)
                businessFail.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                // in case both managed to load same version
                conflicts.incrementAndGet();
            }
        };

        // Run two withdraw(100) in parallel against balance=100
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> runConcurrently(2, withdraw100));

        // Exactly one should succeed; the other should fail (either business or lock conflict)
        assertEquals(1, success.get(), "Exactly one withdrawal should succeed");
        assertEquals(1, businessFail.get() + conflicts.get(),
                "The other should fail by business rule or conflict");

        // Final balance is either 0.0000 (if second failed business) or 0.0000 (conflict still yields one success)
        BigDecimal actual = accountService.getBalance(accountId);
        assertEquals(0, actual.compareTo(new BigDecimal("0.0000")));
    }

    @AfterEach
    void tearDown() {
        // Clean just what we created in this class (keeps your manual local data separate)
        if (accountId != null) {
            // delete child rows first to satisfy FK constraint
            transactionRepository.deleteByAccountId(accountId);
            accountRepository.findById(accountId).ifPresent(a -> accountRepository.deleteById(accountId));
        }
        if (customerId != null) {
            // delete the customer after its accounts are gone
            customerRepository.deleteById(customerId);
        }
    }
}