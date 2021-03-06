package io.github.vonas.manko.fabric;

import com.mojang.brigadier.context.CommandContext;
import io.github.vonas.manko.fabric.exceptions.MissingPlayerException;
import io.github.vonas.manko.mixin.EntitySelectorAccessor;
import io.github.vonas.manko.util.Identifiable;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.EntitySelector;

import java.util.UUID;

public class Player extends Identifiable<UUID> {

    private final UUID uuid;
    private final String name;

    public Player(UUID uuid, String name) {
        super(uuid);
        this.uuid = uuid;
        this.name = name;
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

    public String getName() {
        return name;
    }
}
