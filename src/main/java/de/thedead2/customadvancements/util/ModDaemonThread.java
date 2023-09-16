package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.core.ModHelper;

public class ModDaemonThread extends Thread {

    public ModDaemonThread(){
        this(null);
    }
    public ModDaemonThread(Runnable toRun){
        super(toRun, ModHelper.MOD_ID + "/DAEMON");
        setDaemon(true);
    }
}
