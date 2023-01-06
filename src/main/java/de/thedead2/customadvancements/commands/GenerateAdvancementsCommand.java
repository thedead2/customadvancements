package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static de.thedead2.customadvancements.util.ModHelper.FILE_HANDLER;

public class GenerateAdvancementsCommand {

    public GenerateAdvancementsCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("game_advancements").executes((command) -> FILE_HANDLER.generateGameAdvancements(command.getSource())))));
    }


}
