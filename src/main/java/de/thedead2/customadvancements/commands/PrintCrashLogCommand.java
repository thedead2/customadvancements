package de.thedead2.customadvancements.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.commands.CommandSourceStack;

public class PrintCrashLogCommand extends ModCommand{

    protected PrintCrashLogCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) {
        super(literalArgumentBuilder);
    }

    protected static void register(){
        newModCommand("test/crash", context -> {
            CrashHandler.getInstance().printCrashReport(new CrashReport("Just testing", new Throwable()));
            return COMMAND_SUCCESS;
        });
    }
}
