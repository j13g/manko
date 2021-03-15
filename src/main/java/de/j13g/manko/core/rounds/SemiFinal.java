package de.j13g.manko.core.rounds;

import de.j13g.manko.core.annotations.UnsupportedOperation;
import de.j13g.manko.core.exceptions.NewEntrantsNotAllowedException;

import java.io.Serializable;
import java.util.*;

public class SemiFinal<E> extends DynamicElimination<E> implements Serializable {

    private static final int ENTRANT_COUNT = 4;

    private final Set<E> originalEntrants;

    public SemiFinal(E first, E second, E third, E fourth) {
        super.addEntrant(first);
        super.addEntrant(second);
        super.addEntrant(third);
        super.addEntrant(fourth);
        originalEntrants = getFrozenEntrantsSet();
    }

    public SemiFinal(Collection<E> entrants) {
        if (entrants.size() != ENTRANT_COUNT)
            throw new IllegalArgumentException();

        entrants.forEach(super::addEntrant);
        originalEntrants = getFrozenEntrantsSet();
    }

    private Set<E> getFrozenEntrantsSet() {
        return Collections.unmodifiableSet(new HashSet<>(entrants));
    }

    @Override
    public boolean addEntrant(E entrant) throws NewEntrantsNotAllowedException {
        if (!originalEntrants.contains(entrant))
            throw new NewEntrantsNotAllowedException();

        return super.addEntrant(entrant);
    }

    @Override
    @UnsupportedOperation
    public boolean resetEntrant(E entrant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasStateAbout(E entrant) {
        return originalEntrants.contains(entrant);
    }
}
