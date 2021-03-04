package io.github.vonas.manko;

import io.github.vonas.manko.fabric.Commands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

public class Manko implements ModInitializer {

	private final Commands commands = new Commands();

	@Override
	public void onInitialize() {
		commands.registerCommands(ClientCommandManager.DISPATCHER);
	}
}
