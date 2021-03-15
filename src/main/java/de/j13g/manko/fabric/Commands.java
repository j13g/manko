package de.j13g.manko.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;
import de.j13g.manko.core.Tournament;
import de.j13g.manko.core.annotations.UnsupportedOperation;
import de.j13g.manko.core.base.EliminationRound;
import de.j13g.manko.core.base.Round;
import de.j13g.manko.core.base.TournamentFormat;
import de.j13g.manko.core.exceptions.*;
import de.j13g.manko.core.formats.DefaultFormat;
import de.j13g.manko.core.managers.base.Pairings;
import de.j13g.manko.core.rounds.DynamicElimination;
import de.j13g.manko.core.rounds.Final;
import de.j13g.manko.core.rounds.RoundRobinFinal;
import de.j13g.manko.core.rounds.SemiFinal;
import de.j13g.manko.fabric.arguments.CollectionValuesArgumentType;
import de.j13g.manko.fabric.arguments.EntrantArgumentType;
import de.j13g.manko.fabric.arguments.EntrantArgumentTypeFactory;
import de.j13g.manko.fabric.arguments.EnumValuesArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;

// TODO This class needs a cleanup.
/**
 * Glues minecraft commands with tournament actions.
 * Messages should be unobtrusive and have the same color and format whenever possible.
 * Errors indicate that the command failed and could not be performed. They are red.
 * Warnings indicate that the desired state is already achieved. They are yellow.
 * Player names are written in a different but consistent color.
 * When a player's state changed by a command, their name should be underlined.
 */
public final class Commands {

    private static class State implements Serializable {

        public Tournament<Player> tournament = null;
        public final HashMap<Event, String> onEventTemplates = new HashMap<>();
        public final HashMap<String, String> customCommands = new HashMap<>();
    }

    public static final String PREFIX = "tournament:";
    public static final String PREFIX_SHORT = "t:";

    private static final String RESET_TERMINAL = "-";

    private static final String SERIALIZE_FILE = "manko.ser";
    private static final String SERIALIZE_FILE_BAK = SERIALIZE_FILE + ".bak";

    private static final TournamentFormat<Player> format = new DefaultFormat<>();

    private final State state;

    public Commands(MinecraftClient client) {
        State loadedState = loadState(client);
        state = loadedState != null ? loadedState : new State();
    }

    private State loadState(MinecraftClient client) {
        File serializeFile = new File(client.runDirectory, SERIALIZE_FILE);
        if (!serializeFile.exists())
            return null;

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(serializeFile);
        }
        catch (FileNotFoundException e) {
            System.err.printf("Could not open state file: %s.", e.getMessage());
            return null;
        }

        Object deserializedState;
        try {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            deserializedState = in.readObject();
            in.close();
        }
        catch (IOException e) {
            System.err.printf("Could not deserialize saved state: %s.", e.getMessage());
            return null;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!(deserializedState instanceof State))
            throw new RuntimeException("Deserialized object has wrong class type.");

