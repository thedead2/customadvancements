package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.language.TranslationKeyProvider;
import net.minecraft.commands.CommandSourceStack;

import static de.thedead2.customadvancements.util.ModHelper.MOD_NAME;
import static de.thedead2.customadvancements.util.ModHelper.reloadAll;

public class ReloadCommand extends ModCommand {


    protected ReloadCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    public static void register() {
        newModCommand("reload", (commandContext) -> {
            Thread backgroundThread = new Thread(MOD_NAME){
                final CommandSourceStack source = commandContext.getSource();
                @Override
                public void run() {
                    source.sendSuccess(TranslationKeyProvider.chatMessage("reload_started"), false);

                    reloadAll(source.getServer());

                    source.sendSuccess(TranslationKeyProvider.chatMessage("reload_successful"), false);
                }
            };

            backgroundThread.setDaemon(true);
            backgroundThread.setPriority(5);
            backgroundThread.start();
            return COMMAND_SUCCESS;
        });
    }
}
