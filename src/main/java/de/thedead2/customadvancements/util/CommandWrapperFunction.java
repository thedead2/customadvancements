package de.thedead2.customadvancements.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface CommandWrapperFunction {
    int runCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
}
