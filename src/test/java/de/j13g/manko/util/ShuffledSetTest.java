package de.j13g.manko.util;

import de.j13g.manko.util.exceptions.EmptySetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.j13g.manko.Helper.assertSerializable;
import static de.j13g.manko.Helper.assertSuppliesAll;
import static org.junit.jupiter.api.Assertions.*;

public class ShuffledSetTest {

    private final Random random = new Random();

    private final Integer element = 1;
    private final List<Integer> elementList = Arrays.asList(element, 2, 3, 4, 5, 6, 7, 8, 9);
    private final Set<Integer> elements = new HashSet<>(elementList);

    private ShuffledSet<Integer> singleElementSet;
    private ShuffledSet<Integer> multiElementSet;

    @BeforeEach
    void init() {
        singleElementSet = new ShuffledSet<>();
        singleElementSet.add(element);
        multiElementSet = new ShuffledSet<>(elements);
    }

    @Test
    void removeElement_removeRandomElement_throwsEmptySetException() {
        singleElementSet.remove(element);
        assertThrows(EmptySetException.class, singleElementSet::removeRandom);
    }

    @Test
    void removeAllElements_removeRandomElement_throwsEmptySetException() {
        elements.forEach(e -> multiElementSet.remove(e));
        assertThrows(EmptySetException.class, multiElementSet::removeRandom);
    }

    @Test
    void removeThenAddElement_removeRandomElements_getSingleElement() {
        singleElementSet.remove(element);
        singleElementSet.add(element);
        assertDoesNotThrow(singleElementSet::removeRandom);
        assertThrows(EmptySetException.class, singleElementSet::removeRandom);
    }

    @Test
    void singleElementSet_removeRandom_isEmpty() {
        assertDoesNotThrow(singleElementSet::removeRandom);
        assertTrue(singleElementSet.isEmpty());
    }

    @Test
    void removeElements_getRemainingRandomElements_removedElementsAreNotReturned() {
        HashSet<Integer> removed = new HashSet<>();

        // Remove at most half of the elements.
        int n = multiElementSet.size() / 2;
        for (int i = 0; i < n; ++i) {
            // It's okay to remove an element multiple times.
            // We will just remove less elements in total.
            // Both the chosen elements and the number of elements are varied by this.
            int index = random.nextInt(multiElementSet.size());
            int element = elementList.get(index);
            multiElementSet.remove(element);
            removed.add(element);
        }

        int remaining = multiElementSet.size();
        for (int i = 0; i < remaining; ++i) {
            assertDoesNotThrow(() -> {
                Integer element = multiElementSet.removeRandom();
                assertFalse(removed.contains(element));
            });
        }
    }

    @Test
    void multipleElements_removeRandom_isRandom() {
        assertSuppliesAll(elements, () -> {
            ShuffledSet<Integer> set = new ShuffledSet<>(elements);
            return assertDoesNotThrow(set::removeRandom);
        });
    }

    @Test
    void shuffledSet_isSerializable() {
        multiElementSet.remove(element);
        assertSerializable(multiElementSet, ShuffledSet.class);
    }
}
