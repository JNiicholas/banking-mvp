package com.example.banking.concurrency.service;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.exception.BadRequestException;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.banking.keycloak.service.KeycloakProvisioningService;

@SpringBootTest(properties = {
        "keycloak.base-url=http://localhost:8081",
        "keycloak.realm=BankingApp",
        "keycloak.admin.client-id=BankingAppBackend",
        "keycloak.admin.client-secret=test-secret"
})
@ActiveProfiles("test")
class ConcurrencyServiceTest {


    @Autowired private AccountService accountService;  // your service impl via interface
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;

    @MockitoBean
    private KeycloakProvisioningService keycloakProvisioningService;

    private UUID accountId;
    private UUID customerId;
    private UUID callerExternalId;
    private String callerRealm;
    private static final Logger log = LoggerFactory.getLogger(ConcurrencyServiceTest.class);
    private String runId;

    @BeforeEach
    void setUp() {

        runId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] setUp: creating customer and account", runId);

        callerExternalId = UUID.randomUUID();
        callerRealm = "BankingApp";
        var customer = customerRepository.save(
                CustomerEntity.builder()
                        .firstName("Concurrency")
                        .lastName("Test")
                        .email("concurrency+" + UUID.randomUUID() + "@test.local")
                        .externalAuthId(callerExternalId)
                        .externalAuthRealm(callerRealm)
                        .build());
        customerId = customer.getId();

        Account created = accountService.createAccount(new CreateAccountRequest(customer.getId()));
        accountId = created.getId();

