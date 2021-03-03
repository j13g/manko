package io.github.vonas.tournament.util;

import io.github.vonas.tournament.core.Entrant;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEntrant extends Entrant {

    private final UUID uuid;

    public TestEntrant(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    private static final HashSet<UUID> uniqueUuids = new HashSet<>();

    public static TestEntrant createUnique() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (uniqueUuids.contains(uuid));

        uniqueUuids.add(uuid);
        return new TestEntrant(uuid);
    }

    public static Set<Entrant> generateUnique(int n) {
        HashSet<Entrant> entrants = new HashSet<>();
        for (int i = 0; i < n; ++i)
            entrants.add(createUnique());

        assertEquals(n, entrants.size());
        return entrants;
    }
}
