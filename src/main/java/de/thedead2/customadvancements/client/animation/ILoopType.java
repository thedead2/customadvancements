package de.thedead2.customadvancements.client.animation;


@FunctionalInterface
public interface ILoopType {

    default boolean loop(TickTimer timer) {
        return this.loop(timer, true);
    }


    /**
     * Specifies an action to execute when the given {@link TickTimer} finishes.
     * Implementations must ensure to only execute the specified action if:
     * <b>{@code shouldRun = true}
     *
     * @param timer     the {@link TickTimer} which should be looped or not
     * @param shouldRun whether the specified action, e.g. reset of the timer, should be executed or not,
     *                  use false if you only want to access the return value
     *
     * @return true if the {@link TickTimer} is currently looping, false otherwise
     **/
    boolean loop(TickTimer timer, boolean shouldRun);
}
