package de.j13g.manko.core;

import de.j13g.manko.core.base.Round;
import de.j13g.manko.core.base.RoundManager;
import de.j13g.manko.core.base.TournamentFormat;
import de.j13g.manko.core.exceptions.*;

import java.io.Serializable;

public class Tournament<E> implements RoundManager<E>, Serializable {

    private final TournamentFormat<E> format;

    // TODO One could also make a history (stack).
    //  This would be useful for getting all stats about a tournament.
    private Round<E> previousRound = null;
    private Round<E> currentRound;

    public Tournament(TournamentFormat<E> format) {
        currentRound = format.createInitialRound();
        this.format = format;
    }

    @Override
    public Round<E> getCurrentRound() {
        return currentRound;
    }

    @Override
    public void nextRound() throws RoundNotFinishedException, FinalRoundException {

        // We don't want to overwrite previousRound too early
        // because createNextRound might throw an exception.

        Round<E> oldCurrentRound = currentRound;
        currentRound = format.createNextRound(currentRound);
        previousRound = oldCurrentRound;
    }

    @Override
    public void previousRound() throws AlreadyStartedException, InitialRoundException {
        if (previousRound == null)
            throw new InitialRoundException();
        if (currentRound.getActivePairings().size() > 0 || currentRound.getFinishedPairings().size() > 0)
            throw new AlreadyStartedException();

        // FIXME One can see here that "InitialRoundException" is not the right term.
        //  It'll be thrown many rounds in just because going back once sets previousRound to null.

        currentRound = previousRound;
        previousRound = null;
    }

    @Override
    public boolean addEntrant(E entrant) {
        return currentRound.addEntrant(entrant);
    }

    @Override
    public boolean removeEntrant(E entrant) {
        return currentRound.removeEntrant(entrant);
    }

    @Override
    public boolean resetEntrant(E entrant) {
        return currentRound.resetEntrant(entrant);
    }

    @Override
    public Pairing<E> nextPairing()
            throws NoEntrantsException, NoOpponentException, UnfinishedPairingsException, NoMorePairingsException {

        return currentRound.nextPairing();
    }

    @Override
    public boolean replayPairing(Pairing<E> pairing)
            throws NoSuchPairingException, MissingEntrantException, OrphanedPairingException {

        return currentRound.replayPairing(pairing);
    }

    @Override
    public void declareWinner(E winningEntrant, Pairing<E> pairing)
            throws NoSuchEntrantException, NoSuchPairingException {

        currentRound.declareWinner(winningEntrant, pairing);
    }

    @Override
    public Pairing<E> declareWinner(E winningEntrant)
            throws NoSuchEntrantException, MissingPairingException {

        return currentRound.declareWinner(winningEntrant);
    }

    @Override
    public void declareTie(Pairing<E> pairing)
            throws NoSuchPairingException {

        currentRound.declareTie(pairing);
    }
}
