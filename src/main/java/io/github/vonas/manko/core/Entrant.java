package io.github.vonas.manko.core;

import java.util.UUID;

public abstract class Entrant {

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
}
