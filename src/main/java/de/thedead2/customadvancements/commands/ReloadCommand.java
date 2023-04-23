package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.language.TranslationKeyProvider;
import net.minecraft.commands.CommandSourceStack;

import static de.thedead2.customadvancements.util.ModHelper.reloadAll;

public class ReloadCommand extends ModCommand {


    protected ReloadCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    public static void register() {
        newModCommand("reload", (commandContext) -> {
            var source = commandContext.getSource();
            source.sendSuccess(TranslationKeyProvider.chatMessage("reload_started"), false);

            reloadAll(source.getServer());

            source.sendSuccess(TranslationKeyProvider.chatMessage("reload_successful"), false);
            return COMMAND_SUCCESS;
        });
    }
}
