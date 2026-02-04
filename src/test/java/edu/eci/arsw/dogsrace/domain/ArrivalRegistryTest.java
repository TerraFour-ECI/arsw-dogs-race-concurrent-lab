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
}
