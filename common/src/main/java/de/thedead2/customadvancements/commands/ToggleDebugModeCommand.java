package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ToggleDebugModeCommand extends ModCommand {

    protected ToggleDebugModeCommand(LiteralArgumentBuilder<CommandSourceStack> shortLA, LiteralArgumentBuilder<CommandSourceStack> longLA) {
        super(shortLA, longLA);
    }

    public static void register() {
        Builder.newModCommand("debug", command -> {
            boolean current = ConfigManager.DEBUG_MODE.getAsBoolean();

            ConfigManager.DEBUG_MODE.set(!current);

            command.getSource().sendSuccess(() -> Component.literal("Debug Mode set to " + !current), true);

            return COMMAND_SUCCESS;
        });
    }
}
