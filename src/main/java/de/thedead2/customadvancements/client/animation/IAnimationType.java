package de.thedead2.customadvancements.client.animation;

@FunctionalInterface
public interface IAnimationType {

    float transform(float from, float to, float duration, float timeLeft, IInterpolationType interpolation);
}
