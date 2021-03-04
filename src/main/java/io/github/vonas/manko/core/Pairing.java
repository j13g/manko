package io.github.vonas.manko.core;

import com.google.common.base.Objects;
import io.github.vonas.manko.exceptions.NoSuchEntrantException;

import java.io.Serializable;

public class Pairing implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pairing)) return false;

        Pairing other = (Pairing)o;
        return entrant1.getUuid().equals(other.entrant1.getUuid())
            && entrant2.getUuid().equals(other.entrant2.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entrant1, entrant2);
    }
}
