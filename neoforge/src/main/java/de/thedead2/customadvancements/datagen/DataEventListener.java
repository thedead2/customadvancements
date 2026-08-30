package de.thedead2.customadvancements.datagen;


import de.thedead2.customadvancements.datagen.dataprovider.ModLanguageProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;


@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataEventListener {

    @SubscribeEvent
    public static void onDataGeneration(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MOD_ID, "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MOD_ID, "de_de"));
    }
}
