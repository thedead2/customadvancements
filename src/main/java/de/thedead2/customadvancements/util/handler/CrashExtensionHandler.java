package de.thedead2.customadvancements.util.handler;

import com.google.common.io.ByteStreams;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ISystemReportExtender;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class CrashExtensionHandler implements ISystemReportExtender {

    private static CrashExtensionHandler instance;
    private IAdvancement activeAdvancement;
    private File activeFile;
    private final Set<IAdvancement> advancements = new HashSet<>();
    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private static final String SEPARATOR = "----------------------------------------------------------------------------------------------------------------------------------------------------\n";
    private final Set<CrashDetail> crashDetails = new HashSet<>();
    private boolean isFault = false;

    public CrashExtensionHandler(){
        instance = this;
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
        this.getFault(stringBuilder);
        this.getActiveAdvancement(stringBuilder);
        this.getActiveFile(stringBuilder);
        this.getLoadedAdvancements(stringBuilder);
        this.getRemovedAdvancements(stringBuilder);
    }

    private void getFault(StringBuilder stringBuilder){
        if(!this.crashDetails.isEmpty()) {
            stringBuilder.append("All detected execution errors related with " + MOD_NAME + ":").append("\n\n");
            this.crashDetails.forEach((crashDetail) -> {
                stringBuilder.append("Description: ").append(crashDetail.description()).append("\n");
                Throwable throwable = crashDetail.throwable();
                stringBuilder.append("Reported Error: ").append(throwable.getMessage()).append("\n");
                if(throwable.getCause() != null){
                    stringBuilder.append("Caused by: ").append(throwable.getCause()).append("\n");
                }
                stringBuilder.append("Level: ").append(crashDetail.level()).append("\n");
                stringBuilder.append("Caused Crash: ").append(crashDetail.responsibleForCrash() ? "Definitely!" : "Probably Not!").append("\n");
                stringBuilder.append("\n");
                StackTraceElement[] elements = throwable.getStackTrace();
                for (StackTraceElement element : elements) {
                    stringBuilder.append("\t").append(element).append("\n");
                }
                stringBuilder.append("\n\n");
                stringBuilder.append(SEPARATOR);
            });
        }
        else {
            stringBuilder.append("There was no execution error detected related with ").append(MOD_NAME).append("\n\n");
            stringBuilder.append(SEPARATOR);
        }
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
            stringBuilder.append("\n");
            stringBuilder.append("Is File: ").append(this.activeFile.isFile());
            stringBuilder.append("\n");
            stringBuilder.append("Is Readable: ").append(this.activeFile.canRead());
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

    public static CrashExtensionHandler getInstance(){
        return Objects.requireNonNullElseGet(instance, CrashExtensionHandler::new);
    }

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


    public void addCrashDetails(StringBuilder errorDescription, Level level, Throwable throwable, boolean responsibleForCrash){
        this.addCrashDetails(errorDescription.toString(), level, throwable, responsibleForCrash);
    }

    public void addCrashDetails(String errorDescription, Level level, Throwable throwable, boolean responsibleForCrash){
        AtomicBoolean i = new AtomicBoolean(false);
        AtomicBoolean j = new AtomicBoolean(false);
        AtomicBoolean k = new AtomicBoolean(false);
        this.crashDetails.forEach(crashDetail1 -> {
            i.set(crashDetail1.description().equals(errorDescription));
            j.set(crashDetail1.level().equals(level));
            k.set(crashDetail1.throwable().getMessage().equals(throwable.getMessage()));
        });
        if(!i.get() && !j.get() && !k.get()){
            CrashDetail crashDetail = new CrashDetail(errorDescription, level, throwable, responsibleForCrash);
            this.crashDetails.add(crashDetail);
        }
    }

    public void setFault(Throwable throwable){
        if(!this.isFault){
            for(StackTraceElement element : throwable.getStackTrace()){
                if(element.getClassName().contains("de.thedead2.customadvancements")){
                    this.addCrashDetails("A fatal error occurred executing " + MOD_NAME, Level.FATAL, throwable, true);
                    break;
                }
            }
            if(throwable.getCause() != null){
                this.setFault(throwable.getCause());
            }
        }
    }

    public void setFault(StackTraceElement[] stacktrace){
        if(!this.isFault){
            for(StackTraceElement element : stacktrace){
                if(element.getClassName().contains("de.thedead2.customadvancements")){
                    Throwable throwable = new Throwable("Recreated error");
                    throwable.setStackTrace(stacktrace);
                    this.addCrashDetails("A fatal error occurred executing " + MOD_NAME, Level.FATAL, throwable, true);
                    break;
                }
            }
        }
    }

    record CrashDetail(String description, Level level, Throwable throwable, boolean responsibleForCrash) {

    }
}
