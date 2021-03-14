package de.j13g.manko;

import de.j13g.manko.fabric.Commands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class Manko implements ModInitializer {

	private static Commands commands;

	private static KeyBinding keyOpenCommand;

	@Override
	public void onInitialize() {

		commands = new Commands(MinecraftClient.getInstance());

		keyOpenCommand = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.manko.tournament",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"category.manko.tournament"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyOpenCommand.wasPressed())
				client.openScreen(new ChatScreen("/" + Commands.PREFIX_SHORT));

			File runDir = client.runDirectory;
		});

		commands.registerCommands(ClientCommandManager.DISPATCHER);
	}
}
