package io.github.vonas.manko.fabric;

import com.mojang.brigadier.context.CommandContext;
import io.github.vonas.manko.core.Entrant;
import io.github.vonas.manko.exceptions.MissingPlayerException;
import io.github.vonas.manko.mixin.EntitySelectorAccessor;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.EntitySelector;

import java.util.UUID;

public class Player extends Entrant {

    private final UUID uuid;
    private final String displayName;

    public Player(UUID uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public static Player fromEntitySelector(CommandContext<FabricClientCommandSource> context,
                                            EntitySelector entitySelector) throws MissingPlayerException {

        String playerName = ((EntitySelectorAccessor) entitySelector).getPlayerName();
        if (playerName == null)
            throw new MissingPlayerException();

        return fromPlayerName(context, playerName);
    }

    public static Player fromPlayerName(CommandContext<FabricClientCommandSource> context, String playerName)
            throws MissingPlayerException {

        for (AbstractClientPlayerEntity player : context.getSource().getWorld().getPlayers()) {
            // TODO: Is getEntityName() the right method here?
            if (player.getEntityName().equals(playerName))
                return new Player(player.getUuid(), playerName);
        }

        throw new MissingPlayerException();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }
}
