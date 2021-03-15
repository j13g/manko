package de.j13g.manko.fabric.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Tournament;
import de.j13g.manko.core.base.EliminationRound;
import de.j13g.manko.core.base.Round;
import de.j13g.manko.core.managers.base.Pairings;
import net.minecraft.command.CommandSource;
import de.j13g.manko.fabric.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EntrantArgumentType implements ArgumentType<Player> {

    public enum EntrantType {
        PARTICIPATING,
        FINISHED,
        PAIRED,
        WITH_STATE,
        WITH_RESETTABLE_STATE
    }

    private final Supplier<Tournament<Player>> tournamentSupplier;
    private final EntrantType type;

    private final String argumentName;

    public EntrantArgumentType(EntrantType type, Supplier<Tournament<Player>> tournamentSupplier,
                               String argumentName) {
        this.type = type;
        this.tournamentSupplier = tournamentSupplier;
        this.argumentName = argumentName;
    }

    public EntrantArgumentType(EntrantType type, Supplier<Tournament<Player>> tournamentSupplier) {
        this(type, tournamentSupplier, null);
    }

    public static <S> Player getPlayer(String name, CommandContext<S> context) {
        return context.getArgument(name, Player.class);
    }

    @Override
    public Player parse(StringReader reader) {

        int start = reader.getCursor();
        if (!reader.canRead())
            reader.skip();

        while (reader.canRead() && reader.peek() != ' ')
            reader.skip();

        String playerName = reader.getString().substring(start, reader.getCursor());
        return new Player(playerName);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Tournament<Player> tournament = tournamentSupplier.get();
        if (tournament == null)
            return Suggestions.empty();

        Round<Player> round = tournament.getCurrentRound();
        Pairings<Player> pairings = round.getPairings();

        Stream<Player> suggestions;

        if (type == EntrantType.PARTICIPATING) {
            suggestions = round.getEntrants().stream();
        }
        else if (type == EntrantType.PAIRED) {
            if (argumentName != null) {
                Player player = getPlayer(argumentName, context);

                Pairing<Player> activePairing = pairings.findActiveByEntrant(player);
                Set<Pairing<Player>> finishedPairings = pairings.findFinishedByEntrant(player);

                Stream.Builder<Player> streamBuilder = Stream.builder();
                if (activePairing != null)
                    streamBuilder.add(activePairing.getOther(player));
                for (Pairing<Player> pairing : finishedPairings)
                    streamBuilder.add(pairing.getOther(player));

                suggestions = streamBuilder.build();
            }
            else {
                suggestions = pairings.getActiveEntrants().stream();
            }
        }
        else if (type == EntrantType.FINISHED) {
            suggestions = pairings.getFinishedEntrants().stream();
        }
        else if (type == EntrantType.WITH_STATE || type == EntrantType.WITH_RESETTABLE_STATE) {
            if (round instanceof EliminationRound) {
                EliminationRound<Player> eliminationRound = (EliminationRound<Player>) round;
                suggestions = eliminationRound.getEntrantsWithState();
            }
            else {
                // TODO This is incorrect.
                //  But we won't use WITH_STATE in other types of rounds anyway.
                return Suggestions.empty();
            }
        }
        else {
            return Suggestions.empty();
        }

        if (type == EntrantType.WITH_RESETTABLE_STATE)
            // NOTE: Be careful here with that type cast.
            suggestions = suggestions.filter(p -> !((EliminationRound<Player>) round).isEntrantPending(p));

        Stream<String> names = suggestions.map(Player::getName);
        return CommandSource.suggestMatching(names, builder);
    }
}
