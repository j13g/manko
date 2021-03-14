package de.j13g.manko.core.rounds;

import de.j13g.manko.core.exceptions.InvalidEntrantException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SemiFinal<E extends Serializable> extends DynamicElimination<E> {

    private final Set<E> originalEntrants;

    public SemiFinal(E first, E second, E third, E fourth) {
        super.addEntrant(first);
        super.addEntrant(second);
        super.addEntrant(third);
        super.addEntrant(fourth);
        originalEntrants = Collections.unmodifiableSet(new HashSet<>(entrants));
    }

    @Override
    public boolean addEntrant(E entrant) throws NewEntrantsNotAllowedException {
        if (!originalEntrants.contains(entrant))
            throw new NewEntrantsNotAllowedException();

        return super.addEntrant(entrant);
    }

    @Override
    public boolean resetEntrant(E entrant) {
        throw new UnsupportedOperationException();
    }
}
