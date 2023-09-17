package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.FileHandler;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;

import net.minecraft.command.CommandSource;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class GenerateResourceLocationsFileCommand extends ModCommand {

    protected GenerateResourceLocationsFileCommand(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    public static void register() {
        newModCommand( "generate/resource_locations", (command) -> {
            CommandSource source = command.getSource();

            source.sendFeedback(TranslationKeyProvider.chatMessage("generating_rl_file"), false);
            LOGGER.info("Starting to write resource locations to file...");

            OutputStream fileOut = null;

            try {
                Path outputPath = Paths.get(DIR_PATH + "/resource_locations.txt");
                fileOut = Files.newOutputStream(outputPath);

                writeResourceLocations(fileOut);

                source.sendFeedback(TranslationKeyProvider.chatMessage("generating_rl_file_success"), false);
                return COMMAND_SUCCESS;
            }
            catch (IOException e){
                source.sendErrorMessage(TranslationKeyProvider.chatMessage("generating_rl_file_failed", TextFormatting.RED));
                CrashHandler.getInstance().handleException("Unable to write resource locations to file!", e, Level.ERROR, true);
                return COMMAND_FAILURE;
            }
            finally {
                try {
                    assert fileOut != null;
                    fileOut.close();
                }
                catch (IOException e) {
                    CrashHandler.getInstance().handleException("Unable to close OutputStream!", e, Level.WARN);
                }
                CrashHandler.getInstance().setActiveFile(null);
            }
        });
    }


    private static void writeResourceLocations(OutputStream fileOut) throws IOException {
        Set<ResourceLocation> temp = new HashSet<>(CustomAdvancementManager.ALL_ADVANCEMENTS_RESOURCE_LOCATIONS);
        temp.addAll(CUSTOM_ADVANCEMENTS.keySet());

        for (ResourceLocation resourceLocation: temp) {
            String resourceLocation_as_String = resourceLocation.toString().replace(".json", "") + ",\n";

            InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

            FileHandler.writeToFile(inputStream, fileOut);
        }
    }
}
