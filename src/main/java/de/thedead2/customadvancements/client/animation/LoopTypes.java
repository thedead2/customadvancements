package de.thedead2.customadvancements.client.animation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;


public class LoopTypes {

    public static final ILoopType NO_LOOP = (timer, shouldRun) -> false;

    public static final ILoopType LOOP = (timer, shouldRun) -> {
        if (shouldRun) {
            timer.reset();
        }

        return true;
    };

    public static final ILoopType LOOP_INVERSE = (timer, shouldRun) -> {
        if (shouldRun) {
            timer.invert(!timer.isInverted());
            timer.reset();
        }

        return true;
    };


    public static ILoopType LOOP_TIMES(int amount) {
        AtomicInteger i = new AtomicInteger(0);

        return (timer, shouldRun) -> {
            if (i.get() < amount && shouldRun) {
                i.getAndIncrement();
                LOOP.loop(timer, true);

                return true;
            }

            return false;
        };
    }


    public static ILoopType LOOP_TIMES_INVERSE(int amount) {
        AtomicInteger i = new AtomicInteger(0);

        return (timer, shouldRun) -> {
            if (i.get() < amount && shouldRun) {
                i.getAndIncrement();
                LOOP_INVERSE.loop(timer, true);

                return true;
            }

            return false;
        };
    }


    /**
     * Creates a {@link ILoopType} that loops while the given {@link Predicate} returns true.
     *
     * @param predicate the predicate to test
     **/
    public static ILoopType LOOP_WHILE(Predicate<TickTimer> predicate) {
        return (timer, shouldRun) -> {
            if (predicate.test(timer)) {
                LOOP.loop(timer, true);

                return true;
            }

            return false;
        };
    }


    /**
     * Same as {@link #LOOP_WHILE(Predicate)} but in inverse direction
     **/
    public static ILoopType LOOP_INVERSE_WHILE(Predicate<TickTimer> predicate) {
        return (timer, shouldRun) -> {
            if (predicate.test(timer)) {
                LOOP_INVERSE.loop(timer, true);

                return true;
            }

            return false;
        };
    }


    public static ILoopType LOOP_UNTIL(Predicate<TickTimer> predicate) {
        final boolean[] bool = {true};

        return (timer, shouldRun) -> {
            if (predicate.test(timer) && bool[0]) {
                LOOP.loop(timer, true);
                bool[0] = false;

                return true;
            }

            return false;
        };
    }


    public static ILoopType LOOP_INVERSE_UNTIL(Predicate<TickTimer> predicate) {
        final boolean[] bool = {true};

        return (timer, shouldRun) -> {
            if (predicate.test(timer) && bool[0]) {
                LOOP_INVERSE.loop(timer, true);
                bool[0] = false;

                return true;
            }

            return false;
        };
    }
}
