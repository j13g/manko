package de.j13g.manko.fabric;

import com.mojang.brigadier.context.CommandContext;
import de.j13g.manko.fabric.exceptions.MissingPlayerException;
import de.j13g.manko.util.Identifiable;
import de.j13g.manko.mixin.EntitySelectorAccessor;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.EntitySelector;

import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a player in the game.
 * Note that we are not using their UUID here because players need to
 * also be instantiable when getting their UUID is impossible or impractical.
 */
public class Player extends Identifiable<String> implements Serializable {

    private final String name;

    public Player(String name) {
        super(name.toLowerCase(Locale.ROOT));
        this.name = name;
    }

    public static Player fromEntitySelector(CommandContext<FabricClientCommandSource> context,
                                            EntitySelector entitySelector) {

        String name = ((EntitySelectorAccessor) entitySelector).getPlayerName();
        if (name == null)
            throw new RuntimeException("Player name is empty");

        return new Player(name);
    }

    public String getName() {
        return name;
    }
}
