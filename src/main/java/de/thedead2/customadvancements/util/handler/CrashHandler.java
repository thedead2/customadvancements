package de.thedead2.customadvancements.util.handler;

import com.google.common.io.ByteStreams;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ISystemReportExtender;
import net.minecraftforge.logging.CrashReportExtender;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class CrashHandler implements ISystemReportExtender {

    private static CrashHandler instance;
    private IAdvancement activeAdvancement;
    private File activeFile;
    private final Set<IAdvancement> advancements = new HashSet<>();
    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private static final String SEPARATOR = "----------------------------------------------------------------------------------------------------------------------------------------------------\n";
    private final Set<CrashDetail> crashDetails = new HashSet<>();

    private CrashHandler(){
        instance = this;
    }

    public static CrashHandler getInstance(){
        return Objects.requireNonNullElseGet(instance, CrashHandler::new);
    }

    @Override
    public String getLabel() {
        return "\n\n" + MOD_NAME;
    }

    @Override
    public String get() {
        StringBuilder stringBuilder = new StringBuilder();
        this.getDetails(stringBuilder);

        return stringBuilder.toString();
    }

    private void getDetails(StringBuilder stringBuilder){
        this.getModInformation(stringBuilder);
        this.getExecutionErrors(stringBuilder);
        this.getActiveAdvancement(stringBuilder);
        this.getActiveFile(stringBuilder);
        this.getLoadedAdvancements(stringBuilder);
        this.getRemovedAdvancements(stringBuilder);
    }

    private void getModInformation(StringBuilder stringBuilder){
        stringBuilder.append("\n");
        stringBuilder.append("- Mod ID: ").append(MOD_ID).append("\n");
        stringBuilder.append("- Version: ").append(MOD_VERSION).append("\n");
        stringBuilder.append("- Path Separator: ").append(PATH_SEPARATOR).append("\n");
        stringBuilder.append("- Main Path: ").append(DIR_PATH).append("\n");
        stringBuilder.append("\n\n");
    }

    private void getExecutionErrors(StringBuilder stringBuilder){
        if(!this.crashDetails.isEmpty()) {
            stringBuilder.append("All detected execution errors related to " + MOD_NAME + ":").append("\n");
            stringBuilder.append("---------------------------------------------------------------").append("\n\n");
            Set<CrashDetail> temp = new HashSet<>();
            this.crashDetails.forEach((crashDetail) -> {
                if(crashDetail.level().equals(Level.FATAL)){
                    crashDetail.printStackTrace(stringBuilder);
                    temp.add(crashDetail);
                }
            });
            this.crashDetails.removeAll(temp);

            if(this.crashDetails.size() <= 5){
                this.crashDetails.forEach((crashDetail) -> crashDetail.printStackTrace(stringBuilder));
            }
            else if(this.crashDetails.size() <= 10){
                this.crashDetails.forEach((crashDetail) -> {
                    if(crashDetail.level.isInRange(Level.ERROR, Level.FATAL)){
                        crashDetail.printStackTrace(stringBuilder);
                    }
                });
            }
            else {
                this.crashDetails.forEach((crashDetail) -> {
                    if(crashDetail.level.equals(Level.FATAL)){
                        crashDetail.printStackTrace(stringBuilder);
                    }
                });
            }

        }
        else {
            stringBuilder.append(SEPARATOR);
            stringBuilder.append("There was no execution error detected related to ").append(MOD_NAME).append("!\n\n");
            stringBuilder.append(SEPARATOR);
        }
    }


    private void getActiveAdvancement(StringBuilder stringBuilder) {
        stringBuilder.append("Currently active advancement: ");
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

    private void getActiveFile(StringBuilder stringBuilder) {
        stringBuilder.append("Currently active file: ");
        if(this.activeFile != null){
            stringBuilder.append(this.activeFile.getName());
            stringBuilder.append("\n");
            stringBuilder.append("- Is File: ").append(this.activeFile.isFile());
            stringBuilder.append("\n");
            stringBuilder.append("- Is Readable: ").append(this.activeFile.canRead());
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

    private void getLoadedAdvancements(StringBuilder stringBuilder){
        stringBuilder.append("All loaded Advancements: ");
        List<String> temp = new ArrayList<>();
        if(!this.advancements.isEmpty()){
            stringBuilder.append("\n");
            this.advancements.forEach(advancement -> temp.add("- " + advancement.toString()));
            stringBuilder.append(String.join(",\n", temp));
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
    }

    private void getRemovedAdvancements(StringBuilder stringBuilder){
        stringBuilder.append("All removed Advancements: ");
        List<String> temp = new ArrayList<>();
        if(!this.removedAdvancements.isEmpty()){
            stringBuilder.append("\n");
            this.removedAdvancements.forEach(advancement -> temp.add("- " + advancement.toString()));
            stringBuilder.append(String.join(",\n", temp));
        }
        else {
            stringBuilder.append("NONE");
        }
        stringBuilder.append("\n\n");
        stringBuilder.append(SEPARATOR);
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


    public void addCrashDetails(String errorDescription, Level level, Throwable throwable){
        this.addCrashDetails(errorDescription, level, throwable, false);
    }

    private void addCrashDetails(String errorDescription, Level level, Throwable throwable, boolean responsibleForCrash){
        AtomicBoolean i = new AtomicBoolean(false);
        AtomicBoolean j = new AtomicBoolean(false);
        AtomicBoolean k = new AtomicBoolean(false);
        for(CrashDetail crashDetail1 : this.crashDetails){
            i.set(crashDetail1.description().equals(errorDescription));
            j.set(crashDetail1.level().equals(level));
            if(crashDetail1.throwable().getMessage() != null) {
                k.set(crashDetail1.throwable().getMessage().equals(throwable.getMessage()));

                if(i.get() && j.get() && k.get()){
                    return;
                }
            }
            if(i.get() && j.get()){
                return;
            }
        }
        CrashDetail crashDetail = new CrashDetail(errorDescription, level, throwable, responsibleForCrash);
        this.crashDetails.add(crashDetail);
    }

    public void resolveCrash(Throwable throwable){
        for(StackTraceElement element : throwable.getStackTrace()){
            if(element.getClassName().contains("de.thedead2.customadvancements")){
                this.addCrashDetails("A fatal error occurred executing " + MOD_NAME, Level.FATAL, throwable, true);
                break;
            }
        }
        if(throwable.getCause() != null){
            this.resolveCrash(throwable.getCause());
        }
    }

    public void resolveCrash(StackTraceElement[] stacktrace, String input){
        Throwable throwable = this.resolveThrowable(input);
        throwable.setStackTrace(stacktrace);
        this.resolveCrash(throwable);
    }

    private Throwable resolveThrowable(String input){
        Class<?> exceptionClass;
        Throwable throwable;

        if(input != null) {
            int i = input.lastIndexOf(":");
            String temp = i != -1 ? input.substring(i) : "";
            String className = input.replaceAll(temp, "");
            try {
                exceptionClass = Class.forName(className);

                String exceptionMessage = input.substring(i + 1);
                Object object;
                try {
                    object = exceptionClass.getDeclaredConstructor(exceptionMessage.getClass()).newInstance(exceptionMessage);
                }
                catch (NoSuchMethodException e){
                    object = exceptionClass.getDeclaredConstructor().newInstance();
                }

                if(object instanceof Throwable t) {
                    throwable = t;
                }
                else {
                    throw new IllegalStateException();
                }
            }
            catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                   NoSuchMethodException | IllegalStateException ignored) {
                throwable = new Throwable(input);
            }
        }
        else {
            throwable = new Throwable("Unknown Exception");
        }
        return throwable;
    }

    private record CrashDetail(String description, Level level, Throwable throwable, boolean responsibleForCrash) {

        private void printStackTrace(StringBuilder stringBuilder){
            stringBuilder.append("Description: ").append(this.description).append("\n");
            Throwable throwable = this.throwable;
            stringBuilder.append("Reported Error: ").append(throwable.getMessage()).append("\n");
            if(throwable.getCause() != null){
                stringBuilder.append("Caused by: ").append(throwable.getCause()).append("\n");
            }
            stringBuilder.append("Level: ").append(this.level).append("\n");
            stringBuilder.append("Caused Crash: ").append(this.responsibleForCrash ? "Definitely!" : "Probably Not!").append("\n");
            stringBuilder.append("\n");
            if(this.level.equals(Level.FATAL)){
                stringBuilder.append(CrashReportExtender.generateEnhancedStackTrace(throwable));
            }
            else if (this.level.equals(Level.ERROR)) {
                stringBuilder.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 6)));
            }
            else if (this.level.equals(Level.WARN)) {
                stringBuilder.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 3)));
            }
            else if (this.level.equals(Level.DEBUG)) {
                stringBuilder.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 1)));
            }

            stringBuilder.append("\n\n");
            stringBuilder.append(SEPARATOR);
        }

        private Throwable trimStacktrace(Throwable throwable, int length) {
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            StackTraceElement[] astacktraceelement = new StackTraceElement[length];
            System.arraycopy(stackTraceElements, 0, astacktraceelement, 0, astacktraceelement.length);
            throwable.setStackTrace(astacktraceelement);
            return throwable;
        }
    }
}
