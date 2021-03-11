package de.j13g.manko.core.rounds;

import de.j13g.manko.core.TestEntrant;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseRoundTest {

    protected final Random random = new Random(0);
    protected final HashSet<TestEntrant> testEntrants = new HashSet<>();

    protected final TestEntrant first = createEntrant();
    protected final TestEntrant second = createEntrant();
    protected final TestEntrant third = createEntrant();
    protected final TestEntrant invalidEntrant = createEntrant();

    protected final List<TestEntrant> entrants = Arrays.asList(
            first, second, third, createEntrant(),
            createEntrant(), createEntrant(), createEntrant(),
            createEntrant(), createEntrant(), createEntrant()
    );

    protected final TestEntrant winner = first;
    protected final TestEntrant loser = second;

    protected TestEntrant createEntrant() {
        TestEntrant entrant = new TestEntrant(random.nextInt());
        int previousSize = testEntrants.size();
        testEntrants.add(entrant);

        assert testEntrants.size() > previousSize;
        return entrant;
    }
}
