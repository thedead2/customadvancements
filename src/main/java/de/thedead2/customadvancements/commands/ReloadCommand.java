package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.File;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class ReloadCommand {
    public ReloadCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("reload").executes((command) -> reload(command.getSource()))));
    }

    private int reload(CommandSourceStack source) {
        source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Starting to reload data..."), false);
        CustomAdvancements.LOGGER.info("Starting to reload data...");

        clearAll();

        FILE_HANDLER.checkForMainDirectories();
        FILE_HANDLER.readFiles(new File(DIR_PATH));

        source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Reload complete! Please use /reload to reload all advancements"), false);
        CustomAdvancements.LOGGER.info("Reload complete.");
        return 1;
    }
}
