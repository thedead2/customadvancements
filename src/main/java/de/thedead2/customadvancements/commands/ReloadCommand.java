package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class ReloadCommand {
    public ReloadCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("reload").executes((command) -> {
            CommandSourceStack source = command.getSource();

            Thread backgroundThread = new Thread(MOD_NAME){
                @Override
                public void run() {
                    source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Reloading..."), false);

                    reloadAll(source.getServer());

                    source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Reload complete!"), false);
                }
            };

            backgroundThread.setDaemon(true);
            backgroundThread.setPriority(5);
            backgroundThread.start();
            return 1;
        })));
    }
}
