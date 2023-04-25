package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Collection;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class GenerateAdvancementCommand extends ModCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
        Collection<Advancement> collection = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), suggestionsBuilder);
    };

    protected GenerateAdvancementCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }


    public static void register(){
        newModCommand("generate/advancement/[advancement]", ResourceLocationArgument.id(), SUGGEST_ADVANCEMENTS, command -> {

            ResourceLocation advancement = ResourceLocationArgument.getAdvancement(command, "advancement").getId();
            if (advancement.getNamespace().equals(MOD_ID)){
                command.getSource().sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancement_already_exists", ChatFormatting.RED, advancement.toString()));
                return COMMAND_FAILURE;
            }
            try {
                AdvancementHandler.writeAdvancementToFile(ResourceLocationArgument.getAdvancement(command, "advancement"));
            } catch (IOException e) {
                command.getSource().sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancement_failed", advancement));
                CrashHandler.getInstance().handleException("Unable to generate file for: " + advancement, e, Level.WARN);
                return COMMAND_FAILURE;
            }
            command.getSource().sendSuccess(TranslationKeyProvider.chatMessage("generating_game_advancement_success", advancement.toString()), false);
            LOGGER.debug("Successfully generated file for: " + advancement);
            reloadAll(command.getSource().getServer());
            return COMMAND_SUCCESS;
        });
    }
}
