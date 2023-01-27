package de.thedead2.customadvancements.commands;

import net.minecraft.world.level.GameRules;

public class ModGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> ADVANCEMENT_GENERATOR_GAMERULE = GameRules.register("enableAdvancementGenerator", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
}
