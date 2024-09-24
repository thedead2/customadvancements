package de.thedead2.customadvancements.client.animation;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.floats.FloatConsumer;

import java.util.function.Predicate;


public interface IAnimation {

    static IAnimation fromJson(JsonElement jsonElement) {
        return null;
    }


    default IAnimation startIf(Predicate<IAnimation> predicate) {
        if (predicate.test(this)) {
            this.start();
        }

        return this;
    }


    IAnimation start();


    IAnimation stop();


    /**
     * Sets the {@link ILoopType} for this animation.
     *
     * @see ILoopType
     **/
    IAnimation loop(ILoopType loop);


    /**
     * @return true if this animation has finished and is not currently looping
     **/
    default boolean isFinishedAndNotLooping() {
        return this.isFinished() && !this.isLooping();
    }


    /**
     * @return true if this animation has finished
     * <p>Note: this also returns true for a brief moment while this {@link IAnimation} is looping</p>
     *
     * @see #isLooping()
     * @see #isFinishedAndNotLooping()
     **/
    boolean isFinished();


    /**
     * @return true if this animation is currently looping
     *
     * @see ILoopType#loop(TickTimer, boolean)
     * @see #isFinished()
     **/
    boolean isLooping();


    /**
     * @return true if this {@link IAnimation} is currently paused
     **/
    boolean isPaused();


    boolean isInverted();


    /**
     * Resets this {@link IAnimation} to start again
     *
     * @return this
     **/
    IAnimation reset();


    /**
     * Adds another animation that should be played after this animation has finished.
     *
     * @param other the other animation that should be played after this one
     *
     * @return the other animation
     *
     * @see #animate(float, float, FloatConsumer)
     **/
    default IAnimation andThenAnimate(IAnimation other, float from, float to, FloatConsumer consumer) {
        other.pause(!this.isFinished() || this.isLooping());

        return other.animate(from, to, consumer);
    }


    /**
     * Pauses or resumes this animation
     *
     * @param paused true if this animation should be paused, false if it should be resumed or not paused
     **/
    IAnimation pause(boolean paused);


    /**
     * Animates a value in the given interval.
     *
     * @param from     the start point of the value to animate
     * @param to       the end point of the value to animate
     * @param consumer the action to which the animated value should be applied to
     *
     * @return this
     **/
    IAnimation animate(float from, float to, FloatConsumer consumer);


    default IAnimation animateIf(Predicate<IAnimation> predicate, float from, float to, FloatConsumer consumer) {
        if (predicate.test(this)) {
            this.startIfNeeded()
                .pause(false);
        }
        else {
            this.pause(this.isStarted());
        }

        return this.animate(from, to, consumer);
    }


    /**
     * Starts this animation if it hasn't been started yet.
     *
     * @return this
     *
     * @see #start()
     **/
    default IAnimation startIfNeeded() {
        if (!this.isStarted()) {
            this.start();
        }

        return this;
    }


    /**
     * @return true if this {@link IAnimation} has started
     **/
    boolean isStarted();


    default IAnimation animateInvertedIf(Predicate<IAnimation> predicate, float from, float to, FloatConsumer consumer) {
        if (predicate.test(this)) {
            this.invert(true)
                .startIfNeeded()
                .pause(false);
        }
        else {
            this.pause(this.isStarted());
        }

        return this.animate(from, to, consumer);
    }


    /**
     * Inverts the play direction of this animation.
     *
     * @param inverted true if the animation should run backwards, false otherwise
     **/
    IAnimation invert(boolean inverted);


    JsonElement toJson();


    default IAnimation startIfNeededIf(Predicate<IAnimation> predicate) {
        if (predicate.test(this)) {
            this.startIfNeeded();
        }

        return this;
    }


    /**
     * Pauses this animation for the given time if the given predicate returns true.
     *
     * @param predicate the predicate to test for
     * @param ticks     the amount in ticks this animation should sleep
     *
     * @return this
     *
     * @see #sleep(float)
     **/
    default IAnimation sleepIf(Predicate<IAnimation> predicate, float ticks) {
        if (predicate.test(this)) {
            this.sleep(ticks);
        }

        return this;
    }


    IAnimation sleep(float ticks);


    default boolean isFinishedButLooping() {
        return this.isFinished() && this.isLooping();
    }
}
