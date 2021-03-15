package de.j13g.manko.core.rounds;

import de.j13g.manko.RoundTest;
import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.TestEntrant;
import de.j13g.manko.core.exceptions.UnfinishedPairingsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinFinalTest extends RoundTest {

    private RoundRobinFinal<TestEntrant> round;

    @BeforeEach
    void init() {
        round = new RoundRobinFinal<>(first, second, third);
    }

    private TestEntrant getExcept(TestEntrant one, TestEntrant two) {
        Set<TestEntrant> entrants = new HashSet<>(Arrays.asList(first, second, third));
        entrants.remove(one);
        entrants.remove(two);
        assert entrants.size() == 1;
        return entrants.iterator().next();
    }

    @Test
    void x() throws UnfinishedPairingsException {
        Pairing<TestEntrant> pairing = round.nextPairing();
        TestEntrant winner = pairing.getFirst();
        TestEntrant loser = pairing.getSecond();
        TestEntrant other = getExcept(winner, loser);

        round.declareWinner(winner);
        round.removeEntrant(winner);
        round.addEntrant(winner);

        assertEquals(1, round.getScore(winner));
        assertEquals(0, round.getScore(loser));
        assertEquals(0, round.getScore(other));
    }
}