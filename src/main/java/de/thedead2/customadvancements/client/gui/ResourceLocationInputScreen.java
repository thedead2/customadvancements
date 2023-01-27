package de.thedead2.customadvancements.client.gui;

import de.thedead2.customadvancements.generator.ServerAdvancementGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ResourceLocationInputScreen extends BasicInputScreen {


    protected ResourceLocationInputScreen(Screen parent, Minecraft minecraft, ServerAdvancementGenerator generator) {
        super(parent, minecraft, generator);
    }
}
