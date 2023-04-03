package de.thedead2.customadvancements.util;

import org.apache.commons.lang3.time.StopWatch;

public class Timer extends StopWatch {

    public Timer(){
        this(false);
    }

    public Timer(boolean start){
        if(start){
            this.start();
        }
    }

    @Override
    public void start() {
        if(this.isStarted()){
            this.stop(true);
            super.start();
        }
        else {
            if(this.isStopped())
                this.reset();
            super.start();
        }
    }

    public void stop(boolean reset) {
        this.stop();
        if(reset)
            this.reset();
    }
}
