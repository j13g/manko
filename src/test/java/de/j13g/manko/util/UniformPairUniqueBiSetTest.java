package de.j13g.manko.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UniformPairUniqueBiSetTest {

    private final Integer first = 1;
    private final Integer second = 2;
    private final Integer third = 3;

    private final UniformPair<Integer> pair = createUniformPair(first, second);
    private final UniformPair<Integer> nonUniquePair = createUniformPair(first, third);

    private UniformPairUniqueBiSet<Integer, UniformPair<Integer>> set;
    private UniformPairUniqueBiSet<Integer, UniformPair<Integer>> singleElementSet;

    @BeforeEach
    void init() {
        set = new UniformPairUniqueBiSet<>();
        singleElementSet = new UniformPairUniqueBiSet<>();
        Assertions.assertDoesNotThrow(() -> singleElementSet.add(pair));
    }

    @Test
    void singlePair_addNonUniquePair_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> singleElementSet.add(nonUniquePair));
    }

    @Test
    void singlePair_removePair_isEmpty() {
        singleElementSet.remove(pair);
        assertTrue(set.isEmpty());
    }

    @Test
    void singlePair_findByFirst_returnsPair() {
        assertEquals(pair, singleElementSet.findByElement(first));
    }

    private <E> UniformPair<E> createUniformPair(E a, E b) {
        return new UniformPair<>(a, b);
    }
}
