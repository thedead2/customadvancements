package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.io.AdvancementHandler;
import de.thedead2.customadvancements.util.localisation.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class GenerateAdvancementCommand extends ModCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
        Collection<Advancement> advancements = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();

        return SharedSuggestionProvider.suggestResource(advancements.stream().map(Advancement::getId), suggestionsBuilder);
    };


    protected GenerateAdvancementCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, LiteralArgumentBuilder<CommandSourceStack> shortLiteralArgumentBuilder) {
        super(literalArgumentBuilder, shortLiteralArgumentBuilder);
    }


    public static void register() {
        Builder.newModCommand("generate/advancement/[advancement]", Map.of("[advancement]", ResourceLocationArgument.id()), Map.of("[advancement]", SUGGEST_ADVANCEMENTS), command -> {
            Advancement advancement = ResourceLocationArgument.getAdvancement(command, "advancement");
            ResourceLocation advancementId = advancement.getId();
            CommandSourceStack source = command.getSource();

            if (advancementId.getNamespace().equals(MOD_ID)) {
                source.sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancement_already_exists", ChatFormatting.RED, advancementId.toString()));

                return COMMAND_FAILURE;
            }

            try {
                AdvancementHandler.writeAdvancementToFile(advancement);
            }
            catch (IOException e) {
                source.sendFailure(TranslationKeyProvider.chatMessage("generating_game_advancement_failed", advancementId));
                CrashHandler.getInstance().handleException("Unable to generate file for: " + advancementId, e, Level.WARN);

                return COMMAND_FAILURE;
            }

            source.sendSuccess(() -> TranslationKeyProvider.chatMessage("generating_game_advancement_success", advancementId.toString()), false);
            LOGGER.debug("Successfully generated file for: {}", advancementId);

            source.getServer().executeIfPossible(() -> reloadAll(command.getSource().getServer()));

            return COMMAND_SUCCESS;
        });
    }
}
