package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Collection;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class GenerateAdvancementCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
        Collection<Advancement> collection = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), suggestionsBuilder);
    };

    public GenerateAdvancementCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(ModHelper.MOD_ID).then(Commands.literal("generate").then(Commands.literal("advancement").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes(context -> {
            ResourceLocation advancement = ResourceLocationArgument.getAdvancement(context, "advancement").getId();
            try {
                if (advancement.getNamespace().equals(MOD_ID)){
                    context.getSource().sendFailure(Component.literal("The file for " + advancement + " already exist!"));
                    return -1;
                }
                AdvancementHandler.writeAdvancementToFile(advancement, ALL_DETECTED_GAME_ADVANCEMENTS.get(advancement));
                context.getSource().sendSuccess(Component.literal("Successfully generated file for: " + advancement), false);
                LOGGER.debug("Successfully generated file for: " + advancement);
                reloadAll(context.getSource().getServer());
                return Command.SINGLE_SUCCESS;
            }
            catch (IOException e) {
                context.getSource().sendFailure(Component.literal("Unable to generate file for: " + advancement));
                LOGGER.error("Unable to generate file for: " + advancement);
                CrashHandler.getInstance().addCrashDetails("Unable to generate file for advancement", Level.WARN, e);
                e.printStackTrace();
                return -1;
            }
        })))));
    }
}
