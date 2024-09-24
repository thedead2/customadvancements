package de.thedead2.customadvancements.client.animation;


public class AnimationTypes {

    public static final IAnimationType EASE_IN = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float timePassed = duration - timeLeft;
        float percentDone = timePassed / duration;

        return from + dif * interpolation.apply(percentDone);
    };

    public static final IAnimationType EASE_OUT = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float percentLeft = timeLeft / duration;

        return to - dif * interpolation.apply(percentLeft);
    };

    public static final IAnimationType EASE_IN_OUT = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float halfWay = from + dif / 2;
        float halfTime = duration / 2;

        if (timeLeft > halfTime) {
            return EASE_IN.transform(from, halfWay, halfTime, timeLeft - halfTime, interpolation);
        }
        else {
            return EASE_OUT.transform(halfWay, to, halfTime, timeLeft, interpolation);
        }
    };


    public static IAnimationType STEPS(int steps) {
        return (from, to, duration, timeLeft, interpolation) -> {
            float dif = to - from;
            float amountPerStep = dif / steps;
            float timePerStep = duration / steps;
            int stepsLeft = (int) (timeLeft / timePerStep);
            int stepsPassed = steps - stepsLeft;

            return from + amountPerStep * stepsPassed;
        };
    }


    //Es gibt eine gewisse anzahl an schritten die ausgeführt werden müssen
    //länge eines schrittes hängt von der noch verbleibenden anzahl an schritten und zeit ab
    public static IAnimationType NATURAL_STEPS(int steps) {
        return (from, to, duration, timeLeft, interpolation) -> {
            float dif = to - from;
            float amountPerStep = dif / steps;
            float timePerStep = duration / steps;
            int stepsLeft = (int) (timeLeft / timePerStep);
            int stepsPassed = steps - stepsLeft;

            float percentStepsDone = (float) stepsPassed / steps;

            return from + dif * percentStepsDone;
        };
    }
}
