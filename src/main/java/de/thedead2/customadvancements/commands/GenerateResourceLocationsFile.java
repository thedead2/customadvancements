package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import static de.thedead2.customadvancements.util.ModHelper.FILE_HANDLER;

public class GenerateResourceLocationsFile {
    public GenerateResourceLocationsFile(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("resource_locations").executes((command) -> FILE_HANDLER.printResourceLocations(command.getSource())))));
    }
}
