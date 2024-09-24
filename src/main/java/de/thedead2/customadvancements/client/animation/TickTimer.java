package de.thedead2.customadvancements.client.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.client.Timer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;


public class TickTimer {

    private final Timer timer = new Timer(20, Util.getMillis());

    private float startTime;

    private float duration;

    private float timeLeft;

    private float startCounter;

    private float sleepTime;

    private boolean inverted;

    private boolean paused;

    private ILoopType loop;


    public TickTimer(float startTime, float duration, ILoopType loop) {
        this(startTime, duration, false, false, loop);
    }


    public TickTimer(float startTime, float duration, boolean inverted, boolean paused, ILoopType loop) {
        this.startTime = startTime;
        this.duration = duration;
        this.inverted = inverted;
        this.paused = paused;
        this.loop = loop;

        this.reset();
    }


    /**
     * Resets the timer to start again.
     **/
    public void reset() {
        this.startCounter = 0;
        this.sleepTime = -1;
        this.timeLeft = this.inverted ? 0 : this.duration;

        this.timer.advanceTime(Util.getMillis());
    }


    private TickTimer(float startTime, float duration, float timeLeft, float startCounter, float sleepTime, boolean inverted, boolean paused, ILoopType loop) {
        this.startTime = startTime;
        this.duration = duration;
        this.timeLeft = timeLeft;
        this.startCounter = startCounter;
        this.sleepTime = sleepTime;
        this.inverted = inverted;
        this.paused = paused;
        this.loop = loop;
    }


    public static TickTimer fromJson(JsonElement jsonElement) {
        float startTime, duration, timeLeft, startCounter, sleepTime;
        boolean inverted, paused;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        startTime = jsonObject.get("startTime").getAsFloat();
        duration = jsonObject.get("duration").getAsFloat();
        timeLeft = jsonObject.get("timeLeft").getAsFloat();
        startCounter = jsonObject.get("startCounter").getAsFloat();
        sleepTime = jsonObject.get("sleepTime").getAsFloat();

        inverted = jsonObject.get("inverted").getAsBoolean();
        paused = jsonObject.get("paused").getAsBoolean();

        return new TickTimer(startTime, duration, timeLeft, startCounter, sleepTime, inverted, paused, LoopTypes.NO_LOOP);
    }


    public static TickTimer fromNBT(CompoundTag tag) {
        float startTime, duration, timeLeft, startCounter, sleepTime;
        boolean inverted, paused;

        startTime = tag.getFloat("startTime");
        duration = tag.getFloat("duration");
        timeLeft = tag.getFloat("timeLeft");
        startCounter = tag.getFloat("startCounter");
        sleepTime = tag.getFloat("sleepTime");

        inverted = tag.getBoolean("inverted");
        paused = tag.getBoolean("paused");

        return new TickTimer(startTime, duration, timeLeft, startCounter, sleepTime, inverted, paused, LoopTypes.NO_LOOP);
    }


    public static TickTimer fromNetwork(FriendlyByteBuf buf) {
        float startTime, duration, timeLeft, startCounter, sleepTime;
        boolean inverted, paused;

        startTime = buf.readFloat();
        duration = buf.readFloat();
        timeLeft = buf.readFloat();
        startCounter = buf.readFloat();
        sleepTime = buf.readFloat();

        inverted = buf.readBoolean();
        paused = buf.readBoolean();

        return new TickTimer(startTime, duration, timeLeft, startCounter, sleepTime, inverted, paused, LoopTypes.NO_LOOP);
    }


    public void updateTime() {
        int j = this.timer.advanceTime(Util.getMillis());

        if (this.paused || this.sleepTime > 0) {
            this.sleepTime -= j;

            return;
        }

        if (!this.isStarted()) {
            this.startCounter += j;
        }
        else if (this.isFinished()) {
            this.loop.loop(this);
        }
        else {
            if (this.inverted) {
                this.timeLeft += j;
            }
            else {
                this.timeLeft -= j;
            }
        }
    }


    public boolean isStarted() {
        return this.startCounter > this.startTime;
    }


    public boolean isFinished() {
        return this.inverted ? timeLeft >= this.duration : timeLeft <= 0;
    }


    public TickTimer startIfNeeded() {
        if (!this.isStarted()) {
            this.start();
        }

        return this;
    }


    public TickTimer start() {
        this.pause(false);
        this.reset();

        this.startCounter = this.startTime + 1;

        return this;
    }


    public TickTimer pause(boolean pause) {
        if (this.paused != pause) {
            this.timer.advanceTime(Util.getMillis());
        }

        this.paused = pause;

        return this;
    }


    public TickTimer invert(boolean invert) {
        this.inverted = invert;

        return this;
    }


    public TickTimer loop(ILoopType loop) {
        this.loop = loop;

        return this;
    }


    public boolean isPaused() {
        return this.paused;
    }


    public boolean isLooping() {
        return this.loop.loop(this, false);
    }


    public boolean isInverted() {
        return inverted;
    }


    public float getDuration() {
        return duration;
    }


    public TickTimer setDuration(float duration) {
        this.duration = duration;

        return this;
    }


    public float getStartTime() {
        return startTime;
    }


    public TickTimer setStartTime(float startTime) {
        this.startTime = startTime;

        return this;
    }


    public TickTimer stop() {
        this.timeLeft = this.inverted ? this.duration + 1 : -1;

        this.pause(true);

        return this;
    }


    public TickTimer sleep(float time) {
        if (this.sleepTime < 0) {
            this.sleepTime = time;
        }

        return this;
    }


    public float getTimeLeft() {
        return timeLeft;
    }


    public float getTimePassed() {
        return this.duration - this.timeLeft;
    }


    public ILoopType getLoopType() {
        return this.loop;
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("startTime", this.startTime);
        jsonObject.addProperty("duration", this.duration);
        jsonObject.addProperty("timeLeft", this.timeLeft);
        jsonObject.addProperty("startCounter", this.startCounter);
        jsonObject.addProperty("sleepTime", this.sleepTime);
        jsonObject.addProperty("inverted", this.inverted);
        jsonObject.addProperty("paused", this.paused);

        return null;
    }


    public Tag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("startTime", this.startTime);
        tag.putFloat("duration", this.duration);
        tag.putFloat("timeLeft", this.timeLeft);
        tag.putFloat("startCounter", this.startCounter);
        tag.putFloat("sleepTime", this.sleepTime);
        tag.putBoolean("inverted", this.inverted);
        tag.putBoolean("paused", this.paused);

        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.startTime);
        buf.writeFloat(this.duration);
        buf.writeFloat(this.timeLeft);
        buf.writeFloat(this.startCounter);
        buf.writeFloat(this.sleepTime);
        buf.writeBoolean(this.inverted);
        buf.writeBoolean(this.paused);
    }
}
