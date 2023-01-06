package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.miscellaneous.CustomAdvancementsBackgroundThread;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.ModHelper.MOD_NAME;

public class GenerateAdvancementsCommand {

    public static final List<String> FOLDER_NAMES = new ArrayList<>();

    public GenerateAdvancementsCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("game_advancements").executes((command) -> generateGameAdvancements(command.getSource())))));
    }

    private int generateGameAdvancements(CommandSourceStack source) {
        CustomAdvancements.LOGGER.info("Starting to generate files for game advancements...");
        source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Starting to generate files for game advancements..."), false);

        CustomAdvancementsBackgroundThread backgroundThread = new CustomAdvancementsBackgroundThread();
        backgroundThread.setDaemon(true);
        backgroundThread.setPriority(3);
        backgroundThread.setName(MOD_ID);
        backgroundThread.start();

        return 1;
    }

}
