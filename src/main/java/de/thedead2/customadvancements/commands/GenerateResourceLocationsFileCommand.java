package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class GenerateResourceLocationsFileCommand {

    public GenerateResourceLocationsFileCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MOD_ID).then(Commands.literal("generate").then(Commands.literal("resource_locations").executes((command) -> printResourceLocations(command.getSource())))));
    }

    private int printResourceLocations(CommandSourceStack source) {

        source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Starting to write resource locations to file..."), false);
        LOGGER.info("Starting to write resource locations to file...");

        OutputStream fileOut = null;
        try {
            Path outputPath = Paths.get(DIR_PATH + "/resource_locations.txt");
            fileOut = Files.newOutputStream(outputPath);

            writeToFile(fileOut, ALL_DETECTED_GAME_ADVANCEMENTS.keySet(), outputPath, source);
            writeToFile(fileOut, CUSTOM_ADVANCEMENTS.keySet(), outputPath, source);

            source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Finished!"), false);
            return 1;
        }
        catch (IOException e){
            LOGGER.error("Unable to write resource locations to file!");
            source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
            e.printStackTrace();
            return -1;
        }
        finally {
            try {
                assert fileOut != null;
                fileOut.close();
            }
            catch (IOException e) {
                LOGGER.warn("Unable to close OutputStream!");
                e.printStackTrace();
            }
        }
    }


    private void writeToFile(OutputStream fileOut, Set<ResourceLocation> setIn, Path outputPath, CommandSourceStack source){
        for (ResourceLocation resourceLocation: setIn) {
            String resourceLocation_as_String = resourceLocation.toString().replace(".json", "") + ",\n";

            InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

            try {
                int input;
                while ((input = inputStream.read()) != -1){
                    fileOut.write(input);
                }
            }
            catch (IOException e) {
                LOGGER.error("Unable to write file: " + outputPath.getFileName());
                source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
                e.printStackTrace();
            }
            finally {
                try {
                    inputStream.close();
                }
                catch (IOException e){
                    LOGGER.warn("Unable to close InputStream!");
                    e.printStackTrace();
                }
            }
        }
    }
}
