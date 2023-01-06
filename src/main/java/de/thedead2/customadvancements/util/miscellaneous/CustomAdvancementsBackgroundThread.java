package de.thedead2.customadvancements.util.miscellaneous;

import de.thedead2.customadvancements.CustomAdvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.commands.GenerateAdvancementsCommand.FOLDER_NAMES;
import static de.thedead2.customadvancements.util.FileHandler.createDirectory;
import static de.thedead2.customadvancements.util.FileHandler.writeFile;
import static de.thedead2.customadvancements.util.ModHelper.*;

public class CustomAdvancementsBackgroundThread extends Thread {

    public static AtomicInteger counter = new AtomicInteger();

    @Override
    public void run() {
        createDirectory(new File(DIR_PATH));

        ALL_DETECTED_GAME_ADVANCEMENTS.forEach((advancement, advancementData) -> {
            FOLDER_NAMES.clear();
            String advancementNamespace = advancement.getNamespace();
            String advancementPath = advancement.getPath();


            CustomAdvancements.LOGGER.debug("Generating file: " + advancement);

            createDirectory(new File(DIR_PATH + "/" + advancementNamespace));

            File advancementJson;

            if(advancementPath.contains("/")){
                String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
                FOLDER_NAMES.add(subStringDirectory);

                String nextSubString = advancementPath.replace(subStringDirectory + "/", "");
                FILE_HANDLER.discoverSubDirectories(nextSubString);

                String basePath = DIR_PATH + "/" + advancementNamespace;

                for(String folderName: FOLDER_NAMES){
                    basePath = basePath + "/" + folderName;
                    createDirectory(new File(basePath));
                }

                advancementJson = new File(basePath + advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
            }
            else {
                advancementJson = new File(DIR_PATH + "/" + advancementNamespace + "/" + advancementPath + ".json");
            }

            StringBuilder stringBuilder = new StringBuilder();

            for (char character:advancementData.toString().toCharArray()){
                if (character == '{' || character == ',' || character == '['){
                    stringBuilder.append(character).append('\n');
                }
                else {
                    stringBuilder.append(character);
                }
            }
            String temp = stringBuilder.toString();
            InputStream inputStream = new ByteArrayInputStream(temp.getBytes());

            try {
                writeFile(inputStream, advancementJson.toPath());
                counter.getAndIncrement();
            }
            catch (FileWriteException e){
                CustomAdvancements.LOGGER.error("Unable to write advancement {} to file!", advancement);
                e.printStackTrace();
                if(FMLEnvironment.dist.isClient()){
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§c [" + MOD_NAME + "]: Unable to write advancement " + advancement + " to file! Check log for info!"));
                }
            }
        });
        CustomAdvancements.LOGGER.info("Generated {} files for game advancements", counter.get());

        if(FMLEnvironment.dist.isClient()) {
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("[" + MOD_NAME + "]: Generated " + counter + " files for game advancements successfully!"));
        }
        counter.set(0);
    }
}
