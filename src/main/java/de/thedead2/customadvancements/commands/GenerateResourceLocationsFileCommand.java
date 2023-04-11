package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;

import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import static de.thedead2.customadvancements.util.ModHelper.*;
import static de.thedead2.customadvancements.util.ModHelper.MOD_NAME;
import static de.thedead2.customadvancements.util.language.TranslationKeyProvider.chatMessage;

public class GenerateResourceLocationsFileCommand {
    public GenerateResourceLocationsFileCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("resource_locations").executes((command) -> {
            CommandSourceStack source = command.getSource();

            source.sendSuccess(chatMessage("generating_rl_file"), false);
            LOGGER.info("Starting to write resource locations to file...");

            OutputStream fileOut = null;

            try {
                Path outputPath = Paths.get(DIR_PATH + "/resource_locations.txt");
                fileOut = Files.newOutputStream(outputPath);

                writeResourceLocations(fileOut);

                source.sendSuccess(chatMessage("generating_rl_file_success"), false);
                return 1;
            }
            catch (IOException e){
                LOGGER.error("Unable to write resource locations to file!");
                source.sendFailure(chatMessage("generating_rl_file_failed"));
                CrashHandler.getInstance().addCrashDetails("Unable to write resource locations to file!", Level.ERROR, e);
                e.printStackTrace();
                return -1;
            }
            finally {
                try {
                    assert fileOut != null;
                    fileOut.close();
                } catch (IOException e) {
                    LOGGER.warn("Unable to close OutputStream!");
                    CrashHandler.getInstance().addCrashDetails("Unable to close OutputStream!", Level.WARN, e);
                    e.printStackTrace();
                }
            }
        }))));
    }


    private void writeResourceLocations(OutputStream fileOut) throws IOException {
        Set<ResourceLocation> temp = new HashSet<>(ALL_DETECTED_GAME_ADVANCEMENTS.keySet());
        temp.addAll(CUSTOM_ADVANCEMENTS.keySet());

        for (ResourceLocation resourceLocation: temp) {
            String resourceLocation_as_String = resourceLocation.toString().replace(".json", "") + ",\n";

            InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

            FileHandler.writeToFile(inputStream, fileOut);
        }
    }
}
