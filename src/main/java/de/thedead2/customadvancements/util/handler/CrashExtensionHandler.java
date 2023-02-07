package de.thedead2.customadvancements.util.handler;

import com.google.common.io.ByteStreams;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ISystemReportExtender;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class CrashExtensionHandler implements ISystemReportExtender {

    private static CrashExtensionHandler instance;
    private IAdvancement activeAdvancement;
    private File activeFile;
    private final Set<IAdvancement> advancements = new HashSet<>();
    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private static final String SEPARATOR = "----------------------------------------------------------------------------------------------------------------------------------------------------\n";

    public CrashExtensionHandler(){
        instance = this;
        LOGGER.debug("Registered " + this.getClass().getName());
    }

    @Override
    public String getLabel() {
        return "\n\n" + MOD_NAME;
    }

    @Override
    public String get() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append("- Mod ID: ").append(MOD_ID).append("\n");
        stringBuilder.append("- Version: ").append(MOD_VERSION).append("\n");
        stringBuilder.append("- Path Separator: ").append(PATH_SEPARATOR).append("\n");
        stringBuilder.append("- Main Path: ").append(DIR_PATH).append("\n");
        stringBuilder.append("\n");
        stringBuilder.append(SEPARATOR);


        this.getDetails(stringBuilder);

        return stringBuilder.toString();
    }

    public void getDetails(StringBuilder stringBuilder){
        this.getActiveAdvancement(stringBuilder);
        this.getActiveFile(stringBuilder);
        this.getLoadedAdvancements(stringBuilder);
        this.getRemovedAdvancements(stringBuilder);
    }

    private void getActiveAdvancement(StringBuilder stringBuilder) {
        stringBuilder.append("- Currently active advancement: ");
        if(this.activeAdvancement != null){
            stringBuilder.append(this.activeAdvancement.getResourceLocation());
            stringBuilder.append("\n\n");
            stringBuilder.append(this.activeAdvancement.getJsonObject());
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
    }

    public void getActiveFile(StringBuilder stringBuilder) {
        stringBuilder.append("- Currently active file: ");
        if(this.activeFile != null){
            stringBuilder.append(this.activeFile.getName());
            stringBuilder.append("\n\n");
            try {
                InputStream fileInput = Files.newInputStream(this.activeFile.toPath());
                String file_data = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
                stringBuilder.append(file_data);
                fileInput.close();
            }
            catch (Exception ignored) {}
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
    }

    public void getLoadedAdvancements(StringBuilder stringBuilder){
        stringBuilder.append("- All loaded Advancements: ");
        if(!this.advancements.isEmpty()){
            stringBuilder.append("\n");
            this.advancements.forEach(advancement -> stringBuilder.append(advancement.toString()).append(",").append("\n"));
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
    }

    public void getRemovedAdvancements(StringBuilder stringBuilder){
        stringBuilder.append("- All removed Advancements: ");
        if(!this.removedAdvancements.isEmpty()){
            stringBuilder.append("\n");
            this.removedAdvancements.forEach(advancement -> stringBuilder.append(advancement).append(",").append("\n"));
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
    }

    public static CrashExtensionHandler getInstance(){return instance;}

    public void setActiveAdvancement(IAdvancement advancement){
        this.activeAdvancement = advancement;
        if(advancement != null){
            this.advancements.add(advancement);
        }
    }

    public void setActiveFile(File file){
        this.activeFile = file;
    }

    public void addRemovedAdvancement(ResourceLocation advancement){
        this.removedAdvancements.add(advancement);
    }
}
