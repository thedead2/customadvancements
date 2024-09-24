package de.thedead2.customadvancements.client.animation;

import it.unimi.dsi.fastutil.floats.FloatConsumer;


public abstract class AbstractAnimation implements IAnimation {

    protected final IAnimationType animationType;

    protected final IInterpolationType interpolationType;

    protected final TickTimer timer;


    public AbstractAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType) {
        this(startTime, duration, loop, animationType, interpolationType, true);
    }


    public AbstractAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, boolean started) {
        this.animationType = animationType;
        this.interpolationType = interpolationType;
        this.timer = new TickTimer(startTime, duration, false, !started, loop);
    }


    @Override
    public IAnimation start() {
        this.timer.start();

        return this;
    }


    @Override
    public IAnimation stop() {
        this.timer.stop();

        return this;
    }


    @Override
    public IAnimation loop(ILoopType loop) {
        this.timer.loop(loop);

        return this;
    }


    @Override
    public boolean isFinished() {
        return this.timer.isFinished();
    }


    @Override
    public boolean isLooping() {
        return this.timer.isLooping();
    }


    @Override
    public boolean isPaused() {
        return this.timer.isPaused();
    }


    @Override
    public boolean isInverted() {
        return this.timer.isInverted();
    }


    @Override
    public IAnimation reset() {
        this.timer.reset();

        return this;
    }


    @Override
    public IAnimation pause(boolean paused) {
        this.timer.pause(paused);

        return this;
    }


    @Override
    public IAnimation animate(float from, float to, FloatConsumer consumer) {
        this.timer.updateTime();
        float f;

        if (!this.isStarted()) {
            f = this.timer.isInverted() ? to : from;
        }
        else if (this.isFinished()) {
            f = this.timer.isInverted() ? from : to;
        }
        else {
            f = this.animateInternal(from, to);
        }

        consumer.accept(f);

        return this;
    }


    protected abstract float animateInternal(float from, float to);


    @Override
    public boolean isStarted() {
        return this.timer.isStarted();
    }


    @Override
    public IAnimation invert(boolean inverted) {
        this.timer.invert(inverted);

        return this;
    }


    @Override
    public IAnimation sleep(float ticks) {
        this.timer.sleep(ticks);

        return this;
    }
}
