package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;
import net.minecraft.command.CommandSource;

import static de.thedead2.customadvancements.util.core.ModHelper.reloadAll;

public class ReloadCommand extends ModCommand {


    protected ReloadCommand(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    public static void register() {
        newModCommand("reload", (commandContext) -> {
            CommandSource source = commandContext.getSource();
            source.sendFeedback(TranslationKeyProvider.chatMessage("reload_started"), false);

            reloadAll(source.getServer());

            source.sendFeedback(TranslationKeyProvider.chatMessage("reload_successful"), false);
            return COMMAND_SUCCESS;
        });
    }
}
