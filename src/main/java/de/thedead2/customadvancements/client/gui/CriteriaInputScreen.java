package de.thedead2.customadvancements.client.gui;

import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ServerAdvancementGenerator;
import net.minecraft.client.Minecraft;

public class CriteriaInputScreen extends BasicInputScreen {


    protected CriteriaInputScreen(AdvancementGeneratorGUI parent, Minecraft minecraft, ServerAdvancementGenerator generator) {
        super(parent, minecraft, generator);
    }
}
