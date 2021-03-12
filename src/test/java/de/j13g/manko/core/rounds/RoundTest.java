package de.j13g.manko.core.rounds;

import de.j13g.manko.core.TestEntrant;

import java.util.*;

public abstract class RoundTest {

    protected final Random random = new Random(0);
    protected final HashSet<TestEntrant> testEntrants = new HashSet<>();

    protected final TestEntrant first = createEntrant();
    protected final TestEntrant second = createEntrant();
    protected final TestEntrant third = createEntrant();
    protected final TestEntrant fourth = createEntrant();

    protected final TestEntrant invalidEntrant = createEntrant();

    protected TestEntrant createEntrant() {
        TestEntrant entrant = new TestEntrant(random.nextInt());
        int previousSize = testEntrants.size();
        testEntrants.add(entrant);

        assert testEntrants.size() > previousSize;
        return entrant;
    }
}
