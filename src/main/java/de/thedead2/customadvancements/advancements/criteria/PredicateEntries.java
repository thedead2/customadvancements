package de.thedead2.customadvancements.advancements.criteria;

import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class PredicateEntries {
    public static final Predicate.Entry<String> STRING = new Predicate.Entry<>("String", s -> s, s -> true);
    public static final Predicate.Entry<Boolean> BOOLEAN = new Predicate.Entry<>("boolean", false, Boolean::parseBoolean, (s) -> s.equals("true") || s.equals("false"), true, false);
    public static final Predicate.Entry<Integer> INTEGER = new Predicate.Entry<>("int", 0, Integer::parseInt, s -> {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e){
            return false;
        }
    });
    public static final Predicate.Entry<Double> DOUBLE = new Predicate.Entry<>("double", 0.0, Double::parseDouble, s -> {
        try {
            Double.parseDouble(s);
            return true;
        }
        catch (NumberFormatException | NullPointerException e){
            return false;
        }
    });
    public static final Predicate.Entry<Float> FLOAT = new Predicate.Entry<>("float", 0.0f, Float::parseFloat, s -> {
        try {
            Float.parseFloat(s);
            return true;
        }
        catch (NumberFormatException | NullPointerException e){
            return false;
        }
    });
    public static final Predicate.Entry<Short> SHORT = new Predicate.Entry<>("short", (short) 0, Short::parseShort, s -> {
        try {
            Short.parseShort(s);
            return true;
        }
        catch (NumberFormatException | NullPointerException e){
            return false;
        }
    });
    public static final Predicate.Entry<Long> LONG = new Predicate.Entry<>("long", (long) 0, Long::parseLong, s -> {
        try {
            Long.parseLong(s);
            return true;
        }
        catch (NumberFormatException | NullPointerException e){
            return false;
        }
    });
    public static final Predicate.Entry<Byte> BYTE = new Predicate.Entry<>("byte", (byte) 0, Byte::parseByte, s -> {
        try {
            Byte.parseByte(s);
            return true;
        }
        catch (NumberFormatException | NullPointerException e){
            return false;
        }
    });

    public static final Predicate.Entry<Item> ITEM = new Predicate.Entry<>("ResourceLocation:item", (s) -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.ITEMS.getValues());
    public static final Predicate.Entry<Potion> POTION = new Predicate.Entry<>("ResourceLocation:potion", (s) -> ForgeRegistries.POTIONS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.POTIONS.getValues());
    public static final Predicate.Entry<Biome> BIOME = new Predicate.Entry<>("ResourceLocation:biome", (s) -> ForgeRegistries.BIOMES.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.BIOMES.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.BIOMES.getValues());
    public static final Predicate.Entry<Fluid> FLUID = new Predicate.Entry<>("ResourceLocation:fluid", (s) -> ForgeRegistries.FLUIDS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.FLUIDS.getValues());
    public static final Predicate.Entry<Block> BLOCK = new Predicate.Entry<>("ResourceLocation:block", (s) -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.BLOCKS.getValues());
    public static final Predicate.Entry<Enchantment> ENCHANTMENT = new Predicate.Entry<>("ResourceLocation:enchantment", (s) -> ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.ENCHANTMENTS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.ENCHANTMENTS.getValues());
    public static final Predicate.Entry<CriterionTrigger<?>> ADVANCEMENT_CRITERION = new Predicate.Entry<>("ResourceLocation:criterion", (s) -> CriteriaTriggers.getCriterion(new ResourceLocation(s)), s -> {
        try {
            return CriteriaConditionsIdentifier.CRITERIA_TRIGGERS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, CriteriaConditionsIdentifier.CRITERIA_TRIGGERS.values());
    public static final Predicate.Entry<EntityType<?>> ENTITY = new Predicate.Entry<>("ResourceLocation:entity", (s) -> ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.ENTITY_TYPES.getValues());
    public static final Predicate.Entry<MobEffect> EFFECT = new Predicate.Entry<>("ResourceLocation:effect", (s) -> ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.MOB_EFFECTS.getValues());
    public static final Predicate.Entry<StatType<?>> STATISTIC = new Predicate.Entry<>("ResourceLocation:statistic", (s) -> ForgeRegistries.STAT_TYPES.getValue(new ResourceLocation(s)), s -> {
        try {
            return ForgeRegistries.STAT_TYPES.containsKey(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, ForgeRegistries.STAT_TYPES.getValues());
    public static final Predicate.Entry<Advancement> ADVANCEMENT = new Predicate.Entry<>("ResourceLocation:advancement", s -> Minecraft.getInstance().getSingleplayerServer().getAdvancements().getAdvancement(new ResourceLocation(s)), s -> {
        try {
            return Minecraft.getInstance().getSingleplayerServer().getAdvancements().getAllAdvancements().stream().map(Advancement::getId).toList().contains(new ResourceLocation(s));
        } catch (ResourceLocationException e) {
            return false;
        }
    }, Minecraft.getInstance().getSingleplayerServer().getAdvancements().getAllAdvancements());
    public static final Predicate.Entry<Recipe<?>> RECIPE = new Predicate.Entry<>("ResourceLocation:recipe", s -> Minecraft.getInstance().getSingleplayerServer().getRecipeManager().byKey(new ResourceLocation(s)).orElseThrow(), s -> {
        try {
            return Minecraft.getInstance().getSingleplayerServer().getRecipeManager().getRecipeIds().toList().contains(new ResourceLocation(s));
        }catch (ResourceLocationException e){
            return false;
        }
    }, Minecraft.getInstance().getSingleplayerServer().getRecipeManager().getRecipes());
    public static final Predicate.Entry<?> DIMENSION = new Predicate.Entry<>("ResourceLocation:dimension", );
    public static final Predicate.Entry<?> STRUCTURE = new Predicate.Entry<>("ResourceLocation:structure", );
    public static final Predicate.Entry<LootTable> LOOT_TABLE = new Predicate.Entry<>("ResourceLocation:loot_table", s -> Minecraft.getInstance().getSingleplayerServer().getLootTables().get(new ResourceLocation(s)), s -> {
        try {
            return Minecraft.getInstance().getSingleplayerServer().getLootTables().getIds().contains(new ResourceLocation(s));
        }catch (ResourceLocationException e){
            return false;
        }
    }, Minecraft.getInstance().getSingleplayerServer().getLootTables().tables.values());
    public static final Predicate.Entry<?> ITEM_TAG = new Predicate.Entry<>("ResourceLocation:item_tag", );
    public static final Predicate.Entry<?> BLOCK_TAG = new Predicate.Entry<>("ResourceLocation:block_tag", );
    public static final Predicate.Entry<?> FLUID_TAG = new Predicate.Entry<>("ResourceLocation:fluid_tag", );
}
