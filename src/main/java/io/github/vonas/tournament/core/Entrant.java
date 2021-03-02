package io.github.vonas.tournament.core;

import java.util.UUID;

public abstract class Entrant implements Comparable<Entrant> {

    /**
     * @return The UUID of this Entrant.
     */
    public abstract UUID getUuid();

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;

        Entrant other = (Entrant)o;
        return getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode(){
        return getUuid().hashCode();
    }

    @Override
    public int compareTo(Entrant other){
        return getUuid().compareTo(other.getUuid());
    }
}
