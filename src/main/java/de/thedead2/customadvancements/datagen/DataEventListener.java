package de.thedead2.customadvancements.datagen;

import de.thedead2.customadvancements.datagen.dataprovider.ModLanguageProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEventListener {

    @SubscribeEvent
    public static void onDataGeneration(final GatherDataEvent event){
        DataGenerator generator = event.getGenerator();

        generator.addProvider(new ModLanguageProvider(generator, MOD_ID, "en_us"));
        generator.addProvider(new ModLanguageProvider(generator, MOD_ID, "de_de"));
    }
}
