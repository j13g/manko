package io.github.vonas.tournament.core;

import io.github.vonas.tournament.exceptions.NoSuchEntrantException;

// TODO: Add team functionality.
public class Pairing {

    private final Entrant entrant1;
    private final Entrant entrant2;

    public Pairing(Entrant entrant1, Entrant entrant2) {
        this.entrant1 = entrant1;
        this.entrant2 = entrant2;
    }

    public Entrant getEntrant1() {
        return entrant1;
    }
    public Entrant getEntrant2() {
        return entrant2;
    }

    public Entrant otherEntrant(Entrant entrant) throws NoSuchEntrantException {
        if (!hasEntrant(entrant))
            throw new NoSuchEntrantException();

        return entrant == entrant1 ? entrant2 : entrant1;
    }

    public boolean hasEntrant(Entrant entrant) {
        return entrant == entrant1 || entrant == entrant2;
    }
}
