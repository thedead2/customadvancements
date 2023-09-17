package de.thedead2.customadvancements.util.core;

import com.google.common.io.ByteStreams;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.util.ReflectionHelper;
import de.thedead2.customadvancements.util.handler.JsonHandler;
import de.thedead2.customadvancements.util.logger.ConsoleColors;
import joptsimple.internal.Strings;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.fml.ISystemReportExtender;
import net.minecraftforge.logging.CrashReportExtender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;

import static de.thedead2.customadvancements.CustomAdvancements.MAIN_PACKAGE;
import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class CrashHandler implements ISystemReportExtender {

    private static CrashHandler instance;
    private IAdvancement activeAdvancement;
    private Advancement activeGameAdvancement;
    private File activeFile;
    private final Set<IAdvancement> advancements = new HashSet<>();
    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private final Set<CrashReportException> crashReportExceptions = new HashSet<>();
    private final List<CrashReportSection> sections = new ArrayList<>();
    private final Set<Runnable> listeners = new HashSet<>();

    private CrashHandler(){
        instance = this;
        LogManager.getLogger().debug("Registered CrashHandler!");
    }

    public static CrashHandler getInstance(){
        return Objects.requireNonNullElseGet(instance, CrashHandler::new);
    }

    @Override
    public String getLabel() {
        gatherDetails();
        onCrash();
        return "\n\n" + "-- " + MOD_NAME + " --" + "\n" + "Details";
    }

    @Override
    public String get() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            this.sections.forEach(stringBuilder::append);
            stringBuilder.append("\n\n");
            return stringBuilder.toString();

        }
        catch (Throwable throwable){
            return "\n\tERROR: " + throwable + "\n";
        }
    }

    private void onCrash(){
        this.listeners.forEach(Runnable::run);
    }

    public void registerCrashListener(Runnable runnable){
        this.listeners.add(runnable);
    }

    private void gatherDetails(){
        this.sections.clear();
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
    }

    private void addSection(CrashReportSection section){
        sections.add(section);
    }

    private void getModInformation(){
        CrashReportSection section = new CrashReportSection();
        section.addDetail("Mod ID", MOD_ID);
        section.addDetail("Version", MOD_VERSION);
        section.addDetail("Main Path", DIR_PATH);
        if(this.activeAdvancement == null && this.activeGameAdvancement == null) {
            section.addDetail("Currently active advancement", "NONE");
        }
        if(this.activeFile == null) {
            section.addDetail("Currently active file", "NONE");
        }
    }

    private void getExecutionErrors(){
        CrashReportSection section = new CrashReportSection("All detected execution errors related to " + MOD_NAME);
        if(!this.crashReportExceptions.isEmpty()) {
            Set<CrashReportException> temp = new HashSet<>();
            this.crashReportExceptions.forEach((crashReportException) -> {
                if(crashReportException.getLevel().equals(Level.FATAL)){
                    section.addDetail(crashReportException);
                    temp.add(crashReportException);
                }
            });
            this.crashReportExceptions.removeAll(temp);

            if(this.crashReportExceptions.size() <= 5){
                this.crashReportExceptions.forEach(section::addDetail);
            }
            else if(this.crashReportExceptions.size() <= 10){
                this.crashReportExceptions.forEach((crashReportException) -> {
                    if(crashReportException.level.isInRange(Level.ERROR, Level.FATAL)){
                        section.addDetail(crashReportException);
                    }
                });
            }
            else {
                this.crashReportExceptions.forEach((crashReportException) -> {
                    if(crashReportException.level.equals(Level.FATAL)){
                        section.addDetail(crashReportException);
                    }
                });
            }
        }
        else {
            section.addDetail("There were no execution errors detected!");
        }
    }

    private void getActiveAdvancement() {
        if(this.activeAdvancement != null || this.activeGameAdvancement != null){
            CrashReportSection section = new CrashReportSection("Currently active advancement");

            if(this.activeAdvancement != null){
                section.addDetail(this.activeAdvancement.getResourceLocation().toString(),"\n\n" + JsonHandler.formatJsonObject(this.activeAdvancement.getJsonObject()));
            }
            else {
                section.addDetail(this.activeGameAdvancement.getId().toString(),"\n\n" + JsonHandler.formatJsonObject(this.activeGameAdvancement.deconstruct().serializeToJson()));
            }
        }
    }

    private void getActiveFile() {
        if(this.activeFile != null){
            CrashReportSection section = new CrashReportSection("Currently active file");
            section.addDetail("Name", this.activeFile.getName());
            section.addDetail("Is File", this.activeFile.isFile());
            section.addDetail("Is Readable", this.activeFile.canRead());
            try {
                InputStream fileInput = Files.newInputStream(this.activeFile.toPath());
                String file_data = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
                file_data = file_data.replaceAll("\n", "\n\t\t");
                section.addDetail("Data", "\n\t" + file_data);
                fileInput.close();
            }
            catch (Exception e) {
                section.addDetail("Data", "\n\t" + "ERROR while reading file data: " + e.getMessage());
            }
        }
    }

    private void getLoadedAdvancements(){
        List<String> temp = new ArrayList<>();
        if(!this.advancements.isEmpty()){
            CrashReportSection section = new CrashReportSection("All loaded Advancements");
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
            section.addDetail(String.join(",\n", temp));
        }
    }

    private void getRemovedAdvancements(){
        List<String> temp = new ArrayList<>();
        if(!this.removedAdvancements.isEmpty()){
            CrashReportSection section = new CrashReportSection("All removed Advancements");
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
            section.addDetail(String.join(",\n", temp));
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
                        "\n\t\t\t\t" + ConsoleColors.italic + " Please note that this error was not caused by " + MOD_NAME + "! So don't report it to the mod author!" + ConsoleColors.reset,
                Level.FATAL, exception, true
        );
    }

    private void addCrashDetails(String errorDescription, Level level, Throwable throwable, boolean responsibleForCrash){
        CrashReportException crashReportException = new CrashReportException(errorDescription, level, throwable, responsibleForCrash);
        for(CrashReportException crashReportException1 : this.crashReportExceptions){
            if (crashReportException.equals(crashReportException1)){
                return;
            }
        }
        this.crashReportExceptions.add(crashReportException);
    }

    public boolean resolveCrash(Throwable throwable){
        for(StackTraceElement element : throwable.getStackTrace()){
            if(element.getClassName().contains(MAIN_PACKAGE)){
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
            int i = input.indexOf(":");
            String temp = i != -1 ? input.substring(i) : "";
            String className = input.replace(Matcher.quoteReplacement(temp), "");
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
        this.crashReportExceptions.clear();
        this.advancements.clear();
        this.removedAdvancements.clear();
        this.activeAdvancement = null;
        this.activeFile = null;
    }

    public void printCrashReport(CrashReport crashReport){
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
    }



    public void handleException(String description, String callingClass, Throwable e, Level level) {
        try {
            String callingClassName = ReflectionHelper.getCallerCallerClassName();
            String exceptionClass = callingClass != null ? callingClass : callingClassName.substring(callingClassName.lastIndexOf(".") + 1);
            Marker marker = new MarkerManager.Log4jMarker(exceptionClass);
            if (level.equals(Level.DEBUG))LOGGER.debug(marker, description);
            else if(level.equals(Level.WARN)) LOGGER.warn(marker, description);
            else if(level.equals(Level.ERROR)) LOGGER.error(marker, description, e);
            else if(level.equals(Level.FATAL)) LOGGER.fatal(marker, description, e);
            else LOGGER.info(marker, description);

            this.addCrashDetails(description, level, e);
            if(activeFile != null){
                printFileDataToConsole(activeFile);
            }
        }
        catch (Exception e1){
            LogManager.getLogger().fatal("Error while handling exception: {} \n-> original exception: {}", e1, description + ":\n" + e);
        }
    }

    public void handleException(String description, Throwable e, Level level) {
        handleException(description, null, e, level);
    }

    private void printFileDataToConsole(File file){
        try {
            InputStream fileInput = Files.newInputStream(file.toPath());
            String file_data = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
            LOGGER.error("\n" + file_data);
            fileInput.close();
        }
        catch (IOException e) {
            LOGGER.warn("Unable to read File by InputStream!");
            this.addCrashDetails("Unable to read File by InputStream!", Level.WARN, e);
            e.printStackTrace();
        }
    }

    private static class CrashReportException extends CrashReportSection{
        private final String description;
        private final Level level;
        private final Throwable throwable;
        private final boolean responsibleForCrash;

        CrashReportException(String description, Level level, Throwable throwable, boolean responsibleForCrash) {
            super(throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf(".") + 1));
            this.description = description;
            this.level = level;
            this.throwable = throwable;
            this.responsibleForCrash = responsibleForCrash;
            this.subSection = true;
            this.getErrorDetails();
            this.getStackTrace();
        }


            private void getErrorDetails() {
            this.addDetail(new CrashReportDetail("Reported Error", getExceptionName(throwable)));
            this.addDetail(new CrashReportDetail("Description", description));
            if (throwable.getCause() != null) {
                this.addDetail(new CrashReportDetail("Caused by", getExceptionName(throwable.getCause())));
            }
            this.addDetail(new CrashReportDetail("Level", level));
            this.addDetail(new CrashReportDetail("Caused Crash", responsibleForCrash ? "Definitely! \n\t\t"
                    + ConsoleColors.italic + "Please report this crash to the mod author: " + MOD_ISSUES_LINK + ConsoleColors.reset :
                    "Probably Not!"
            ));
        }

        private String getExceptionName(Throwable throwable) {
            return throwable.getClass().getName() + (throwable.getMessage() != null ? (": " + throwable.getMessage()) : "");
        }

        private void getStackTrace() {
            StringBuilder stringBuilder2 = new StringBuilder();

            if (level.equals(Level.FATAL)) {
                stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(throwable, false));
            } else if (level.equals(Level.ERROR)) {
                stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 6), false));
            } else if (level.equals(Level.WARN)) {
                stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 3), false));
            } else if (level.equals(Level.DEBUG)) {
                stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(this.trimStacktrace(throwable, 1), false));
            }
            String temp1 = stringBuilder2.toString();
            if(temp1.contains("Caused by:"))
                stringBuilder2.insert(temp1.indexOf("Caused by:"), "\t\t");
            String temp2 = stringBuilder2.toString().replaceAll("\tat", "\t\t\tat");
            this.addDetail(new CrashReportDetail("Stacktrace", temp2));
        }

        private Throwable trimStacktrace(Throwable throwable, int length) {
            if(length > throwable.getStackTrace().length) return throwable;
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            StackTraceElement[] astacktraceelement = new StackTraceElement[length];
            System.arraycopy(stackTraceElements, 0, astacktraceelement, 0, astacktraceelement.length);
            throwable.setStackTrace(astacktraceelement);
            return throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public String getDescription() {
            return description;
        }

        public Level getLevel() {
            return level;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CrashReportException that = (CrashReportException) o;
            return responsibleForCrash == that.responsibleForCrash && com.google.common.base.Objects.equal(description, that.description) && com.google.common.base.Objects.equal(level, that.level) && com.google.common.base.Objects.equal(throwable, that.throwable);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(description, level, throwable, responsibleForCrash);
        }

        @Override
        public String toString() {
            return super.toString() + "\n\n" +
                    Strings.repeat('-', 200);
        }
    }

    private static class CrashReportSection{
        protected final List<CrashReportSection> details = new ArrayList<>();
        protected final String title;
        protected boolean subSection;

        CrashReportSection(){
            this(null);
        }

        CrashReportSection(String title){
            this.title = title;
            this.subSection = false;
            if(!(this instanceof CrashReportDetail || this instanceof CrashReportException)) //better via interface!
                CrashHandler.getInstance().addSection(this);
        }

        protected void addDetail(CrashReportSection detail){
            details.add(detail);
        }

        protected void addDetail(String name, Object in){
            this.addDetail(new CrashReportDetail(name, in));
        }

        public void addDetail(String name) {
            this.addDetail(new CrashReportDetail(name));
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder1 = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            if(title != null) {
                stringBuilder1.append("\n\n");
                if(subSection)
                    stringBuilder1.append("\t");

                stringBuilder1.append(title).append(":");
            }

            details.forEach(section -> {
                StringBuilder temp3 = new StringBuilder(section.toString());
                if(subSection)
                    temp3.insert(temp3.indexOf("\t"), "\t");

                stringBuilder2.append(temp3);
            });

            return stringBuilder1.append(stringBuilder2).toString();
        }

        public String getTitle() {
            return title;
        }
    }

    private static class CrashReportDetail extends CrashReportSection{
        private final String name;
        private final Object in;

        private CrashReportDetail(String name, Object in) {
            this.name = name;
            this.in = in;
        }

        private CrashReportDetail(String name) {
            this(name, null);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("\n");
            stringBuilder1.append("\t").append(name);
            if(in != null){
                stringBuilder1.append(": ");
                if (in instanceof List<?> list) {
                    list.forEach((o) -> {
                        stringBuilder1.append("\n\t\t\t");
                        stringBuilder1.append(o);
                    });
                }
                else stringBuilder1.append(in);
            }
            return stringBuilder1.toString();
        }
    }
}