package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.generator.AdvancementGenerator;
import de.thedead2.customadvancements.generator.gui.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AdvancementGeneratorCommand {

    public AdvancementGeneratorCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("advancement").executes((command) -> {
            Minecraft.getInstance().setScreen(new AdvancementGeneratorGUI(Minecraft.getInstance().player.connection.getAdvancements()));
            return 1;
        }))));
    }
}
