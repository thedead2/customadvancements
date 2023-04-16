package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.handler.FileHandler;
import de.thedead2.customadvancements.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class GenerateGameAdvancementsCommand extends ModCommand {


    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Timer timer = new Timer();

    protected GenerateGameAdvancementsCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }


    public static void register() {
        newModCommand("generate/advancement/all", (command) -> {
            var source = command.getSource();
            Thread backgroundThread = new Thread(MOD_NAME) {
                @Override
                public void run() {
                    timer.start();
                    LOGGER.info("Starting to generate files for game advancements...");
                    source.sendSuccess(TranslationKeyProvider.chatMessage("generating_game_advancements"), false);

                    FileHandler.createDirectory(DIR_PATH.toFile());

                    source.getServer().getAdvancements().getAllAdvancements().forEach((advancement) -> {
                        try {
                            AdvancementHandler.writeAdvancementToFile(advancement);
                            COUNTER.getAndIncrement();
                        }
                        catch (IOException e) {
                            source.sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancements_failed", ChatFormatting.RED, advancement.getId()));
                            CrashHandler.getInstance().handleException("Unable to write " + advancement.getId() + " to file!", e, Level.WARN);
                        }
                    });
                    LOGGER.info("Generating {} files for game advancements took {} ms", COUNTER.get(), timer.getTime());
                    source.sendSuccess(TranslationKeyProvider.chatMessage("generating_game_advancements_success", COUNTER.toString()), false);
                    COUNTER.set(0);
                    timer.stop(true);

                    reloadAll(source.getServer());
                }
            };

            backgroundThread.setDaemon(true);
            backgroundThread.setPriority(3);
            backgroundThread.start();
            return COMMAND_SUCCESS;
        });
    }
}
