package de.thedead2.customadvancements.util.handler;

import com.google.common.io.ByteStreams;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.logger.ConsoleColors;
import joptsimple.internal.Strings;
import net.minecraft.CrashReportCategory;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ISystemReportExtender;
import net.minecraftforge.logging.CrashReportExtender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class CrashHandler implements ISystemReportExtender {

    private static CrashHandler instance;
    private IAdvancement activeAdvancement;
    private Advancement activeGameAdvancement;
    private File activeFile;
    private final Set<IAdvancement> advancements = new HashSet<>();
    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private final Set<CrashDetail> crashDetails = new HashSet<>();
    private StringBuilder stringBuilder;

    private CrashHandler(){
        instance = this;
        LogManager.getLogger().debug("Registered CrashHandler!");
    }

    public static CrashHandler getInstance(){
        return Objects.requireNonNullElseGet(instance, CrashHandler::new);
    }

    @Override
    public String getLabel() {
        return "\n\n" + "-- " + MOD_NAME + " --" + "\n" + "Details";
    }

    @Override
    public String get() {
        this.stringBuilder = new StringBuilder();
        return this.getDetails();
    }

    private String getDetails(){
        this.getModInformation();
        if(this.activeAdvancement != null || this.activeGameAdvancement != null) {
            this.getActiveAdvancement();
        }
        if(this.activeFile != null) {
            this.getActiveFile();
        }
        this.getExecutionErrors();
        this.getLoadedAdvancements();
        this.getRemovedAdvancements();

        this.stringBuilder.append("\n\n");
        return this.stringBuilder.toString();
    }

    private void addDetail(String name, Object in){
        this.stringBuilder.append("\t").append(name);
        if(in != null){
            this.stringBuilder.append(": ");
            if(in instanceof Throwable throwable){
                this.stringBuilder.append(throwable.getMessage());
            }
            else {
                this.stringBuilder.append(in);
            }
        }
        this.stringBuilder.append("\n");
    }

    private void addDetail(String name){
        this.addDetail(name, null);
    }

    private void addSection(String name){
        this.stringBuilder.append("\n");
        this.stringBuilder.append(name).append(":");
        this.stringBuilder.append("\n");
    }

    private void getModInformation(){
        this.stringBuilder.append("\n");
        this.addDetail("Mod ID", MOD_ID);
        this.addDetail("Version", MOD_VERSION);
        this.addDetail("Main Path", DIR_PATH);
        if(this.activeAdvancement == null && this.activeGameAdvancement == null) {
            this.getActiveAdvancement();
        }
        if(this.activeFile == null) {
            this.getActiveFile();
        }
    }

    private void getErrorDetails(CrashDetail crashDetail){
        this.stringBuilder.append("\n");
        this.addErrorDetails(crashDetail);
        this.stringBuilder.append("\n");
        this.addDetail("Stacktrace", this.addStackTrace(crashDetail));
        this.stringBuilder.append("\n").append(Strings.repeat('-', 200)).append("\n");
    }

    private void getExecutionErrors(){
        if(!this.crashDetails.isEmpty()){
            this.stringBuilder.append("\n").append(Strings.repeat('-', 200)).append("\n");
        }
        this.addSection("All detected execution errors related to " + MOD_NAME);
        if(!this.crashDetails.isEmpty()) {
            Set<CrashDetail> temp = new HashSet<>();
            this.crashDetails.forEach((crashDetail) -> {
                if(crashDetail.level().equals(Level.FATAL)){
                    this.getErrorDetails(crashDetail);
                    temp.add(crashDetail);
                }
            });
            this.crashDetails.removeAll(temp);

            if(this.crashDetails.size() <= 5){
                this.crashDetails.forEach(this::getErrorDetails);
            }
            else if(this.crashDetails.size() <= 10){
                this.crashDetails.forEach((crashDetail) -> {
                    if(crashDetail.level.isInRange(Level.ERROR, Level.FATAL)){
                        this.getErrorDetails(crashDetail);
                    }
                });
            }
            else {
                this.crashDetails.forEach((crashDetail) -> {
                    if(crashDetail.level.equals(Level.FATAL)){
                        this.getErrorDetails(crashDetail);
                    }
                });
            }

        }
        else {
            this.addDetail("There were no execution errors detected!");
        }
    }

    private void addErrorDetails(CrashDetail detail){
        Throwable throwable = detail.throwable();
        this.addDetail("Reported Error", getExceptionName(throwable));
        this.addDetail("Description", detail.description());
        if(throwable.getCause() != null){
            this.addDetail("Caused by", getExceptionName(throwable.getCause()));
        }
        this.addDetail("Level", detail.level());
        this.addDetail("Caused Crash", detail.responsibleForCrash() ? "Definitely! \n\t"
                + ConsoleColors.italic + "Please report this crash to the mod author: " + MOD_ISSUES_LINK + ConsoleColors.reset :
                "Probably Not!"
        );
    }

    private String getExceptionName(Throwable throwable){
        return throwable.getClass().getName() + (throwable.getMessage() != null ? (": " + throwable.getMessage()) : "");
    }

    private String addStackTrace(CrashDetail detail){
        Throwable throwable = detail.throwable();
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();

        if(detail.level().equals(Level.FATAL)){
            stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(throwable, false));
        }
        else if (detail.level().equals(Level.ERROR)) {
            stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 6), false));
        }
        else if (detail.level().equals(Level.WARN)) {
            stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 3), false));
        }
        else if (detail.level().equals(Level.DEBUG)) {
            stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 1), false));
        }
        String temp = stringBuilder2.toString().replaceAll("\t", "\t\t");
        return stringBuilder1.append(temp).toString();
    }

    private Throwable trimStacktrace(Throwable throwable, int length) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StackTraceElement[] astacktraceelement = new StackTraceElement[length];
        System.arraycopy(stackTraceElements, 0, astacktraceelement, 0, astacktraceelement.length);
        throwable.setStackTrace(astacktraceelement);
        return throwable;
    }



    private void getActiveAdvancement() {
        if(this.activeAdvancement != null || this.activeGameAdvancement != null){
            this.addSection("Currently active advancement");

            if(this.activeAdvancement != null){
                this.addDetail(this.activeAdvancement.getResourceLocation().toString(), this.activeAdvancement.getJsonObject());
            }
            else {
                this.addDetail(this.activeGameAdvancement.getId().toString(), this.activeGameAdvancement.deconstruct().serializeToJson());
            }

            if(this.activeFile == null){
                this.stringBuilder.append("\n");
            }
        }
        else {
            this.addDetail("Currently active advancement", "NONE");
        }
    }

    private void getActiveFile() {
        if(this.activeFile != null){
            this.addSection("Currently active file");
            this.addDetail("Name", this.activeFile.getName());
            this.addDetail("Is File", this.activeFile.isFile());
            this.addDetail("Is Readable", this.activeFile.canRead());
            try {
                InputStream fileInput = Files.newInputStream(this.activeFile.toPath());
                String file_data = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
                file_data = file_data.replaceAll("\n", "\n\t\t");
                this.addDetail("Data", "\n\t" + file_data);
                fileInput.close();
            }
            catch (Exception e) {
                this.addDetail("Data", "\n\t" + "ERROR while reading file data: " + e.getMessage());
            }
        }
        else {
            this.addDetail("Currently active file", "NONE");
        }
    }

    private void getLoadedAdvancements(){
        List<String> temp = new ArrayList<>();
        if(!this.advancements.isEmpty()){
            this.addSection("All loaded Advancements");
            int i = 0;
            for(IAdvancement advancement : this.advancements){
                StringBuilder builder = new StringBuilder();
                if(i != 0){
                    builder.append("\t");
                }
                else {
                    i++;
                }
                builder.append(advancement.toString());
                temp.add(builder.toString());
            }
            this.addDetail(String.join(",\n", temp));
        }
    }

    private void getRemovedAdvancements(){
        List<String> temp = new ArrayList<>();
        if(!this.removedAdvancements.isEmpty()){
            this.addSection("All removed Advancements");
            int i = 0;
            for(ResourceLocation advancement : this.removedAdvancements){
                StringBuilder builder = new StringBuilder();
                if(i != 0){
                    builder.append("\t");
                }
                else {
                    i++;
                }
                builder.append(advancement.toString());
                temp.add(builder.toString());
            }
            this.addDetail(String.join(",\n", temp));
        }
    }


    public <T> void setActiveAdvancement(T advancement){
        if(advancement instanceof IAdvancement activeAdvancement1){
            this.activeAdvancement = activeAdvancement1;
            if(!activeAdvancement1.getResourceLocation().toString().contains("recipes/")){
                this.advancements.add(activeAdvancement1);
            }
        }
        else if(advancement instanceof Advancement advancement1){
            this.activeGameAdvancement = advancement1;
        }
        else if(advancement == null) {
            this.activeAdvancement = null;
            this.activeGameAdvancement = null;
        }
    }

    public void setActiveFile(File file){
        this.activeFile = file;
    }

    public void addRemovedAdvancement(ResourceLocation advancement){
        if(!advancement.toString().contains("recipes/")) {
            this.removedAdvancements.add(advancement);
        }
    }

    public void addCrashDetails(String errorDescription, Level level, Throwable throwable){
        this.addCrashDetails(errorDescription, level, throwable, false);
    }

    public void addScreenCrash(CrashReportCategory.Entry crashReportCategory$Entry, Throwable exception){
        this.addCrashDetails("Error while rendering screen: " + crashReportCategory$Entry.getValue() +
                        "\n\t\t\t\t" + ConsoleColors.italic + " Please note that this error was not caused by " + ModHelper.MOD_NAME + "! So don't report it to the mod author!" + ConsoleColors.reset,
                Level.FATAL, exception, true
        );
    }

    private void addCrashDetails(String errorDescription, Level level, Throwable throwable, boolean responsibleForCrash){
        CrashDetail crashDetail = new CrashDetail(errorDescription, level, throwable, responsibleForCrash);
        for(CrashDetail crashDetail1 : this.crashDetails){
            if (crashDetail.equals(crashDetail1)){
                return;
            }
        }
        this.crashDetails.add(crashDetail);
    }

    public boolean resolveCrash(Throwable throwable){
        for(StackTraceElement element : throwable.getStackTrace()){
            if(element.getClassName().contains("de.thedead2.customadvancements")){
                this.addCrashDetails("A fatal error occurred executing " + MOD_NAME, Level.FATAL, throwable, true);
                return true;
            }
        }
        if(throwable.getCause() != null){
            this.resolveCrash(throwable.getCause());
        }
        return false;
    }

    public boolean resolveCrash(StackTraceElement[] stacktrace, String input){
        return this.resolveCrash(this.recreateThrowable(stacktrace, input));
    }

    public Throwable recreateThrowable(StackTraceElement[] stacktrace, String exceptionMessage){
        Throwable throwable = this.resolveThrowable(exceptionMessage);
        throwable.setStackTrace(stacktrace);
        return throwable;
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
                    object = exceptionClass.getDeclaredConstructor(String.class).newInstance(exceptionMessage);
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

    public void reset(){
        this.crashDetails.clear();
        this.advancements.clear();
        this.removedAdvancements.clear();
        this.activeAdvancement = null;
        this.activeFile = null;
    }

    private record CrashDetail(String description, Level level, Throwable throwable, boolean responsibleForCrash) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CrashDetail that = (CrashDetail) o;
            return responsibleForCrash == that.responsibleForCrash && com.google.common.base.Objects.equal(description, that.description) && com.google.common.base.Objects.equal(level, that.level) && com.google.common.base.Objects.equal(throwable, that.throwable);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(description, level, throwable, responsibleForCrash);
        }
    }
}
