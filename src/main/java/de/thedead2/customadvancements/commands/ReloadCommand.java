package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;
import net.minecraft.commands.CommandSourceStack;

import static de.thedead2.customadvancements.util.core.ModHelper.reloadAll;

public class ReloadCommand extends ModCommand {


    protected ReloadCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, LiteralArgumentBuilder<CommandSourceStack> shortLiteralArgumentBuilder) {
        super(literalArgumentBuilder, shortLiteralArgumentBuilder);
    }

    public static void register() {
        Builder.newModCommand("reload", (commandContext) -> {
            var source = commandContext.getSource();
            source.sendSuccess(TranslationKeyProvider.chatMessage("reload_started"), false);

            reloadAll(source.getServer());

            source.sendSuccess(TranslationKeyProvider.chatMessage("reload_successful"), false);
            return COMMAND_SUCCESS;
        });
    }
}
