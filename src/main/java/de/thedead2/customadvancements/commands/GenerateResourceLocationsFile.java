package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.util.ModHelper;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.thedead2.customadvancements.util.ModHelper.*;
import static de.thedead2.customadvancements.util.ModHelper.MOD_NAME;

public class GenerateResourceLocationsFile {
    public GenerateResourceLocationsFile(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("resource_locations").executes((command) -> printResourceLocations(command.getSource())))));
    }

    public int printResourceLocations(CommandSourceStack source) {

        source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Starting to write resource locations to file..."), false);
        CustomAdvancements.LOGGER.info("Starting to write resource locations to file...");

        OutputStream fileOut = null;
        try {
            Path outputPath = Paths.get(DIR_PATH + "/resource_locations.txt");
            fileOut = Files.newOutputStream(outputPath);

            for (ResourceLocation resourceLocation: ALL_DETECTED_GAME_ADVANCEMENTS.keySet()) {
                String resourceLocation_as_String = resourceLocation.toString() + ",\n";

                InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

                try {
                    int input;
                    while ((input = inputStream.read()) != -1){
                        fileOut.write(input);
                    }
                }
                catch (IOException e) {
                    CustomAdvancements.LOGGER.error("Unable to write file: " + outputPath.getFileName());
                    source.sendFailure(Component.literal("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
                    e.printStackTrace();
                }
                finally {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e){
                        CustomAdvancements.LOGGER.warn("Unable to close InputStream!");
                        e.printStackTrace();
                    }
                }
            }

            for (ResourceLocation resourceLocation: CUSTOM_ADVANCEMENTS.keySet()){
                String resourceLocation_as_String = resourceLocation.toString().replace(".json", "") + ",\n";

                InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

                try {
                    int input;
                    while ((input = inputStream.read()) != -1){
                        fileOut.write(input);
                    }
                }
                catch (IOException e) {
                    CustomAdvancements.LOGGER.error("Unable to write file: " + outputPath.getFileName());
                    source.sendFailure(Component.literal("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
                    e.printStackTrace();
                }
                finally {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e){
                        CustomAdvancements.LOGGER.warn("Unable to close InputStream!");
                        e.printStackTrace();
                    }
                }
            }
            source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Finished!"), false);
            return 1;
        }
        catch (IOException e){
            CustomAdvancements.LOGGER.error("Unable to write resource locations to file!");
            source.sendFailure(Component.literal("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
            e.printStackTrace();
            return -1;
        }
        finally {
            try {
                assert fileOut != null;
                fileOut.close();
            }
            catch (IOException e) {
                CustomAdvancements.LOGGER.warn("Unable to close OutputStream!");
                e.printStackTrace();
            }
        }
    }
}
