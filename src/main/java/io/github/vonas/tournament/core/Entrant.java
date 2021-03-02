package io.github.vonas.tournament.core;

import java.util.UUID;

public class Entrant implements Comparable<Entrant> {

    private final UUID playerUUID;

    public Entrant(UUID player) {
        this.playerUUID = player;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;

        Entrant other = (Entrant)o;
        return this.playerUUID.equals(other.playerUUID);
    }

    @Override
    public int hashCode(){
        return playerUUID.hashCode();
    }

    @Override
    public int compareTo(Entrant other){
        //returns -1 if "this" object is less than "that" object
        //returns 0 if they are equal
        //returns 1 if "this" object is greater than "that" object
        return this.playerUUID.compareTo(other.playerUUID);
    }
}
