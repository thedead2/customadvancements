package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class ModCommand {

    public static final int COMMAND_FAILURE = -1;
    public static final int COMMAND_SUCCESS = 1;
    private static final List<ModCommand> commands = new ArrayList<>();
    private final LiteralArgumentBuilder<CommandSourceStack> shortLA;
    private final LiteralArgumentBuilder<CommandSourceStack> longLA;

    protected ModCommand(LiteralArgumentBuilder<CommandSourceStack> shortLA, LiteralArgumentBuilder<CommandSourceStack> longLA) {
        this.shortLA = shortLA;
        this.longLA = longLA;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher){
        LOGGER.debug("Registering commands...");

        GenerateGameAdvancementsCommand.register();
        GenerateResourceLocationsFileCommand.register();
        ReloadCommand.register();
        GenerateAdvancementCommand.register();
        if(isDevEnv()){
            PrintCrashLogCommand.register();
        }

        commands.forEach(modCommand -> {
            dispatcher.register(modCommand.getShortLA());
            dispatcher.register(modCommand.getLongLA());
        });
        ConfigCommand.register(dispatcher);
        LOGGER.debug("Command registration complete.");
    }

    private LiteralArgumentBuilder<CommandSourceStack> getShortLA() {
        return this.shortLA;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getLongLA() {
        return longLA;
    }


    protected static class Builder{
        static void newModCommand(String commandPath, Command<CommandSourceStack> executable) {
            newModCommand(commandPath, Collections.emptyMap(), Collections.emptyMap(), executable);
        }

        static void newModCommand(String commandPath, Map<String, ArgumentType<?>> arguments, Map<String, SuggestionProvider<CommandSourceStack>> suggestions, Command<CommandSourceStack> executable) {
            var ca = Commands.literal("ca");
            var customadvancements = Commands.literal(MOD_ID);

            List<String> commandParts = new ArrayList<>();

            if(commandPath.contains("/")){
                String sub = commandPath.replace(commandPath.substring(commandPath.indexOf("/")), "");
                commandParts.add(sub);

                String nextSub = commandPath.replace(sub + "/", "");
                resolvePath(commandParts, nextSub);
            }
            else {
                commandParts.add(commandPath);
            }

            commands.add(new ModCommand(ca.then(addToArgumentBuilder(ca, commandParts, arguments, suggestions, executable)), customadvancements.then(addToArgumentBuilder(customadvancements, commandParts, arguments, suggestions, executable))));
        }

        private static ArgumentBuilder<CommandSourceStack, ?> addToArgumentBuilder(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, List<String> commandParts, Map<String, ArgumentType<?>> arguments, Map<String, SuggestionProvider<CommandSourceStack>> suggestions, Command<CommandSourceStack> command) {
            if(commandParts.isEmpty()) return addExecutable(argumentBuilder, command);
            String s = commandParts.get(0);
            ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2;
            if(s.startsWith("[") && s.endsWith("]")) {
                if (arguments.isEmpty() || suggestions.isEmpty())
                    throw new NullPointerException("Can't create command argument because argument or suggestion is null!");

                argumentBuilder2 = addArgument(s.substring(1, s.length() - 1), arguments.get(s), suggestions.get(s));
            }
            else {
                argumentBuilder2 = addCommandPart(s);
            }
            if(!commandParts.subList(1, commandParts.size()).isEmpty())
                argumentBuilder2.then(addToArgumentBuilder(argumentBuilder2, commandParts.subList(1, commandParts.size()), arguments, suggestions, command));
            return argumentBuilder2.executes(command);
        }

        private static ArgumentBuilder<CommandSourceStack, ?> addCommandPart(String literal){
            return Commands.literal(literal);
        }

        private static ArgumentBuilder<CommandSourceStack, ?> addArgument(String name, ArgumentType<?> argumentType, SuggestionProvider<CommandSourceStack> suggestionProvider){
            if(name == null || argumentType == null || suggestionProvider == null){
                throw new NullPointerException("Can't create command argument because argument or suggestion is null!");
            }
            return Commands.argument(name, argumentType).suggests(suggestionProvider);
        }

        private static ArgumentBuilder<CommandSourceStack, ?> addExecutable(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Command<CommandSourceStack> commandAction){
            return argumentBuilder.executes(commandAction);
        }


        private static void resolvePath(List<String> arguments, String pathIn){
            if (pathIn.contains("/")){
                String temp1 = pathIn.substring(pathIn.indexOf("/"));
                String temp2 = pathIn.replace(temp1 + "/", "");
                String temp3 = temp2.replace(temp2.substring(temp2.indexOf("/")), "");
                arguments.add(temp3);

                String next = pathIn.replace((temp3 + "/"), "");
                resolvePath(arguments, next);
            }
            else arguments.add(pathIn);
        }
    }
}
