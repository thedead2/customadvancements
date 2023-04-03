package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class GenerateGameAdvancementsCommand {


    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final Timer timer = new Timer();


    public GenerateGameAdvancementsCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("advancements").executes((command) -> generateGameAdvancements(command.getSource())))));
    }


    private int generateGameAdvancements(CommandSourceStack source) {

        Thread backgroundThread = new Thread(MOD_NAME) {
            @Override
            public void run() {
                timer.start();
                LOGGER.info("Starting to generate files for game advancements...");
                source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Starting to generate files for game advancements..."), false);

                FileHandler.createDirectory(DIR_PATH.toFile());

                ALL_DETECTED_GAME_ADVANCEMENTS.forEach((advancement, advancementData) -> {
                    try {
                        AdvancementHandler.writeAdvancementToFile(advancement, advancementData);
                        COUNTER.getAndIncrement();
                    }
                    catch (IOException e) {
                        LOGGER.error("Unable to write {} to file!", advancement);
                        source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write " + advancement + " to file!"));
                        CrashHandler.getInstance().addCrashDetails("Unable to write resource location to file!", Level.WARN, e);
                        e.printStackTrace();
                    }
                });
                LOGGER.info("Generating {} files for game advancements took {} ms", COUNTER.get(), timer.getTime());
                source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Generated " + COUNTER + " files for game advancements successfully!"), false);
                COUNTER.set(0);
                timer.stop(true);

                reloadAll(source.getServer());
            }
        };

        backgroundThread.setDaemon(true);
        backgroundThread.setPriority(3);
        backgroundThread.start();

        return 1;
    }




}
