package de.thedead2.customadvancements.commands;

import net.minecraft.world.level.GameRules;

public class ModGameRules {

    public static GameRules.Key<GameRules.BooleanValue> ADVANCEMENT_GENERATOR_GAMERULE;

    public static void register(){
        ADVANCEMENT_GENERATOR_GAMERULE = GameRules.register("enableAdvancementGenerator", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
    }
}
