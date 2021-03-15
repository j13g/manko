package de.j13g.manko.fabric.arguments;

import de.j13g.manko.core.Tournament;
import de.j13g.manko.fabric.Player;
import de.j13g.manko.fabric.arguments.EntrantArgumentType.EntrantType;

import java.util.function.Supplier;

public class EntrantArgumentTypeFactory {

    private final Supplier<Tournament<Player>> tournamentSupplier;

    public EntrantArgumentTypeFactory(Supplier<Tournament<Player>> tournamentSupplier) {
        this.tournamentSupplier = tournamentSupplier;
    }

    public EntrantArgumentType entrant() {
        return new EntrantArgumentType(EntrantType.PARTICIPATING, tournamentSupplier);
    }

    public EntrantArgumentType paired() {
        return new EntrantArgumentType(EntrantType.PAIRED, tournamentSupplier);
    }

    public EntrantArgumentType pairedWithArgument(String argument) {
        return new EntrantArgumentType(EntrantType.PAIRED, tournamentSupplier, argument);
    }

    public EntrantArgumentType withState() {
        return new EntrantArgumentType(EntrantType.WITH_STATE, tournamentSupplier);
    }

    public EntrantArgumentType withResettableState() {
        return new EntrantArgumentType(EntrantType.WITH_RESETTABLE_STATE, tournamentSupplier);
    }

    public EntrantArgumentType finished() {
        return new EntrantArgumentType(EntrantType.FINISHED, tournamentSupplier);
    }
}
