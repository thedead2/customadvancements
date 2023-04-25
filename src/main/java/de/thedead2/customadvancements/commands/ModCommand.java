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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.core.ModHelper.isDevEnv;

public class ModCommand {

    public static final int COMMAND_FAILURE = -1;
    public static final int COMMAND_SUCCESS = 1;
    private static final List<ModCommand> commands = new ArrayList<>();
    private final LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder;

    protected ModCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        this.literalArgumentBuilder = literalArgumentBuilder;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher){
        LOGGER.debug("Registering commands...");

        GenerateGameAdvancementsCommand.register();
        GenerateResourceLocationsFileCommand.register();
        ReloadCommand.register();
        GenerateAdvancementCommand.register();
        if(isDevEnv()) PrintCrashLogCommand.register();

        commands.forEach(modCommand -> dispatcher.register(modCommand.getLiteralArgumentBuilder()));
        ConfigCommand.register(dispatcher);
        LOGGER.debug("Command registration complete.");
    }

    private LiteralArgumentBuilder<CommandSourceStack> getLiteralArgumentBuilder() {
        return this.literalArgumentBuilder;
    }

    static void newModCommand(String commandPath, Command<CommandSourceStack> executable) {
        newModCommand(commandPath, null, null, executable);
    }

    static void newModCommand(String commandPath, @Nullable ArgumentType<?> argument, @Nullable SuggestionProvider<CommandSourceStack> suggestion, Command<CommandSourceStack> executable) {
        var argumentBuilder = Commands.literal("ca");

        List<String> arguments = new ArrayList<>();

        if(commandPath.contains("/")){
            String sub = commandPath.replace(commandPath.substring(commandPath.indexOf("/")), "");
            arguments.add(sub);

            String nextSub = commandPath.replace(sub + "/", "");
            resolvePath(arguments, nextSub);
        }
        else {
            arguments.add(commandPath);
        }

        commands.add(new ModCommand(argumentBuilder.then(addToArgumentBuilder(argumentBuilder, arguments, argument, suggestion, executable))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addToArgumentBuilder(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, List<String> arguments, ArgumentType<?> argument, SuggestionProvider<CommandSourceStack> suggestion, Command<CommandSourceStack> command) {
        String sub;
        if(!arguments.isEmpty()) { //reload all -> 2
            sub = arguments.get(0); // reload
            ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2;
            if(sub.startsWith("[") && sub.endsWith("]")) {
                if (argument == null || suggestion == null)
                    throw new NullPointerException("Can't create command argument because argument or suggestion is null!");

                argumentBuilder2 = Commands.argument(sub.substring(1, sub.length() - 1), argument).suggests(suggestion);
            }
            else {
                argumentBuilder2 = Commands.literal(sub); //enthält reload
            }
            if(!arguments.subList(1, arguments.size()).isEmpty())
                argumentBuilder2.then(addToArgumentBuilder(argumentBuilder2, arguments.subList(1, arguments.size() /*sublist 1 -> all */), argument, suggestion, command));
            return argumentBuilder2.executes(command);
        }
        else return argumentBuilder.executes(command);
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