        return (State) deserializedState;
    }

    private void saveState(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = ctx.getSource().getClient();
        File serializeFile = new File(client.runDirectory, SERIALIZE_FILE);

        if (serializeFile.exists()) {
            try {
                File serializeFileBak = new File(client.runDirectory, SERIALIZE_FILE_BAK);
                Files.copy(serializeFile.toPath(), serializeFileBak.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                error(ctx, format("Could not create backup of old state: %s", e.getMessage()));
            }

            if (!serializeFile.delete()) {
                error(ctx, "Could not delete old state before saving new state.");
                return;
            }
        }

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(serializeFile);
        }
        catch (FileNotFoundException e) {
            error(ctx, format("Failed to write state: %s", e.getMessage()));
            return;
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(state);
            out.close();
        }
        catch (IOException e) {
            error(ctx, format("Failed to write state: %s", e.getMessage()));
        }
    }

    private enum Event {

        PAIRING;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private enum InfoType {

        PARTICIPANTS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        Arrays.asList(PREFIX, PREFIX_SHORT).forEach(prefix -> registerCommands(dispatcher, prefix));
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, String prefix) {

        final EntrantArgumentTypeFactory entrantArgument = new EntrantArgumentTypeFactory(() -> state.tournament);

        new EnumValuesArgumentType<>(InfoType.class);

        dispatcher.register(
                literal(prefix, "new")
                        .executes(this::cNew)
        );

        dispatcher.register(
                literal(prefix, "stop")
                        .executes(this::cStop)
        );

        dispatcher.register(
                literal(prefix, "add").then(
                        argument("player", EntityArgumentType.player())
                                .executes(this::cAdd))
        );

        dispatcher.register(
                literal(prefix, "remove").then(
                        argument("participant", entrantArgument.entrant())
                                .executes(this::cRemove))
        );

        dispatcher.register(
                literal(prefix, "pair").executes(this::cPair)
        );

        dispatcher.register(
                literal(prefix, "win").then(
                        argument("participant", entrantArgument.paired())
                                .executes(this::cWin))
        );

        dispatcher.register(
                literal(prefix, "replay")
                        .then(argument("participant", entrantArgument.finished())
                                .then(argument("opponent", entrantArgument.pairedWithArgument("participant"))
                                        .executes(this::cReplay)))
        );

        // TODO Replay last pairing without arguments.

        dispatcher.register(
                literal(prefix, "reset")
                        .then(argument("participant", entrantArgument.withResettableState())
                                .executes(this::cReset))
        );

        dispatcher.register(
                literal(prefix, "next")
                        .executes(this::cNext)
        );

        dispatcher.register(
                literal(prefix, "info")
                        .then(argument("topic", EnumValuesArgumentType.enumValues(InfoType.class))
                                .executes(this::cInfo))
                        .executes(this::cInfoDefaultTopic)

        );

        // Commands

        // %1 -- The first player of the earliest active pairing.
        // %2 -- The second player of the earliest active pairing.

        dispatcher.register(
                literal(prefix, "command")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("template", StringArgumentType.greedyString())
                                        .executes(this::cCommand)))
        );

        dispatcher.register(
                literal(prefix, "exec")
                        .then(argument("command", CollectionValuesArgumentType.collection(state.customCommands.keySet()))
                                .executes(this::cExec))
        );

        // Events

        dispatcher.register(
                literal(prefix, "on")
                        .then(argument("event", EnumValuesArgumentType.enumValues(Event.class))
                                .then(argument("template", StringArgumentType.greedyString())
                                        .executes(this::cOnEvent)))
        );

        // Shortcuts

        // FIXME Does not pick up arguments..
