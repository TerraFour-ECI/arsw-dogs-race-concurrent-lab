package edu.eci.arsw.dogsrace.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ArrivalRegistryTest {

    @Test
    void registerArrival_assignsUniquePositionsAndWinner() throws Exception {
        int n = 50;
        ArrivalRegistry registry = new ArrivalRegistry();

        // Use a pool with n threads so every submitted task can start and call
        // ready.countDown()
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);

        var futures = IntStream.range(0, n)
                .mapToObj(i -> pool.submit(() -> {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    return registry.registerArrival("dog-" + i);
                }))
                .toList();

        // Verify that all tasks were queued before waiting for them
        // to start.
        assertEquals(n, futures.size(), "All tasks must be submitted");
        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        Set<Integer> positions = futures.stream()
                .map(f -> {
                    try {
                        return f.get(5, TimeUnit.SECONDS).position();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

        pool.shutdownNow();

        assertEquals(n, positions.size(), "All positions must be unique");
        assertTrue(positions.contains(1), "There must be a first place");
        assertTrue(positions.contains(n), "There must be an n-th place");

        assertNotNull(registry.getWinner(), "Winner must be set");
        assertEquals(n + 1, registry.getNextPosition(), "Next position must be n+1");
    }

    // ---- Part II: Threshold / early-stop tests ----

    @Test
    void constructor_throwsOnInvalidThreshold() {
        assertThrows(IllegalArgumentException.class, () -> new ArrivalRegistry(0));
        assertThrows(IllegalArgumentException.class, () -> new ArrivalRegistry(-5));
    }

    @Test
    void hasReachedThreshold_falseBeforeEnoughArrivals() {
        ArrivalRegistry registry = new ArrivalRegistry(3);
        assertFalse(registry.hasReachedThreshold(), "Threshold not yet reached");

        registry.registerArrival("dog-A");
        assertFalse(registry.hasReachedThreshold(), "1 arrival < threshold 3");

        registry.registerArrival("dog-B");
        assertFalse(registry.hasReachedThreshold(), "2 arrivals < threshold 3");
    }

    @Test
    void hasReachedThreshold_trueOnceThresholdMet() {
        ArrivalRegistry registry = new ArrivalRegistry(3);

        registry.registerArrival("dog-A");
        registry.registerArrival("dog-B");
        registry.registerArrival("dog-C");

        assertTrue(registry.hasReachedThreshold(), "3 arrivals == threshold 3");
    }

    @Test
    void hasReachedThreshold_trueWhenExceeded() {
        ArrivalRegistry registry = new ArrivalRegistry(2);

        registry.registerArrival("dog-A");
        registry.registerArrival("dog-B");
        registry.registerArrival("dog-C"); // exceeds threshold

        assertTrue(registry.hasReachedThreshold(), "3 arrivals > threshold 2");
    }

    @Test
    void defaultConstructor_thresholdNeverReached() {
        ArrivalRegistry registry = new ArrivalRegistry(); // Integer.MAX_VALUE

        for (int i = 0; i < 100; i++) {
            registry.registerArrival("dog-" + i);
        }
        assertFalse(registry.hasReachedThreshold(),
                "Default threshold (MAX_VALUE) should not be reached with 100 arrivals");
    }

    @Test
    void concurrentArrivals_withThreshold_noRaceConditions() throws Exception {
        int n = 50;
        int threshold = 5;
        ArrivalRegistry registry = new ArrivalRegistry(threshold);

        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);

        var futures = IntStream.range(0, n)
                .mapToObj(i -> pool.submit(() -> {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    return registry.registerArrival("dog-" + i);
                }))
                .toList();

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        Set<Integer> positions = futures.stream()
                .map(f -> {
                    try {
                        return f.get(5, TimeUnit.SECONDS).position();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

        pool.shutdownNow();

        // All 50 threads registered — all positions must be unique
        assertEquals(n, positions.size(), "All positions must be unique (no race condition)");
        assertTrue(positions.contains(1), "There must be a first place");
        assertTrue(positions.contains(n), "There must be an n-th place");

        // Counter is consistent
        assertEquals(n + 1, registry.getNextPosition(), "Next position must be n+1");

        // Threshold must have been reached (50 arrivals >= 5 threshold)
        assertTrue(registry.hasReachedThreshold(),
                "Threshold must be reached after " + n + " arrivals");

        assertNotNull(registry.getWinner(), "Winner must be set");
    }

    @Test
    void earlyStopSimulation_threadsBreakWhenThresholdReached() throws Exception {
        int threshold = 3;
        int totalThreads = 10;
        int maxIterations = 100;
        ArrivalRegistry registry = new ArrivalRegistry(threshold);

        // Each thread iterates up to maxIterations, but checks the threshold
        // and breaks early — simulating the Galgo early-stop pattern.
        // Some threads will register an arrival; once threshold is met, others stop.
        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch start = new CountDownLatch(1);

        var futures = IntStream.range(0, totalThreads)
                .mapToObj(i -> pool.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    int iterations = 0;
                    for (int step = 0; step < maxIterations; step++) {
                        // Early-stop check (same as Galgo.corra())
                        if (registry.hasReachedThreshold()) break;
                        iterations++;
                        // Simulate some work, then register arrival at the end
                        if (step == maxIterations - 1) {
                            registry.registerArrival("dog-" + i);
                        }
                    }
                    return iterations;
                }))
                .toList();

        start.countDown();

        int totalIterations = 0;
        for (var f : futures) {
            totalIterations += f.get(5, TimeUnit.SECONDS);
        }

        pool.shutdownNow();

        // If there was no early stop, total would be totalThreads * maxIterations = 1000
        // With threshold=3, most threads should stop well before maxIterations
        assertTrue(totalIterations < totalThreads * maxIterations,
                "Total iterations (" + totalIterations + ") should be less than "
                + (totalThreads * maxIterations) + " due to early stop");
    }
}
