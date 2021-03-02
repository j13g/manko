package io.github.vonas.tournament;

import io.github.vonas.tournament.core.Entrant;

import java.util.UUID;

public class TestEntrant extends Entrant {

    private final UUID uuid;

    public TestEntrant(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
