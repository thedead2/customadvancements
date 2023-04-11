package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static de.thedead2.customadvancements.util.ModHelper.*;
import static de.thedead2.customadvancements.util.language.TranslationKeyProvider.chatMessage;

public class ReloadCommand {
    public ReloadCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("reload").executes((command) -> {
            CommandSourceStack source = command.getSource();

            Thread backgroundThread = new Thread(MOD_NAME){
                @Override
                public void run() {
                    source.sendSuccess(chatMessage("reload_started"), false);

                    reloadAll(source.getServer());

                    source.sendSuccess(chatMessage("reload_successful"), false);
                }
            };

            backgroundThread.setDaemon(true);
            backgroundThread.setPriority(5);
            backgroundThread.start();
            return 1;
        })));
    }
}
