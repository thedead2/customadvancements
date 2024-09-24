package de.thedead2.customadvancements.client.animation;

import net.minecraft.util.Mth;

import java.util.Random;


public class InterpolationTypes {

    public static final IInterpolationType LINEAR = val -> val;

    public static final IInterpolationType QUADRATIC = val -> val * val;

    public static final IInterpolationType CUBIC = val -> val * val * val;

    public static final IInterpolationType QUARTIC = val -> val * val * val * val;

    public static final IInterpolationType QUINTIC = val -> val * val * val * val * val;

    public static final IInterpolationType SINUS = val -> (float) ((float) 1 - Math.cos(val * Math.PI / 2f));

    public static final IInterpolationType CIRCLE = val -> (float) (1 - Math.sqrt(1 - val * val));

    public static final IInterpolationType EXPONENTIAL = val -> (float) Math.pow(2, 10 * (val - 1));

    public static final IInterpolationType ELASTIC = val -> val == 0 ? 0 : (float) (val == 1 ? 1 : -Math.pow(2, 10 * val - 10) * Math.sin((val * 10 - 10.75f) * (2 * Math.PI) / 3));

    public static final IInterpolationType BOUNCE = val -> 1 - easeOutBounce(1 - val);

    public static final IInterpolationType BACK = val -> {
        float c1 = 1.70158f;
        float c3 = c1 + 1;

        return c3 * val * val * val - c1 * val * val;
    };

    private static final Random RANDOM_GEN = new Random();

    public static final IInterpolationType RANDOM = val -> val * RANDOM_GEN.nextFloat(0.75f, 1.25f);


    public static IInterpolationType CATMULL_ROM(float pointA, float pointB, float pointC, float pointD) {
        return val -> Mth.catmullrom(val, pointA, pointB, pointC, pointD);
    }


    private static float easeOutBounce(float val) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (val < 1 / d1) {
            return n1 * val * val;
        }
        else if (val < 2f / d1) {
            return n1 * (val -= 1.5f / d1) * val + 0.75f;
        }
        else if (val < 2.5f / d1) {
            return n1 * (val -= 2.25f / d1) * val + 0.9375f;
        }
        else {
            return n1 * (val -= 2.625f / d1) * val + 0.984375f;
        }
    }
}
