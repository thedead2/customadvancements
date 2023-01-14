package de.thedead2.customadvancements.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.exceptions.FileWriteException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.handler.FileHandler.createDirectory;
import static de.thedead2.customadvancements.util.handler.FileHandler.writeFile;
import static de.thedead2.customadvancements.util.ModHelper.*;

public class GenerateGameAdvancementsCommand {

    private static final List<String> FOLDER_NAMES = new ArrayList<>();
    private static final AtomicInteger COUNTER = new AtomicInteger();


    public GenerateGameAdvancementsCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("game_advancements").executes((command) -> generateGameAdvancements(command.getSource())))));
    }


    private int generateGameAdvancements(CommandSourceStack source) {

        Thread backgroundThread = new Thread(MOD_NAME) {
            @Override
            public void run() {
                LOGGER.info("Starting to generate files for game advancements...");
                source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Starting to generate files for game advancements..."), false);

                createDirectory(new File(DIR_PATH));

                ALL_DETECTED_GAME_ADVANCEMENTS.forEach((advancement, advancementData) -> {
                    LOGGER.debug("Generating file: " + advancement);

                    FOLDER_NAMES.clear();
                    String basePath = DIR_PATH + "/" + advancement.getNamespace();

                    createDirectory(new File(basePath));

                    try {
                        writeFile(createInput(advancementData), resolvePath(basePath, advancement.getPath()));
                        COUNTER.getAndIncrement();
                    }
                    catch (FileWriteException e){
                        LOGGER.error("Unable to write advancement {} to file!", advancement);
                        source.sendFailure(Component.literal("§c [" + MOD_NAME + "]: Unable to write advancement " + advancement + " to file! Check log for info!"));
                        e.printStackTrace();
                    }
                });
                LOGGER.info("Generated {} files for game advancements", COUNTER.get());
                source.sendSuccess(Component.literal("[" + MOD_NAME + "]: Generated " + COUNTER + " files for game advancements successfully!"), false);
                COUNTER.set(0);

                reloadAll(source.getServer());
            }
        };

        backgroundThread.setDaemon(true);
        backgroundThread.setPriority(3);
        backgroundThread.start();

        return 1;
    }


    private void discoverSubDirectories(String pathIn){
        if (pathIn.contains("/")){
            String temp1 = pathIn.substring(pathIn.indexOf("/"));
            String temp2 = pathIn.replace(temp1 + "/", "");
            String temp3 = temp2.replace(temp2.substring(temp2.indexOf("/")), "");
            FOLDER_NAMES.add(temp3);

            String next = pathIn.replace((temp3 + "/"), "");
            discoverSubDirectories(next);
        }
    }


    private Path resolvePath(String basePath, String advancementPath){
        if(advancementPath.contains("/")){
            String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
            FOLDER_NAMES.add(subStringDirectory);

            String nextSubString = advancementPath.replace(subStringDirectory + "/", "");
            discoverSubDirectories(nextSubString);

            for(String folderName: FOLDER_NAMES){
                basePath = basePath + "/" + folderName;
                createDirectory(new File(basePath));
            }

            return Path.of(basePath + advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
        }
        else {
            return Path.of(basePath + "/" + advancementPath + ".json");
        }
    }


    private InputStream createInput(JsonElement advancementData){
        StringBuilder stringBuilder = new StringBuilder();

        for (char character : advancementData.toString().toCharArray()){
            if (character == '{' || character == ',' || character == '['){
                stringBuilder.append(character).append('\n');
            }
            else {
                stringBuilder.append(character);
            }
        }
        String temp = stringBuilder.toString();

        return new ByteArrayInputStream(temp.getBytes());
    }

}
