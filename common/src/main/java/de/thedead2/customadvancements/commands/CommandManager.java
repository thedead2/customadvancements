package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.customadvancements.CAMain;
import de.thedead2.customadvancements.util.helper.ResourceLocationHelper;
import de.thedead2.customadvancements.util.io.AdvancementHandler;
import de.thedead2.mc_libs.io.FileHandler;
import de.thedead2.customadvancements.util.localisation.ModTranslationKeys;
import de.thedead2.mc_libs.commands.CommandBuilder;
import de.thedead2.mc_libs.commands.CommandResult;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.core.ModHelper.*;
import static de.thedead2.customadvancements.util.localisation.ModTranslationKeys.*;


public class CommandManager {

    private static final Set<LiteralArgumentBuilder<CommandSourceStack>> COMMANDS = new HashSet<>();


    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
        Collection<AdvancementHolder> advancements = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();

        return SharedSuggestionProvider.suggestResource(advancements.stream().map(AdvancementHolder::id), suggestionsBuilder);
    };


    static {
        register(CommandBuilder
                .newCommand("ca/reload")
                .withAction(commandContext -> {
                    CommandSourceStack source = commandContext.getSource();

                    source.getServer().executeIfPossible(() -> {
                        source.sendSuccess(() -> ModTranslationKeys.chatMessage(RELOAD_START_MESSAGE), false);

                        reloadAll(source.getServer());

                        source.sendSuccess(() -> ModTranslationKeys.chatMessage(RELOAD_SUCCESS_MESSAGE), false);
                    });

                    return CommandResult.SUCCESS;
                })
                .build()
        );


        register(CommandBuilder
                .newCommand("ca/generate/ids")
                .withAction(command -> {
                    CommandSourceStack source = command.getSource();

                    LOGGER.info("Starting to write resource locations to file...");
                    source.sendSuccess(() -> ModTranslationKeys.chatMessage(GENERATE_IDS_MESSAGE), false);

                    Path outputPath = Paths.get(DIR_PATH + "/advancements.txt");

                    try (OutputStream fileOut = Files.newOutputStream(outputPath)) {

                        writeResourceLocations(fileOut);

                        source.sendSuccess(() -> ModTranslationKeys.chatMessage(GENERATE_IDS_SUCCESS_MESSAGE), false);

                        return CommandResult.SUCCESS;
                    }
                    catch (IOException e) {
                        source.sendFailure(ModTranslationKeys.chatMessage(GENERATE_IDS_FAILED_MESSAGE, ChatFormatting.RED));
                        LOGGER.error("Unable to write resource locations to file!", e);

                        return CommandResult.FAILURE;
                    }
                })
                .build()
        );


        register(CommandBuilder
                .newCommand("ca/generate/advancement/all")
                .withAction((command) -> {
                    CommandSourceStack source = command.getSource();
                    HolderLookup.Provider registryAccess = source.registryAccess();

                    source.getServer().executeIfPossible(() -> {
                        LOGGER.info("Starting to generate files for game advancements...");
                        source.sendSuccess(() -> ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENTS_MESSAGE), false);

                        long startTime = System.currentTimeMillis();
                        AtomicInteger counter = new AtomicInteger();

                        FileHandler.createDirectoryIfNecessary(DIR_PATH.toFile());

                        source.getServer().getAdvancements().getAllAdvancements().forEach((advancement) -> {
                            try {
                                if(!CAMain.getInstance().getCustomAdvancementManager().isCustomAdvancement(ResourceLocationHelper.addFileExtension(advancement.id(), ".json"))) {
                                    AdvancementHandler.writeAdvancementToFile(advancement, registryAccess);
                                    counter.getAndIncrement();
                                }
                                else {
                                    source.sendFailure(ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENT_FILE_ALREADY_EXISTS_MESSAGE, ChatFormatting.RED, advancement.id().toString()));
                                    LOGGER.info("Skipping {} as it is already a custom advancement", advancement.id());
                                }
                            }
                            catch (IOException e) {
                                source.sendFailure(ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENT_FILE_FAILED_MESSAGE, ChatFormatting.RED, advancement.id()));
                                LOGGER.warn("Unable to write {} to file!", advancement.id(), e);
                            }
                        });

                        LOGGER.info("Generating {} files for game advancements took {} ms", counter.get(), System.currentTimeMillis() - startTime);
                        source.sendSuccess(() -> ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENTS_SUCCESS_MESSAGE, counter.get()), false);

                        reloadAll(source.getServer());
                    });

                    return CommandResult.SUCCESS;
                })
                .build()
        );


        register(CommandBuilder
                .newCommand("ca/generate/advancement/[advancement]")
                .withArgument("[advancement]", ResourceLocationArgument.id())
                .withSuggestion("[advancement]", SUGGEST_ADVANCEMENTS)
                .withAction(command -> {
                    AdvancementHolder advancement = ResourceLocationArgument.getAdvancement(command, "advancement");
                    ResourceLocation advancementId = advancement.id();
                    CommandSourceStack source = command.getSource();

                    if (advancementId.getNamespace().equals(MOD_ID)) {
                        source.sendFailure(ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENT_FILE_ALREADY_EXISTS_MESSAGE, ChatFormatting.RED, advancementId.toString()));

                        return CommandResult.FAILURE;
                    }

                    try {
                        HolderLookup.Provider registryAccess = source.registryAccess();
                        AdvancementHandler.writeAdvancementToFile(advancement, registryAccess);
                    }
                    catch (IOException e) {
                        source.sendFailure(ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENT_FILE_FAILED_MESSAGE, advancementId));
                        LOGGER.warn("Unable to generate file for: {}", advancementId, e);

                        return CommandResult.FAILURE;
                    }

                    source.sendSuccess(() -> ModTranslationKeys.chatMessage(GENERATE_ADVANCEMENT_FILE_SUCCESS_MESSAGE, advancementId.toString()), false);
                    LOGGER.debug("Successfully generated file for: {}", advancementId);

                    source.getServer().executeIfPossible(() -> reloadAll(command.getSource().getServer()));

                    return CommandResult.FAILURE;
                })
                .build()
        );
    }


    private CommandManager() {}


    public static void registerCommandsToDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        LOGGER.debug("Registering commands...");

        COMMANDS.forEach(dispatcher::register);
    }

    private static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        COMMANDS.add(command);
    }


    private static void writeResourceLocations(OutputStream fileOut) throws IOException {
        for (ResourceLocation resourceLocation : CAMain.getInstance().getCustomAdvancementManager().getAllAdvancementIds()) {
            String id = resourceLocation.toString().replace(".json", "") + ",\n";

            InputStream inputStream = new ByteArrayInputStream(id.getBytes());

            FileHandler.writeToFile(inputStream, fileOut);
        }
    }
}
