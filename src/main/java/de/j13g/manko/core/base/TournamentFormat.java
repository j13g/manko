package de.j13g.manko.core.base;

import de.j13g.manko.core.exceptions.FinalRoundException;
import de.j13g.manko.core.exceptions.RoundNotFinishedException;

import java.io.Serializable;

public interface TournamentFormat<E> {

    Round<E> createInitialRound();

    Round<E> createNextRound(Round<E> currentRound)
            throws RoundNotFinishedException, FinalRoundException;
}