        log.info("[{}] Created accountId={} for customerId={}", runId, accountId, customerId);

    }

    // --- Helper to run tasks in parallel and wait for them ---
    private static void runConcurrently(int threads, Runnable barrierTask) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setName("concurrency-test-" + t.getId());
            return t;
        });
        CyclicBarrier barrier = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                String tn = Thread.currentThread().getName();
                try {
                    LoggerFactory.getLogger(ConcurrencyServiceTest.class).info("[barrier] {} waiting", tn);
                    barrier.await(5, TimeUnit.SECONDS); // synchronize start
                    LoggerFactory.getLogger(ConcurrencyServiceTest.class).info("[start ] {} running", tn);
                    barrierTask.run();
                    LoggerFactory.getLogger(ConcurrencyServiceTest.class).info("[done  ] {} finished ok", tn);
                } catch (Exception e) {
                    LoggerFactory.getLogger(ConcurrencyServiceTest.class).info("[error ] {} failed: {}", tn, e.toString());
                } finally {
                    done.countDown();
                }
            });
        }
        boolean completed = done.await(10, TimeUnit.SECONDS);
        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        LoggerFactory.getLogger(ConcurrencyServiceTest.class).info("[suite ] parallel run completed={} in {} ms", completed, durMs);
        pool.shutdownNow();
        assertTrue(completed, "Tasks did not complete in time");
    }

    @Test
    @DisplayName("Two concurrent deposits serialize and both succeed (pessimistic locking)")
    void concurrentDeposits_oneSuccessOneConflict() throws InterruptedException {
        BigDecimal delta = new BigDecimal("100.00");

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<Throwable> others = new CopyOnWriteArrayList<>();

        log.info("[{}] Starting two concurrent deposits of 100.00 on account {}", runId, accountId);

        runConcurrently(2, () -> {
            try {
                accountService.deposit(accountId, delta, callerExternalId, callerRealm);
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
        BigDecimal balance = accountService.getBalance(accountId, callerExternalId, callerRealm);
        assertEquals(0, balance.compareTo(new BigDecimal("200.0000")),
                "Final balance must reflect both successful writes under locking");

        log.info("[{}] Results: successes={}, conflicts={}, others={}", runId, successes.get(), conflicts.get(), others.size());
        log.info("[{}] Final balance after concurrent deposits: {}", runId, balance);
    }

    @Test
    @DisplayName("Many parallel deposits: total equals count of successes (no retry)")
    void manyParallelDeposits_totalsMatchSuccessCount() throws InterruptedException {
        int threads = 10;
        BigDecimal delta = new BigDecimal("10.00");

        AtomicInteger successes = new AtomicInteger();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        log.info("[{}] Starting {} parallel deposits of {} on account {}", runId, threads, delta, accountId);

        runConcurrently(threads, () -> {
            try {
                accountService.deposit(accountId, delta, callerExternalId, callerRealm);
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
        BigDecimal actual = accountService.getBalance(accountId, callerExternalId, callerRealm);
        assertEquals(0, expected.compareTo(actual),
                "Final balance must equal sum of successful deposits");

        log.info("[{}] successes={}, failures={} (stackErrors? {})", runId, successes.get(), failures.size(), failures.isEmpty() ? "no" : "yes");
        log.info("[{}] expected={}, actual={}", runId, expected, actual);
    }

    @Test
    @DisplayName("Concurrent withdraw from just-enough balance: one succeeds, one fails business rule")
    void concurrentWithdrawals_withInsufficientAfterFirst() {
        // Seed: deposit 100.00 first (single-thread)
        log.info("[{}] Seeding balance with 100.00 for account {}", runId, accountId);
        accountService.deposit(accountId, new BigDecimal("100.00"), callerExternalId, callerRealm);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        Runnable withdraw100 = () -> {
            try {
                accountService.withdraw(accountId, new BigDecimal("100.00"), callerExternalId, callerRealm);
                success.incrementAndGet();
            } catch (BadRequestException e) {
                // “insufficient funds” (business rule)
                businessFail.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                // in case both managed to load same version
                conflicts.incrementAndGet();
            }
        };

        log.info("[{}] Running two concurrent withdrawals of 100.00", runId);
        // Run two withdraw(100) in parallel against balance=100
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> runConcurrently(2, withdraw100));

        // Exactly one should succeed; the other should fail (either business or lock conflict)
        assertEquals(1, success.get(), "Exactly one withdrawal should succeed");
        assertEquals(1, businessFail.get() + conflicts.get(),
                "The other should fail by business rule or conflict");

        log.info("[{}] withdraw results: success={}, businessFail={}, conflicts={}", runId, success.get(), businessFail.get(), conflicts.get());

        // Final balance is either 0.0000 (if second failed business) or 0.0000 (conflict still yields one success)
        BigDecimal actual = accountService.getBalance(accountId, callerExternalId, callerRealm);
        assertEquals(0, actual.compareTo(new BigDecimal("0.0000")));
        log.info("[{}] Final balance after concurrent withdrawals: {}", runId, actual);
    }

    @Test
    @DisplayName("Concurrent overdraft attempts: no negative balance, only one success")
    void concurrentWithdrawals_overdraft_noNegativeBalance() throws InterruptedException {
        // Seed the account with a known balance of 100.00
        log.info("[{}] Seeding balance with 100.00 for account {}", runId, accountId);
        accountService.deposit(accountId, new BigDecimal("100.00"), callerExternalId, callerRealm);

        int threads = 8; // many concurrent attempts
        AtomicInteger success = new AtomicInteger();
        AtomicInteger businessFail = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        java.util.concurrent.CopyOnWriteArrayList<Throwable> others = new java.util.concurrent.CopyOnWriteArrayList<>();

        Runnable withdraw100 = () -> {
            try {
                accountService.withdraw(accountId, new BigDecimal("100.00"), callerExternalId, callerRealm);
                success.incrementAndGet();
            } catch (BadRequestException e) {
                log.info("[{}] overdraft blocked: {}", runId, e.getMessage());
                businessFail.incrementAndGet(); // insufficient funds path
            } catch (OptimisticLockingFailureException e) {
                conflicts.incrementAndGet(); // shouldn't happen with pessimistic locking
            } catch (Throwable t) {
                others.add(t);
            }
        };

        log.info("[{}] Running {} concurrent withdrawals of 100.00 (overdraft scenario)", runId, threads);
        runConcurrently(threads, withdraw100);

        // Exactly one withdrawal should succeed; the rest must fail by business rule (or conflict in non-locking variants)
        assertEquals(1, success.get(), "Exactly one withdrawal should succeed from a 100.00 balance");
        assertEquals(threads - 1, businessFail.get() + conflicts.get(), "All other withdrawals must fail");
        assertTrue(others.isEmpty(), "Unexpected exceptions: " + others);

        // Final balance must never be negative; with one success it must be 0.0000
        BigDecimal finalBalance = accountService.getBalance(accountId, callerExternalId, callerRealm);
        log.info("[{}] Final balance after overdraft concurrency test: {}", runId, finalBalance);
        assertEquals(0, finalBalance.compareTo(new BigDecimal("0.0000")), "Final balance must be 0.0000 (no negative balances)");
    }

    @AfterEach
    void tearDown() {

        log.info("[{}] tearDown: cleaning test data (accountId={}, customerId={})", runId, accountId, customerId);
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
        log.info("[{}] tearDown: done", runId);
    }
}