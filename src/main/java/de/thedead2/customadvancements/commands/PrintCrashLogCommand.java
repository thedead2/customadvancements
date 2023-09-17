package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.core.CrashHandler;

import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;


public class PrintCrashLogCommand extends ModCommand{

    protected PrintCrashLogCommand(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    protected static void register(){
        newModCommand("test/crash", context -> {
            CrashHandler.getInstance().printCrashReport(new CrashReport("Just testing", new Throwable()));
            return COMMAND_SUCCESS;
        });
    }
}