//        dispatcher.register(literal(prefix, "a").redirect(add));
    }

    private Tournament<Player> createTournament() {
        return new Tournament<>(format);
    }

    private int cNew(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (checkRunningTournament(ctx, false)) {
            error(ctx, "Cannot create a new tournament while another is active.");
            return -1;
        }

        state.tournament = createTournament();
        info(ctx, "Created a new tournament.");

        return 0;
    }

    private int cStop(CommandContext<FabricClientCommandSource> ctx) {
        if (!checkRunningTournament(ctx))
            return -1;

        if (isConfirmed(Confirmation.STOP, ctx)) {
            state.tournament = null;
            info(ctx, "The tournament has been stopped and deleted.");
            return 0;
        }

        String message = "You are about to stop and delete the current tournament.";
        warn(ctx, message + i(" Please confirm your action by entering the command again."));
        return 0;
    }

    private int cAdd(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        EntitySelector playerSelector = ctx.getArgument("player", EntitySelector.class);
        Player player = Player.fromEntitySelector(ctx, playerSelector);

        Round<Player> currentRound = state.tournament.getCurrentRound();
        boolean hadState = currentRound.hasStateAbout(player);

        boolean isAdded;
        try {
            isAdded = state.tournament.addEntrant(player);
        }
        catch (NewEntrantsNotAllowedException e) {
            error(ctx, "Cannot add new players to this round.");
            return -3;
        }

        if (!isAdded) {
            warn(ctx, format("%s already participates in the tournament.", h(player.getName())));
            return 0;
        }

        String adverb = hadState ? "back " : "";
        info(ctx, format("Added %s %sto the tournament.", h(player.getName()), adverb));

        if (currentRound instanceof EliminationRound) {
            EliminationRound<Player> eliminationRound = (EliminationRound<Player>) currentRound;

            String state = null;
            if (eliminationRound.isEntrantAdvanced(player))
                state = "advanced";
            else if (eliminationRound.isEntrantEliminated(player))
                state = "eliminated";

            if (state != null) {
                String message = format("They are still %s.", state);
                if (currentRound instanceof DynamicElimination)
                    message += " Reset them if necessary.";
                info(ctx, message);
            }
        }

        return 0;
    }

    private int cRemove(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        Player player = EntrantArgumentType.getPlayer("participant", ctx);
        Round<Player> currentRound = state.tournament.getCurrentRound();

        if (currentRound.isEntrantPaired(player)) {
            Pairing<Player> pairing = currentRound.getLastPairing(player);
            Player opponent = pairing.getOther(player);
            error(ctx, format("%s is currently paired with another participant: %s.",
                    h(player.getName()), h(opponent.getName())));
            error(ctx, "First reset one of the players or finish the pairing.");
            return -2;
        }

        if (state.tournament.removeEntrant(player))
            info(ctx, format("Removed %s from the tournament.", h(player.getName())));
        else
            warn(ctx, format("%s does not participate in the tournament.", h(player.getName())));

        return 0;
    }

    private int cPair(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        Pairing<Player> pairing;
        try {
            pairing = state.tournament.nextPairing();
        }
        catch (NoEntrantsException e) {
            error(ctx, "There are no participants left for another pairing.");
            return -2;
        }
        catch (UnfinishedPairingsException e) {
            error(ctx, "Running pairings need to be finished first.");
            return -3;
        }
        catch (NoOpponentException e) {
            Round<Player> currentRound = state.tournament.getCurrentRound();
            if (!(currentRound instanceof EliminationRound))
                return uncheckedError(ctx, -4);

            EliminationRound<Player> eliminationRound = (EliminationRound<Player>) currentRound;
            Set<Player> entrants = eliminationRound.getPendingEntrants();

            String playerName = "?";
            if (entrants.size() >= 1) {
                assert entrants.size() == 1;
                playerName = entrants.iterator().next().getName();
            }

            error(ctx, format("Not enough players. Only %s is left.", h(playerName)));
            return -5;
        }
        catch (NoMorePairingsException e) {
            Round<Player> currentRound = state.tournament.getCurrentRound();
            if (currentRound instanceof RoundRobinFinal) {
                RoundRobinFinal<Player> roundRobinFinal = (RoundRobinFinal<Player>) currentRound;
                if (roundRobinFinal.isTie()) {
                    warn(ctx, "The round is a tie! Everyone has the same score. " +
                            "Continue to the next round in order to replay the finals.");
                    return -6;
                }
            }

            String message = currentRound.isFinished() ? "The tournament is finished. " : "";
            error(ctx, message + "There are no more pairings.");
            return -7;
        }

        String firstName = pairing.getFirst().getName();
        String secondName = pairing.getSecond().getName();

        attention(ctx, format("Next pairing: %s vs. %s.", hu(firstName), hu(secondName)));

        if (state.onEventTemplates.containsKey(Event.PAIRING)) {
            String chatMessage = state.onEventTemplates.get(Event.PAIRING);
            chatMessage = format(chatMessage, firstName, secondName);
            ctx.getSource().getPlayer().sendChatMessage(chatMessage);
        }

        return 0;
    }

    private int cWin(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        Player player = EntrantArgumentType.getPlayer("participant", ctx);

        Pairing<Player> pairing;
        try {
            pairing = state.tournament.declareWinner(player);
        }
        catch (NoSuchEntrantException e) {
            error(ctx, format("%s does not participate in the tournament.", h(player.getName())));
            return -2;
        }
        catch (MissingPairingException e) {
            error(ctx, format("%s is not in a pairing.", h(player.getName())));
            return -3;
        }

        Player opponent = pairing.getOther(player);
        info(ctx, format("%s has won their pairing against %s", hu(player.getName()), hu(opponent.getName())));

        return 0;
    }

    private int cReplay(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        Player player = EntrantArgumentType.getPlayer("participant", ctx);
        Player opponent = EntrantArgumentType.getPlayer("opponent", ctx);

        Round<Player> currentRound = state.tournament.getCurrentRound();
        Pairing<Player> pairing = new Pairing<>(player, opponent);

        boolean wasRunning;
        try {
            wasRunning = !state.tournament.replayPairing(pairing);
        }
        catch (MissingEntrantException e) {
            boolean hasPlayer = currentRound.hasEntrant(player);
            boolean hasOpponent = currentRound.hasEntrant(opponent);
            if (!hasPlayer && !hasOpponent) {
                error(ctx, "Neither of the players participate in the tournament.");
                return -2;
            }
            else if (!hasPlayer || !hasOpponent) {
                Player invalidPlayer = !hasPlayer ? player : opponent;
                error(ctx, format("%s does not participate in the tournament.", h(invalidPlayer.getName())));
                return -3;
            }

            return uncheckedError(ctx, -4);
        }
        catch (NoSuchPairingException e) {
            error(ctx, format("%s and %s did not appear in a previous pairing.",
                    h(player.getName()), h(opponent.getName())));
            return -5;
        }
        catch (OrphanedPairingException e) {
            String message = "Cannot replay pairing. ";
            Pairings<Player> pairings = currentRound.getPairings();

            // Determine the orphaned (i.e. past) pairing of one of the players.
            Pairing<Player> orphanedPairing = pairings.getLastPairingOfEntrant(player);
            if (orphanedPairing == null || pairing.equals(orphanedPairing))
                orphanedPairing = pairings.getLastPairingOfEntrant(opponent);
            if (orphanedPairing == null || pairing.equals(orphanedPairing))
                return uncheckedError(ctx, -6);

            Player orphanedPlayer = null;
            if (orphanedPairing.contains(player))
                orphanedPlayer = player;
            else if (orphanedPairing.contains(opponent))
                orphanedPlayer = opponent;

            String verb = "";
            if (pairings.isActive(orphanedPairing))
                verb = " running";
            else if (pairings.isFinished(orphanedPairing))
                verb = " finished";

            assert orphanedPlayer != null;
            message += format("%s is in another%s pairing.", h(orphanedPlayer.getName()), verb);
            if (isSupportsReset(currentRound))
                message += " Reset them to replay.";

            error(ctx, message);
            return -7;
        }

        if (wasRunning) {
            warn(ctx, format("The pairing between %s and %s is still running.",
                    h(player.getName()), h(opponent.getName())));
            return -8;
        }

        attention(ctx, format("Replaying pairing: %s vs. %s.",
                hu(player.getName()), hu(opponent.getName())));
        return 0;
    }

    private int cReset(CommandContext<FabricClientCommandSource> ctx) {
        if (!checkRunningTournament(ctx))
            return -1;

        if (isConfirmed(Confirmation.RESET, ctx))
            return cResetConfirmed(ctx);

        Player player = EntrantArgumentType.getPlayer("participant", ctx);
        Round<Player> currentRound = state.tournament.getCurrentRound();

        if (!isSupportsReset(currentRound)) {
            error(ctx, "Cannot reset a participant in the current type of round.");
            return -2;
        }

        try {
            Class<?> klass = currentRound.getClass();
            Method resetMethod = klass.getMethod("resetEntrant", Object.class);
            if (resetMethod.getDeclaredAnnotation(UnsupportedOperation.class) != null) {
                error(ctx, "Cannot reset a participant in the current type of round.");
                return -2;
            }
        } catch (NoSuchMethodException e) {
            resetConfirmation();
            throw new RuntimeException(e);
        }

        if (!currentRound.hasEntrant(player) && !currentRound.hasStateAbout(player)) {
            error(ctx, format("%s does not participate in the tournament.", h(player.getName())));
            return -3;
        }

        String message = format("About to reset %s. ", hu(player.getName()));

        if (!currentRound.hasEntrant(player) && currentRound.hasStateAbout(player)) {
            String floatingState = "but they would ";
            if (currentRound.hasWon(player))
                floatingState += "advance to the next round ";
            else if (currentRound.hasLost(player))
                floatingState += "be eliminated ";
            floatingState += "if added back.";

            message += format("They do not actively participate in this round, %s", floatingState);
        }
        else if (currentRound.isEntrantPaired(player)) {
            Pairing<Player> pairing = currentRound.getLastPairing(player);
            Player opponent = pairing.getOther(player);
            message += format("Their pairing against %s will be canceled.", h(opponent.getName()));
        }
        else if (currentRound.hasEntrantResult(player)) {
            Pairing<Player> pairing = currentRound.getLastPairing(player);
            Player opponent = pairing.getOther(player);

            String state = "";
            if (currentRound.hasWon(player))
                state = " won";
            else if (currentRound.hasLost(player))
                state = " lost";

            message += format("Their%s pairing against %s will be removed.", state, h(opponent.getName()));
        }
        else {
            info(ctx, format("%s was reset. No state has changed.", h(player.getName())));
            resetConfirmation();
            return 0;
        }

        warn(ctx, message + i(" Please confirm these changes by entering the command again."));
        return 0;
    }

    private int cResetConfirmed(CommandContext<FabricClientCommandSource> ctx) {

        Player player = EntrantArgumentType.getPlayer("participant", ctx);
        Round<Player> currentRound = state.tournament.getCurrentRound();

        boolean hadFloatingState = currentRound.hasStateAbout(player) && !currentRound.hasEntrant(player);

        boolean wasReset;
        try {
            wasReset = state.tournament.resetEntrant(player);
        }
        catch (UnsupportedOperationException e) {
            error(ctx, "Cannot reset a participant in the current type of round.");
            return -2;
        }

        if (!wasReset) {
            info(ctx, format("%s was reset. No state has changed.", h(player.getName())));
            return 0;
        }

        String message = "They are now back in pending state.";
        if (hadFloatingState)
            message = "They were fully removed from the tournament.";

        info(ctx, format("%s was reset. %s", hu(player.getName()), message));
        return 0;
    }

    private int cNext(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        try {
            state.tournament.nextRound();
        }
        catch (RoundNotFinishedException e) {
            error(ctx, "The current round is not finished.");
            return -2;
        }
        catch (FinalRoundException e) {
            error(ctx, "Already in the final round of the tournament.");
            return -3;
        }

        Round<Player> currentRound = state.tournament.getCurrentRound();
        Class<?> klass = currentRound.getClass();
        String type = "";

        if (klass == DynamicElimination.class)
            type = "Elimination";
        else if (klass == SemiFinal.class)
            type = "Semi-Finals";
        else if (klass == Final.class)
            type = "Finals";
        else if (klass == RoundRobinFinal.class)
            type = "Finals (Round Robin)";

        attention(ctx, format("Continued to the next round: %s", type));

        Stream<String> playerNameStream = currentRound.getEntrants().stream().map(p -> h(p.getName()));
        String playerNames = playerNameStream.collect(Collectors.joining(", "));

        info(ctx, format("Participants: %s", playerNames));
        return 0;
    }

    private int cInfo(CommandContext<FabricClientCommandSource> ctx) {
        return cInfoWithTopic(ctx, EnumValuesArgumentType.getEnum("topic", InfoType.class, ctx));
    }

    private int cInfoDefaultTopic(CommandContext<FabricClientCommandSource> ctx) {
        return cInfoWithTopic(ctx, InfoType.PARTICIPANTS);
    }

    private String colorForPlacement(Placement placement) {
        switch (placement) {
            case FIRST: return "§a";
            case SECOND: return "§e";
            case THIRD: return "§c";
            default: return "";
        }
    }

    private int cInfoWithTopic(CommandContext<FabricClientCommandSource> ctx, InfoType topic) {
        withoutConfirmation();

        if (!checkRunningTournament(ctx))
            return -1;

        Round<Player> currentRound = state.tournament.getCurrentRound();

        if (topic == InfoType.PARTICIPANTS) {
            // TODO: Differentiation between RoundRobin, Final, SemiFinal, etc.

            if (currentRound instanceof RoundRobinFinal) {
                RoundRobinFinal<Player> round = (RoundRobinFinal<Player>) currentRound;

                String[] placementInfos = new String[3];
                ArrayList<String> playerInfos = new ArrayList<>();

                for (Player player : round.getEntrants()) {
                    String playerFormat = format("%s (%%s)", round.isEntrantPaired(player)
                            ? color(player.getName(), "§e") : color(player.getName(), "§f"));

                    Placement placement = round.getPlacement(player);
                    int placementValue = placement.getValue();
                    if (placementValue >= 1) {
                        String placementInfo = color(placement.toString(), colorForPlacement(placement));
                        placementInfos[placementValue - 1] = format(playerFormat, placementInfo);
                    }
                    else {
                        int score = round.getScore(player);
                        String playerInfo = color(String.valueOf(score), score == 1 ? "§b" : "§f");
                        playerInfos.add(format(playerFormat, playerInfo));
                    }
                }

                for (String placementInfo : placementInfos)
                    if (placementInfo != null)
                        playerInfos.add(placementInfo);

                String participants = String.join(", ", playerInfos);
                info(ctx, format("Participants: %s", color(participants, "§7")));
            }
            else if (currentRound instanceof EliminationRound) {

                Stream.Builder<String> pendingEntrants = Stream.builder();
                Stream.Builder<String> pairedEntrants = Stream.builder();
                Stream.Builder<String> wonEntrants = Stream.builder();
                Stream.Builder<String> lostEntrants = Stream.builder();

                int nPending = 0, nWon = 0;

                for (Player player : currentRound.getEntrants()) {
                    String playerName = player.getName();
                    if (currentRound.hasWon(player)) {
                        wonEntrants.add(color(playerName, "§a", "§l"));
                        nWon += 1;
                    }
                    else if (currentRound.hasLost(player))
                        lostEntrants.add(color(playerName, "§c", "§m"));
                    else if (currentRound.isEntrantPaired(player))
                        pairedEntrants.add(color(playerName, "§e"));
                    else {
                        pendingEntrants.add(color(playerName, "§f"));
                        nPending += 1;
                    }
                }

                Stream<String> active = Stream.concat(pendingEntrants.build(), pairedEntrants.build());
                Stream<String> inactive = Stream.concat(wonEntrants.build(), lostEntrants.build());
                Stream<String> entrants = Stream.concat(active, inactive);

                String amountPending = color(String.valueOf(nPending), "§f");
                String amountWon = color(String.valueOf(nWon), "§a");
                String countInfo = color(format("(%s, %s):", amountPending, amountWon), "§7");

                String participants = String.join(", ", entrants.collect(Collectors.toList()));
                info(ctx, format("Participants %s %s", countInfo, participants));
            }
        }

        return 0;
    }

    private int cCommand(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        String name = StringArgumentType.getString(ctx, "name");
        String template = StringArgumentType.getString(ctx, "template");

        if (template.equals(RESET_TERMINAL)) {
            if (!state.customCommands.containsKey(name)) {
                info(ctx, format("Command \"%s\" already removed.", name));
                return 0;
            }

            state.customCommands.remove(name);
            info(ctx, format("Removed command \"%s\".", name));
            return 0;
        }

        boolean hadKey = state.customCommands.containsKey(name);
        state.customCommands.put(name, template);

        String operation = hadKey ? "Overwritten" : "Created";
        info(ctx, format("%s \"%s\" = %s", operation, name, template));
        return 0;
    }

    private int cExec(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        String command = StringArgumentType.getString(ctx, "command");

        if (!state.customCommands.containsKey(command)) {
            error(ctx, format("Command \"%s\" does not exist.", command));
            return -1;
        }

        // Note: § cannot be entered by the player,
        // so let's use that in our placeholder.
        final String PLACEHOLDER = "§#$#§";
        final String ESCAPE_SEQUENCE = "%%";

        final String TEMPLATE_FIRST_PAIRED = "%1";
        final String TEMPLATE_SECOND_PAIRED = "%2";

        String template = state.customCommands.get(command);
        template = template.replace(ESCAPE_SEQUENCE, PLACEHOLDER);

        if (template.contains(TEMPLATE_FIRST_PAIRED) || template.contains(TEMPLATE_SECOND_PAIRED)) {
            if (!checkRunningTournament(ctx))
                return -2;

            Round<Player> currentRound = state.tournament.getCurrentRound();
            Iterator<Pairing<Player>> activePairings = currentRound.getPairings().getActivePairingIterator();

            if (!activePairings.hasNext()) {
                error(ctx, format("Cannot use command \"%s\". There is no active pairing.", command));
                return -3;
            }

            Pairing<Player> earliestPairing = activePairings.next();
            template = template.replace(TEMPLATE_FIRST_PAIRED, earliestPairing.getFirst().getName());
            template = template.replace(TEMPLATE_SECOND_PAIRED, earliestPairing.getSecond().getName());
        }

        template = template.replace(PLACEHOLDER, ESCAPE_SEQUENCE);

        ctx.getSource().getPlayer().sendChatMessage(template);
        return 0;
    }

    private int cOnEvent(CommandContext<FabricClientCommandSource> ctx) {
        withoutConfirmation();

        Event event = EnumValuesArgumentType.getEnum("event", Event.class, ctx);
        String template = StringArgumentType.getString(ctx, "template");

        if (template.equals(RESET_TERMINAL)) {
            state.onEventTemplates.remove(event);
            info(ctx, format("Removed @%s.", event.toString()));
            return 0;
        }

        if (event == Event.PAIRING) {
            final String PLAYER_TEMPLATE = "%s";
            final int REQUIRED_OCCURRENCES = 2;

            if (countOccurrences(template, PLAYER_TEMPLATE) != REQUIRED_OCCURRENCES) {
                error(ctx, format("Requires %d %s formats.", REQUIRED_OCCURRENCES, PLAYER_TEMPLATE));
                return -1;
            }

            state.onEventTemplates.put(event, template);
        }

        info(ctx, format("@%s: %s", event.toString(), h(template)));
        return 0;
    }

    private enum Confirmation { NONE, RESET, STOP }

    private static class ConfirmationState {

        private final Confirmation confirmationType;
        private final String input;

        public ConfirmationState(Confirmation confirmationType, String input) {
            this.confirmationType = confirmationType;
            this.input = input;
        }

        public ConfirmationState(Confirmation confirmationType, CommandContext<FabricClientCommandSource> context) {
            this(confirmationType, context.getInput());
        }

        public ConfirmationState(Confirmation confirmationType) {
            this(confirmationType, (String) null);
        }

        public static ConfirmationState empty() {
            return new ConfirmationState(Confirmation.NONE);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConfirmationState))
                return false;

            ConfirmationState other = (ConfirmationState) o;
            return this.confirmationType == other.confirmationType
                && Objects.equals(this.input, other.input);
        }
    }

    // FIXME Does deserialization make this null? I think so..
    // This one's transient because we don't want to serialize confirmation state, duh.
    private transient ConfirmationState lastConfirmationState = null;

    private boolean isConfirmed(Confirmation type, CommandContext<FabricClientCommandSource> ctx) {
        ConfirmationState state = new ConfirmationState(type, ctx);
        boolean isConfirmed = state.equals(lastConfirmationState);
        lastConfirmationState = state;
        if (isConfirmed)
            resetConfirmation();
        return isConfirmed;
    }

    private void resetConfirmation() {
        lastConfirmationState = ConfirmationState.empty();
    }

    private void withoutConfirmation() {
        resetConfirmation();
    }

    private boolean isSupportsReset(Round<Player> round) {
        return (round instanceof EliminationRound) && !(round instanceof SemiFinal);

//        if (!Round.class.isAssignableFrom(klass))
//            throw new RuntimeException();
//
//        try {
//            Method resetMethod = klass.getMethod("resetEntrant", Object.class);
//            Object x = resetMethod.getDeclaredAnnotation(UnsupportedOperation.class);
//            return resetMethod.getDeclaredAnnotation(UnsupportedOperation.class) == null;
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
    }

    private int countOccurrences(String string, String subString) {
        if (subString.length() == 0)
            throw new IllegalArgumentException();
        return (string.length() - string.replace(subString, "").length()) / subString.length();
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> literal(String prefix, String name) {
        return ClientCommandManager.literal(prefix + name);
    }

    private boolean checkRunningTournament(CommandContext<FabricClientCommandSource> context, boolean printError) {
        if (state.tournament == null) {
            // TODO: Use TranslatableText here and in other places.
            //  See https://fabricmc.net/wiki/tutorial:lang
            if (printError)
                error(context, "No tournament is active.");
            return false;
        }

        // Save the state before modifying the tournament.
        saveState(context);
        return true;
    }

    private boolean checkRunningTournament(CommandContext<FabricClientCommandSource> context) {
        return checkRunningTournament(context, true);
    }

    private void attention(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(new LiteralText(A(message)));
    }

    private void info(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(new LiteralText(I(message)));
    }

    private void warn(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(new LiteralText(W(message)));
    }

    private void error(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendError(new LiteralText(E(message)));
        resetConfirmation(); // An error resets confirmation.
    }

    private int uncheckedError(CommandContext<FabricClientCommandSource> context, int code) {
        error(context, format("An unchecked error occurred (%d).", code));
        return code;
    }

    /** Highlight player (inline) */
    private String hu(String highlight) {
        return color(highlight, "§f", "§n");
    }

    /** Highlight (inline) */
    private String h(String highlight) {
        return color(highlight, "§f", "§o");
    }

    /** Attention (inline) */
    private String a(String info) {
        return color(info, "§a");
    }

    /** Info (inline) */
    private String i(String info) {
        return color(info, "§7", "§o");
    }

    /** Warning (inline) */
    private String w(String info) {
        return color(info, "§e", "§o");
    }

    /** Error (inline) */
    private String e(String error) {
        return color(error, "§c", "§o");
    }

    /** Info (message) */
    private String A(String text) {
        return finish(a(text));
    }

    /** Info (message) */
    private String I(String text) {
        return finish(i(text));
    }

    /** Warning (message) */
    private String W(String text) {
        return finish(w(text));
    }

    /** Error (message) */
    private String E(String error) {
        return finish(e(error));
    }

    private String color(String text, String color, String emphasis) {
        text = text.replace("§r", "§r" + emphasis);
        text = text.replace("§x", "§r" + color + emphasis);
        return color + emphasis + text + "§x";
    }

    private String color(String text, String color) {
        return color(text, color, "");
    }

    private String finish(String text) {
        return "§r" + text.replace("§x", "§r");
    }
}
