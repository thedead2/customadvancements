package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.exceptions.ExceptionHandler;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.io.InputStream;



@MethodsReturnNonnullByDefault
public class ReflectionHelper {

    public static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;

        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];

            if (!ste.getClassName().equals(ExceptionHandler.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
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
            ExceptionHandler.getInstance().log("Couldn't find resource with path: " + path, new NullPointerException("The return result of 'classloader.getResourceAsStream(" + path + ")' is null!"));
            stream = InputStream.nullInputStream();
        }

        return stream;
    }

    public static Class<?> findClassWithName(String className) {
        try {
            return ReflectionHelper.class.getClassLoader().loadClass(className);
        }
        catch(ClassNotFoundException e) {
            ExceptionHandler.getInstance().log("Couldn't load class with name: " + className, e);
            throw new RuntimeException(e);
        }
    }
}
