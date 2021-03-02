package io.github.vonas.tournament;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.text.LiteralText;

public class TournamentMod implements ModInitializer {
	@Override
	public void onInitialize() {
		ClientCommandManager.DISPATCHER.register(
			ClientCommandManager.literal("foo").executes(context -> {
				System.out.println("foo");
				context.getSource().sendFeedback(new LiteralText("Hello, world!"));
				return 0;
			})
		);
	}
}
