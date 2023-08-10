package de.thedead2.customadvancements.util;

import com.google.common.reflect.ClassPath;
import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

@MethodsReturnNonnullByDefault
public abstract class ReflectionHelper {

    public static Set<Class<?>> findClassesInPackage(String packageName) {
        return findClassesInPackage(packageName, ClassLoader.getSystemClassLoader());
    }

    public static Set<Class<?>> findClassesInPackage(String packageName, ClassLoader classLoader) {
        try {
            return ClassPath.from(classLoader)
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName().equalsIgnoreCase(packageName))
                    .map(classInfo -> {
                        try {
                            return classLoader.loadClass(classInfo.getName());
                        } catch (ClassNotFoundException e) {
                            CrashHandler.getInstance().handleException("Unable to load class " + classInfo.getName() + " with classloader " + classLoader.getName(), e, Level.ERROR);
                            return classInfo.load();
                        }
                    })
                    .collect(Collectors.toSet());
        }
        catch (IOException e) {
            CrashHandler.getInstance().handleException("Failed to get classes of package " + packageName, e, Level.FATAL);
            return Collections.emptySet();
        }
    }

    public static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(CrashHandler.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
                if (callerClassName==null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName();
                }
            }
        }
        return "";
    }

    public static Class<?> changeClassLoader(Class<?> aClass, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(aClass.getName());
        }
        catch (ClassNotFoundException e) {
            CrashHandler.getInstance().handleException("Unable to change ClassLoader of Class: " + aClass.getName(), e, Level.ERROR);
            return aClass;
        }
    }

    public static InputStream findResource(String path){
        InputStream stream = ReflectionHelper.class.getClassLoader().getResourceAsStream(path);
        if(stream == null){
            CrashHandler.getInstance().handleException("Couldn't find resource with path: " + path, new NullPointerException("The return result of 'classloader.getResourceAsStream(" + path + ")' is null!"), Level.ERROR);
            stream = InputStream.nullInputStream();
        }
        return stream;
    }

    public static void registerClasses(Class<?> baseClass, IEventBus eventBus){
        findClassesInPackage(baseClass.getPackageName())
                .stream()
                .filter(aClass -> checkForSuperClassOrInterface(aClass, baseClass))
                .map(aClass -> changeClassLoader(aClass, baseClass.getClassLoader()))
                .forEach(aClass -> {
                    var list = Arrays.stream(aClass.getMethods()).filter(method-> Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(SubscribeEvent.class)).toList();
                    if(list.isEmpty()) throw new IllegalStateException("Class " + aClass.getName() + " doesn't provide a static @SubscribeEvent method!");
                    LOGGER.info("Registering class {} to event bus", aClass.getName());
                    eventBus.register(aClass);
                });
    }

    private static boolean checkForSuperClassOrInterface(Class<?> aClass, Class<?> baseClass){
        Class<?> superClass = aClass.getSuperclass();
        if(superClass == null || !superClass.getName().equals(baseClass.getName())) {
            LOGGER.debug(aClass.getName());
            if(!Modifier.isAbstract(aClass.getModifiers())) {
                LOGGER.debug("non abstract class");
                if(Arrays.stream(aClass.getInterfaces()).noneMatch(aClass1 -> aClass1.getName().equals(baseClass.getName()))){
                    if(superClass != null) return checkForSuperClassOrInterface(superClass, baseClass);
                }
                return Arrays.stream(aClass.getInterfaces()).anyMatch(aClass1 -> aClass1.getName().equals(baseClass.getName()));
            }
            return false;
        }
        return true;
    }
}
