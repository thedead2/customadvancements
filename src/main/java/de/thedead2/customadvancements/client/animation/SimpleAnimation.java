package de.thedead2.customadvancements.client.animation;

import com.google.gson.JsonElement;


public class SimpleAnimation extends AbstractAnimation {

    public SimpleAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType) {
        super(startTime, duration, loop, animationType, interpolationType);
    }


    public SimpleAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, boolean started) {
        super(startTime, duration, loop, animationType, interpolationType, started);
    }


    @Override
    public JsonElement toJson() {
        return null;
    }


    @Override
    protected float animateInternal(float from, float to) {
        return this.animationType.transform(from, to, this.timer.getDuration(), this.timer.getTimeLeft(), this.interpolationType);
    }
}
