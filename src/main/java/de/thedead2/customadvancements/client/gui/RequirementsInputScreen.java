package de.thedead2.customadvancements.client.gui;

import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ServerAdvancementGenerator;
import net.minecraft.client.Minecraft;

public class RequirementsInputScreen extends BasicInputScreen {


    protected RequirementsInputScreen(AdvancementGeneratorGUI parent, Minecraft minecraft, ServerAdvancementGenerator generator) {
        super(parent, minecraft, generator);
    }
}
