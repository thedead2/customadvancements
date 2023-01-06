package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.util.ModHelper;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import static de.thedead2.customadvancements.util.ModHelper.FILE_HANDLER;

public class ReloadCommand {
    public ReloadCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("reload").executes((command) -> FILE_HANDLER.reload(command.getSource()))));
    }
}
