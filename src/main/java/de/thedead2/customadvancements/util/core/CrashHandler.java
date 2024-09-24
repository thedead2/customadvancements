package de.thedead2.customadvancements.util.core;

import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.util.ReflectionHelper;
import joptsimple.internal.Strings;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.fml.ISystemReportExtender;
import net.minecraftforge.logging.CrashReportExtender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class CrashHandler implements ISystemReportExtender {

    private static CrashHandler INSTANCE;

    private final Set<CustomAdvancement> advancements = new HashSet<>();

    private final Set<ResourceLocation> removedAdvancements = new HashSet<>();

    private final Set<CrashReportException> crashReportExceptions = new HashSet<>();

    private final List<CrashReportSection> sections = new ArrayList<>();


    public static CrashHandler getInstance() {
        return Objects.requireNonNullElseGet(INSTANCE, CrashHandler::new);
    }


    private CrashHandler() {
        INSTANCE = this;

        LogManager.getLogger().debug("Registered CrashHandler!");
    }


    public static String createStacktrace(Throwable throwable, int length) {
        return CrashReportExtender.generateEnhancedStackTrace(trimStacktrace(throwable, length), false);
    }


    public static Throwable trimStacktrace(Throwable throwable, int length) {
        if (length > throwable.getStackTrace().length) {
            return throwable;
        }

        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StackTraceElement[] astacktraceelement = new StackTraceElement[length];
        System.arraycopy(stackTraceElements, 0, astacktraceelement, 0, astacktraceelement.length);
        throwable.setStackTrace(astacktraceelement);

        return throwable;
    }


    @Override
    public String getLabel() {
        gatherDetails();

        return "\n\n" + "-- " + MOD_NAME + " --" + "\n" + "Details";
    }


    private void gatherDetails() {
        this.sections.clear();
        this.getModInformation();

        this.getExecutionErrors();
        this.getLoadedAdvancements();
        this.getRemovedAdvancements();
    }


    private void getModInformation() {
        CrashReportSection section = new CrashReportSection();

        section.addDetail("Mod ID", MOD_ID);
        section.addDetail("Version", MOD_VERSION);
        section.addDetail("Main Path", DIR_PATH);
    }


    private void getExecutionErrors() {
        CrashReportSection section = new CrashReportSection("All detected execution errors related to " + MOD_NAME);

        if(this.crashReportExceptions.isEmpty()) {
            section.addDetail("There were no execution errors detected!");

            return;
        }

        Set<CrashReportException> temp = new HashSet<>();

        this.crashReportExceptions.forEach((crashReportException) -> {
            if (crashReportException.getLevel().equals(Level.FATAL)) {
                section.addDetail(crashReportException);
                temp.add(crashReportException);
            }
        });

        this.crashReportExceptions.removeAll(temp);

        if (this.crashReportExceptions.size() <= 5) {
            this.crashReportExceptions.forEach(section::addDetail);
        }
        else if (this.crashReportExceptions.size() <= 10) {
            this.crashReportExceptions.forEach((crashReportException) -> {
                if (crashReportException.level.isInRange(Level.ERROR, Level.FATAL)) {
                    section.addDetail(crashReportException);
                }
            });
        }
        else {
            this.crashReportExceptions.forEach((crashReportException) -> {
                if (crashReportException.level.equals(Level.FATAL)) {
                    section.addDetail(crashReportException);
                }
            });
        }
    }


    private void getLoadedAdvancements() {
        List<String> temp = new ArrayList<>();

        if (!this.advancements.isEmpty()) {
            CrashReportSection section = new CrashReportSection("All loaded Advancements");
            int i = 0;

            for (CustomAdvancement advancement : this.advancements) {
                StringBuilder builder = new StringBuilder();

                if (i != 0) {
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


    private void getRemovedAdvancements() {
        List<String> temp = new ArrayList<>();

        if (!this.removedAdvancements.isEmpty()) {
            CrashReportSection section = new CrashReportSection("All removed Advancements");
            int i = 0;

            for (ResourceLocation advancement : this.removedAdvancements) {
                StringBuilder builder = new StringBuilder();

                if (i != 0) {
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


    @Override
    public String get() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            this.sections.forEach(stringBuilder::append);
            stringBuilder.append("\n\n");

            return stringBuilder.toString();

        }
        catch (Throwable throwable) {
            return "\n\tERROR: " + throwable + "\n";
        }
    }


    private void addSection(CrashReportSection section) {
        sections.add(section);
    }


    public void addRemovedAdvancement(ResourceLocation advancementId) {
        this.removedAdvancements.add(advancementId);
    }


    public void addScreenCrash(CrashReportCategory.Entry crashReportCategory$Entry, Throwable exception) {
        this.addCrashDetails("Error while rendering screen: " + crashReportCategory$Entry.getValue() +
                                     "\n\t\t\t\t" + " Please note that this error was not caused by " + MOD_NAME + "! So don't report it to the mod author!",
                             Level.FATAL, exception, true
        );
    }


    private void addCrashDetails(String errorDescription, Level level, Throwable throwable, boolean responsibleForCrash) {
        CrashReportException crashReportException = new CrashReportException(errorDescription, level, throwable, responsibleForCrash);

        for (CrashReportException crashReportException1 : this.crashReportExceptions) {
            if (crashReportException.equals(crashReportException1)) {
                return;
            }
        }

        this.crashReportExceptions.add(crashReportException);
    }


    public boolean resolveCrash(Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (element.getClassName().contains(MAIN_PACKAGE)) {
                this.addCrashDetails("A fatal error occurred executing " + MOD_NAME, Level.FATAL, throwable, true);

                return true;
            }
        }

        if (throwable.getCause() != null) {
            this.resolveCrash(throwable.getCause());
        }

        return false;
    }


    public boolean resolveCrash(StackTraceElement[] stacktrace, String input) {
        return this.resolveCrash(this.recreateThrowable(stacktrace, input));
    }


    public Throwable recreateThrowable(StackTraceElement[] stacktrace, String exceptionMessage) {
        Throwable throwable = this.resolveThrowable(exceptionMessage);
        throwable.setStackTrace(stacktrace);

        return throwable;
    }


    private Throwable resolveThrowable(String input) {
        Class<?> exceptionClass;
        Throwable throwable;

        if(input == null) {
            return new Throwable("Unknown Exception");
        }

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
            catch (NoSuchMethodException e) {
                object = exceptionClass.getDeclaredConstructor().newInstance();
            }

            if (object instanceof Throwable t) {
                throwable = t;
            }
            else {
                throw new IllegalStateException();
            }
        }
        catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalStateException ignored) {
            throwable = new Throwable(input);
        }

        return throwable;
    }


    public void reset() {
        this.crashReportExceptions.clear();
        this.advancements.clear();
        this.removedAdvancements.clear();
    }


    public void printCrashReport(CrashReport crashReport) {
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
    }


    public void handleException(String description, Throwable e, Level level) {
        handleException(description, null, e, level);
    }


    public void handleException(String description, String callingClass, Throwable e, Level level) {
        try {
            String callingClassName = ReflectionHelper.getCallerCallerClassName();
            String exceptionClass = callingClass != null ? callingClass : callingClassName.substring(callingClassName.lastIndexOf(".") + 1);
            Marker marker = new MarkerManager.Log4jMarker(exceptionClass);

            if (level.equals(Level.DEBUG)) {
                LOGGER.debug(marker, description);
            }
            else if (level.equals(Level.WARN)) {
                LOGGER.warn(marker, description);
            }
            else if (level.equals(Level.ERROR)) {
                LOGGER.error(marker, description, e);
            }
            else if (level.equals(Level.FATAL)) {
                LOGGER.fatal(marker, description, e);
            }
            else {
                LOGGER.info(marker, description);
            }

            this.addCrashDetails(description, level, e);
        }
        catch (Exception e1) {
            LogManager.getLogger().fatal("Error while handling exception: {} \n-> original exception: {}", e1, description + ":\n" + e);
        }
    }


    public void addCrashDetails(String errorDescription, Level level, Throwable throwable) {
        this.addCrashDetails(errorDescription, level, throwable, false);
    }


    public void addRemovedAdvancements(Collection<ResourceLocation> advancementIds) {
        this.removedAdvancements.addAll(advancementIds);
    }


    private static class CrashReportException extends CrashReportSection {

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
                    + "Please report this crash to the mod author: " + MOD_ISSUES_LINK :
                    "Probably Not!"
            ));
        }


        private void getStackTrace() {
            StringBuilder stringBuilder2 = new StringBuilder();

            if (level.equals(Level.FATAL)) {
                stringBuilder2.append(CrashReportExtender.generateEnhancedStackTrace(throwable, false));
            }
            else if (level.equals(Level.ERROR)) {
                stringBuilder2.append(createStacktrace(throwable, 6));
            }
            else if (level.equals(Level.WARN)) {
                stringBuilder2.append(createStacktrace(throwable, 3));
            }
            else if (level.equals(Level.DEBUG)) {
                stringBuilder2.append(createStacktrace(throwable, 1));
            }

            String temp1 = stringBuilder2.toString();

            if (temp1.contains("Caused by:")) {
                stringBuilder2.insert(temp1.indexOf("Caused by:"), "\t\t");
            }

            String temp2 = stringBuilder2.toString().replaceAll("\tat", "\t\t\tat");
            this.addDetail(new CrashReportDetail("Stacktrace", temp2));
        }


        private String getExceptionName(Throwable throwable) {
            return throwable.getClass().getName() + (throwable.getMessage() != null ? (": " + throwable.getMessage()) : "");
        }


        public Level getLevel() {
            return level;
        }


        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(description, level, throwable, responsibleForCrash);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CrashReportException that = (CrashReportException) o;

            return responsibleForCrash == that.responsibleForCrash && com.google.common.base.Objects.equal(description, that.description) && com.google.common.base.Objects.equal(level, that.level) && com.google.common.base.Objects.equal(throwable, that.throwable);
        }


        @Override
        public String toString() {
            return super.toString() + "\n\n" +
                    Strings.repeat('-', 200);
        }
    }


    private static class CrashReportSection {

        protected final List<CrashReportSection> details = new ArrayList<>();

        protected final String title;

        protected boolean subSection;


        CrashReportSection() {
            this(null);
        }


        CrashReportSection(String title) {
            this.title = title;
            this.subSection = false;

            if (!(this instanceof CrashReportDetail || this instanceof CrashReportException)) //better via interface!
            {
                CrashHandler.getInstance().addSection(this);
            }
        }


        protected void addDetail(String name, Object in) {
            this.addDetail(new CrashReportDetail(name, in));
        }


        protected void addDetail(CrashReportSection detail) {
            details.add(detail);
        }


        public void addDetail(String name) {
            this.addDetail(new CrashReportDetail(name));
        }


        @Override
        public String toString() {
            StringBuilder stringBuilder1 = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();

            if (title != null) {
                stringBuilder1.append("\n\n");

                if (subSection) {
                    stringBuilder1.append("\t");
                }

                stringBuilder1.append(title).append(":");
            }

            details.forEach(section -> {
                StringBuilder temp3 = new StringBuilder(section.toString());

                if (subSection) {
                    temp3.insert(temp3.indexOf("\t"), "\t");
                }

                stringBuilder2.append(temp3);
            });

            return stringBuilder1.append(stringBuilder2).toString();
        }
    }


    private static class CrashReportDetail extends CrashReportSection {

        private final String name;

        private final Object in;


        private CrashReportDetail(String name) {
            this(name, null);
        }


        private CrashReportDetail(String name, Object in) {
            this.name = name;
            this.in = in;
        }


        @Override
        public String toString() {
            StringBuilder stringBuilder1 = new StringBuilder();

            stringBuilder1.append("\n");
            stringBuilder1.append("\t").append(name);

            if (in != null) {
                stringBuilder1.append(": ");

                if (in instanceof List<?> list) {
                    list.forEach((o) -> {
                        stringBuilder1.append("\n\t\t\t");
                        stringBuilder1.append(o);
                    });
                }
                else {
                    stringBuilder1.append(in);
                }
            }

            return stringBuilder1.toString();
        }
    }
}