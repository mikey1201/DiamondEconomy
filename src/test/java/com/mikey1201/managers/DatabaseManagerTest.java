package com.mikey1201.managers;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private File dataFolder;
    private DatabaseManager db;

    @BeforeEach
    void setUp() throws Exception {
        dataFolder = Files.createTempDirectory("diamondeconomy-test").toFile();
        db = new DatabaseManager(dataFolder, Logger.getLogger("test"));
        db.connect();
        db.initialize();
    }

    @AfterEach
    void tearDown() {
        db.disconnect();
        deleteRecursively(dataFolder);
    }

    private static void deleteRecursively(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (File c : children) deleteRecursively(c);
        }
        f.delete();
    }

    @Test
    void setAndGetBalanceRoundTrip() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 100.0);
        assertEquals(100.0, db.getBalance(uuid));
    }

    @Test
    void getBalanceOfUnknownAccountIsZero() {
        assertEquals(0.0, db.getBalance(UUID.randomUUID()));
    }

    @Test
    void tryWithdrawInsufficientFundsReturnsNullAndKeepsBalance() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 100.0);
        assertNull(db.tryWithdraw(uuid, 150.0));
        assertEquals(100.0, db.getBalance(uuid));
    }

    @Test
    void tryWithdrawExactFundsSucceeds() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 100.0);
        Double result = db.tryWithdraw(uuid, 100.0);
        assertNotNull(result);
        assertEquals(0.0, result);
        assertEquals(0.0, db.getBalance(uuid));
    }

    @Test
    void tryWithdrawOnMissingAccountReturnsNull() {
        assertNull(db.tryWithdraw(UUID.randomUUID(), 1.0));
    }

    @Test
    void addToBalanceCreatesAccountWithDelta() {
        UUID uuid = UUID.randomUUID();
        double balance = db.addToBalance(uuid, "Bob", 5.0);
        assertEquals(5.0, balance);
        assertEquals(5.0, db.getBalance(uuid));
    }

    @Test
    void addToBalanceIncrements() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 3.0);
        double balance = db.addToBalance(uuid, "Alice", 3.0);
        assertEquals(6.0, balance);
    }

    @Test
    void concurrentWithdrawalsCannotDoubleSpend() throws Exception {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 100.0);

        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return db.tryWithdraw(uuid, 10.0) != null;
            }));
        }
        start.countDown();
        int successes = 0;
        for (Future<Boolean> f : futures) {
            if (f.get()) successes++;
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(10, successes, "exactly 10 of 20 withdrawals must succeed");
        assertEquals(0.0, db.getBalance(uuid), "balance must be exactly zero");
    }

    @Test
    void concurrentDepositsNeverLoseUpdates() throws Exception {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 0.0);

        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                db.addToBalance(uuid, "Alice", 10.0);
                return true;
            }));
        }
        start.countDown();
        for (Future<Boolean> f : futures) f.get();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(200.0, db.getBalance(uuid), "all 20 deposits must be applied");
    }

    @Test
    void nullNamesAreHandledForNonPlayerAccounts() {
        UUID towny = UUID.randomUUID();
        db.updateBalance(towny, null, 42.0);
        assertEquals(42.0, db.getBalance(towny));

        db.addToBalance(towny, null, 8.0);
        assertEquals(50.0, db.getBalance(towny));

        // Regression: balance changes must never rename an account (synthetic
        // names from UUID-based OfflinePlayer lookups used to clobber Towny
        // town/nation names via Vault deposits).
        db.updateBalance(towny, "Town-Something", 50.0);
        assertEquals(50.0, db.getBalance(towny));
        assertEquals(towny, db.getUUID("Town-Something"));

        db.addToBalance(towny, null, 1.0);
        assertEquals(towny, db.getUUID("Town-Something"), "null-name increment must preserve the stored name");
        db.addToBalance(towny, "SomeSyntheticName", 1.0);
        assertEquals(towny, db.getUUID("Town-Something"), "named increment must not rename an existing account");
        assertEquals(52.0, db.getBalance(towny));

        db.updateBalance(towny, null, 3.0);
        assertEquals(towny, db.getUUID("Town-Something"), "null-name update must preserve the stored name");
        assertEquals(3.0, db.getBalance(towny));
    }

    @Test
    void nameLookupIsCaseInsensitiveAndUpdates() {
        UUID uuid = UUID.randomUUID();
        db.updateBalance(uuid, "Alice", 10.0);
        assertEquals(uuid, db.getUUID("alice"));
        assertEquals(uuid, db.getUUID("ALICE"));

        db.updateBalance(uuid, "AliceNew", 10.0);
        assertNull(db.getUUID("Alice"));
        assertEquals(uuid, db.getUUID("AliceNew"));
    }

    @Test
    void unconnectedManagerThrowsDatabaseExceptionInsteadOfNpe() {
        DatabaseManager unconnected = new DatabaseManager(dataFolder, Logger.getLogger("test"));
        assertThrows(DatabaseException.class, () -> unconnected.getBalance(UUID.randomUUID()));
    }

    @Test
    void topBalancesWithNamesReturnsOrderedEntries() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        db.updateBalance(a, "Rich", 500.0);
        db.updateBalance(b, "Poor", 10.0);

        List<DatabaseManager.BalanceEntry> top = db.getTopBalancesWithNames(10);
        assertEquals(2, top.size());
        assertEquals("Rich", top.get(0).name());
        assertEquals(500.0, top.get(0).balance());
        assertEquals(a, top.get(0).uuid());
        assertEquals("Poor", top.get(1).name());
    }

    // ---- Round-on-write: no floating-point drift accumulation ----

    @Test
    void fractionalAdditionsDoNotAccumulateFloatDrift() {
        // The classic: 0.1 + 0.2 == 0.30000000000000004 in plain double math
        UUID uuid = UUID.randomUUID();
        db.addToBalance(uuid, "Alice", 0.1);
        db.addToBalance(uuid, "Alice", 0.2);
        assertEquals(0.3, db.getBalance(uuid), "0.1 + 0.2 must be stored as exactly 0.3");

        // 0.3 (carried) + 0.3 + 0.3 + 0.3 + 0.1 must be exactly 1.3
        db.addToBalance(uuid, "Alice", 0.3);
        db.addToBalance(uuid, "Alice", 0.3);
        db.addToBalance(uuid, "Alice", 0.3);
        db.addToBalance(uuid, "Alice", 0.1);
        assertEquals(1.3, db.getBalance(uuid));
    }

    @Test
    void thousandsOfTinyDepositsStayExact() {
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < 1000; i++) {
            db.addToBalance(uuid, "Alice", 0.1);
        }
        assertEquals(100.0, db.getBalance(uuid), "1000 x 0.1 must be exactly 100.0");
    }

    @Test
    void withdrawalsAreRoundedToTwoDecimals() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 10.0);
        for (int i = 0; i < 10; i++) {
            Double result = db.tryWithdraw(uuid, 0.3);
            assertNotNull(result);
        }
        assertEquals(7.0, db.getBalance(uuid), "10.0 - 10 x 0.3 must be exactly 7.0");
    }

    @Test
    void setBalanceRoundsToTwoDecimals() {
        UUID uuid = UUID.randomUUID();
        db.setBalance(uuid, 1.9999999999);
        assertEquals(2.0, db.getBalance(uuid));
        db.updateBalance(uuid, "Alice", 0.0050000001);
        assertEquals(0.01, db.getBalance(uuid));
    }
}
