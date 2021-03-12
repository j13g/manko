package de.j13g.manko.core.rounds;

import de.j13g.manko.core.TestEntrant;
import de.j13g.manko.core.exceptions.InvalidEntrantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SemiFinalTest extends RoundTest {

    private final TestEntrant invalidEntrant = createEntrant();

    private SemiFinal<TestEntrant> newSemiFinal;

    @BeforeEach
    void init() {
        newSemiFinal = new SemiFinal<>(first, second, third, fourth);
    }

    @Test
    void newSemiFinal_addInvalidEntrant_throwsInvalidEntrantException() {
        assertThrows(InvalidEntrantException.class, () -> newSemiFinal.addEntrant(invalidEntrant));
    }

    @Test
    void newSemiFinal_resetEntrant_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> newSemiFinal.resetEntrant(first));
    }
}
