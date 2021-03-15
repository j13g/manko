package de.j13g.manko.core.formats;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;
import de.j13g.manko.core.Standings;
import de.j13g.manko.core.base.FinalRound;
import de.j13g.manko.core.base.Round;
import de.j13g.manko.core.base.TournamentFormat;
import de.j13g.manko.core.exceptions.FinalRoundException;
import de.j13g.manko.core.exceptions.RoundNotFinishedException;
import de.j13g.manko.core.exceptions.UnfinishedPairingsException;
import de.j13g.manko.core.rounds.DynamicElimination;
import de.j13g.manko.core.rounds.Final;
import de.j13g.manko.core.rounds.RoundRobinFinal;
import de.j13g.manko.core.rounds.SemiFinal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DefaultFormat<E> implements TournamentFormat<E>, Serializable {

    @Override
    public Round<E> createInitialRound() {
        return new DynamicElimination<>();
    }

    @Override
    public Round<E> createNextRound(Round<E> currentRound)
            throws RoundNotFinishedException, FinalRoundException {

        if (!currentRound.isFinished())
            throw new RoundNotFinishedException();

        if (currentRound instanceof RoundRobinFinal) {
            RoundRobinFinal<E> roundRobinFinal = (RoundRobinFinal<E>) currentRound;
            if (roundRobinFinal.isTie()) {
                assert roundRobinFinal.getEntrants().size() == 3;
                Iterator<E> it = roundRobinFinal.getEntrants().iterator();
                return new RoundRobinFinal<>(it.next(), it.next(), it.next());
            }
        }

        if (currentRound instanceof FinalRound)
            throw new FinalRoundException();

        if (currentRound instanceof SemiFinal)
            return createNextRound((SemiFinal<E>) currentRound);
        if (currentRound instanceof DynamicElimination)
            return createNextRound((DynamicElimination<E>) currentRound);

        throw new IllegalArgumentException("Round type not supported");
    }

    public Round<E> createNextRound(DynamicElimination<E> round) throws FinalRoundException {

        Set<E> advancedEntrants = round.getAdvancedEntrants();

        if (advancedEntrants.size() > 4)
            return new DynamicElimination<>(advancedEntrants);
        if (advancedEntrants.size() == 4)
            return new SemiFinal<>(advancedEntrants);
        if (advancedEntrants.size() == 3) {
            Iterator<E> it = advancedEntrants.iterator();
            return new RoundRobinFinal<>(it.next(), it.next(), it.next());
        }

        if (advancedEntrants.size() == 2) {
            Iterator<E> it = advancedEntrants.iterator();
            return new Final<>(new Pairing<>(it.next(), it.next()));
        }

        throw new FinalRoundException();
    }

    public Final<E> createNextRound(SemiFinal<E> round) throws FinalRoundException {

        Set<E> advancedEntrants = round.getAdvancedEntrants();
        Set<E> eliminatedEntrants = round.getEliminatedEntrants();
        Iterator<E> advancedIterator = advancedEntrants.iterator();
        Iterator<E> eliminatedIterator = eliminatedEntrants.iterator();

        Pairing<E> firstPlacePairing;
        Pairing<E> thirdPlacePairing;

        if (advancedEntrants.size() > 2 || eliminatedEntrants.size() > 2)
            throw new RuntimeException();

        if (advancedEntrants.size() == 2 && eliminatedEntrants.size() == 2) {
            firstPlacePairing = new Pairing<>(advancedIterator.next(), advancedIterator.next());
            thirdPlacePairing = new Pairing<>(eliminatedIterator.next(), eliminatedIterator.next());
            return new Final<>(firstPlacePairing, thirdPlacePairing);
        }

        // There are no entrants on the same "level", i.e. that need to
        // get paired in order to determine the winners of the tournament.
        if (advancedEntrants.size() <= 1 && eliminatedEntrants.size() <= 1)
            throw new FinalRoundException();

        // Since there are at least 2 advanced or eliminated entrants,
        // there consequently have to be two pairings.
        assert round.getFinishedPairings().size() == 2;

        // Note: One of the sizes has to be 2.
        int sizeSum = advancedEntrants.size() + eliminatedEntrants.size();
        assert sizeSum == 3; // One size == 2 and the other is < 2.

        E winningEntrant;
        Pairing<E> predeterminedPairing;
        Pairing<E> otherPairing;

        Set<E> withTwo = advancedEntrants.size() == 2 ? advancedEntrants : eliminatedEntrants;
        Iterator<E> withTwoIterator = withTwo.iterator();
        otherPairing = new Pairing<>(withTwoIterator.next(), withTwoIterator.next());

        Set<E> withOne = advancedEntrants.size() == 1 ? advancedEntrants : eliminatedEntrants;
        Set<E> finishedEntrants = new HashSet<>(round.getPairings().getFinishedEntrants());
        assert finishedEntrants.size() == 4;

        winningEntrant = withOne.iterator().next();
        finishedEntrants.remove(otherPairing.getFirst());
        finishedEntrants.remove(otherPairing.getSecond());
        finishedEntrants.remove(winningEntrant);

        E losingEntrant = finishedEntrants.iterator().next();
        predeterminedPairing = new Pairing<>(winningEntrant, losingEntrant);

        firstPlacePairing = advancedEntrants.size() == 2 ? otherPairing : predeterminedPairing;
        thirdPlacePairing = advancedEntrants.size() == 1 ? otherPairing : predeterminedPairing;

        Final<E> finalRound = new Final<>(firstPlacePairing, thirdPlacePairing);

        if (winningEntrant != null) {
            if (thirdPlacePairing.contains(winningEntrant))
                finalRound.setThirdPlacePairingFirst();
            else if (firstPlacePairing.contains(winningEntrant))
                finalRound.setFirstPlacePairingFirst();

            try {
                finalRound.nextPairing();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            finalRound.declareWinner(winningEntrant);
        }

        return finalRound;
    }
}
