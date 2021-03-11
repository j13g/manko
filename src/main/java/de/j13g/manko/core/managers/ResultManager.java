package de.j13g.manko.core.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResultManager<E> {

    private final HashSet<E> advanced = new HashSet<>();
    private final HashSet<E> eliminated = new HashSet<>();

    public void advance(E entrant) {
        assert !eliminated.contains(entrant);

        reset(entrant);
        advanced.add(entrant);
    }

    public void eliminate(E entrant) {
        assert !advanced.contains(entrant);

        reset(entrant);
        eliminated.add(entrant);
    }

    public void reset(E entrant) {
        advanced.remove(entrant);
        eliminated.remove(entrant);
    }

    /**
     * Moves the entrant from these results to other results.
     * The entrant is not moved if it's not part of this result set.
     * @param other The other result set.
     * @param entrant The entrant.
     * @return If the entrant was moved.
     */
    public boolean moveTo(ResultManager<E> other, E entrant) {
        if (!contains(entrant))
            return false;

        if (isAdvanced(entrant)) {
            other.advance(entrant);
        }
        else {
            assert isEliminated(entrant);
            other.eliminate(entrant);
        }

        reset(entrant);
        return true;
    }

    public boolean contains(E entrant) {
        return isAdvanced(entrant) || isEliminated(entrant);
    }

    public boolean isAdvanced(E entrant) {
        return advanced.contains(entrant);
    }

    public boolean isEliminated(E entrant) {
        return eliminated.contains(entrant);
    }

    public Set<E> getAdvanced() {
        return Collections.unmodifiableSet(advanced);
    }

    public Set<E> getEliminated() {
        return Collections.unmodifiableSet(eliminated);
    }
}
