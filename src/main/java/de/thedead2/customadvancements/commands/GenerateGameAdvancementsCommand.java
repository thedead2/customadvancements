package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.io.AdvancementHandler;
import de.thedead2.customadvancements.util.io.FileHandler;
import de.thedead2.customadvancements.util.localisation.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class GenerateGameAdvancementsCommand extends ModCommand {

    protected GenerateGameAdvancementsCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, LiteralArgumentBuilder<CommandSourceStack> shortLiteralArgumentBuilder) {
        super(literalArgumentBuilder, shortLiteralArgumentBuilder);
    }


    public static void register() {
        Builder.newModCommand("generate/advancement/all", (command) -> {
            CommandSourceStack source = command.getSource();

            source.getServer().executeIfPossible(() -> {
                LOGGER.info("Starting to generate files for game advancements...");
                source.sendSuccess(() -> TranslationKeyProvider.chatMessage("generating_game_advancements"), false);

                long startTime = System.currentTimeMillis();
                AtomicInteger counter = new AtomicInteger();

                FileHandler.createDirectoryIfNecessary(DIR_PATH.toFile());

                source.getServer().getAdvancements().getAllAdvancements().forEach((advancement) -> {
                    try {
                        AdvancementHandler.writeAdvancementToFile(advancement);
                        counter.getAndIncrement();
                    }
                    catch (IOException e) {
                        source.sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancements_failed", ChatFormatting.RED, advancement.getId()));
                        CrashHandler.getInstance().handleException("Unable to write " + advancement.getId() + " to file!", e, Level.WARN);
                    }
                });

                LOGGER.info("Generating {} files for game advancements took {} ms", counter.get(), System.currentTimeMillis() - startTime);
                source.sendSuccess(() -> TranslationKeyProvider.chatMessage("generating_game_advancements_success", counter.get()), false);

                reloadAll(source.getServer());
            });

            return COMMAND_SUCCESS;
        });
    }
}
