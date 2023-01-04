package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import static de.thedead2.customadvancements.util.ModHelper.FILE_HANDLER;

public class GenerateAdvancementsCommand {

    public GenerateAdvancementsCommand(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("advancements").executes((command) -> FILE_HANDLER.generateGameAdvancements(command.getSource())))));
    }


}
