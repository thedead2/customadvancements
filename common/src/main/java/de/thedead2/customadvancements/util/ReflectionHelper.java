package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.apache.logging.log4j.Level;

import java.io.InputStream;



@MethodsReturnNonnullByDefault
public class ReflectionHelper {

    public static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;

        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];

            if (!ste.getClassName().equals(CrashHandler.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                if (callerClassName == null) {
                    callerClassName = ste.getClassName();
                }
                else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName();
                }
            }
        }

        return "";
    }


    public static InputStream findResource(String path) {
        InputStream stream = ReflectionHelper.class.getClassLoader().getResourceAsStream(path);

        if (stream == null) {
            CrashHandler.getInstance().handleException("Couldn't find resource with path: " + path, new NullPointerException("The return result of 'classloader.getResourceAsStream(" + path + ")' is null!"), Level.ERROR);
            stream = InputStream.nullInputStream();
        }

        return stream;
    }
}
