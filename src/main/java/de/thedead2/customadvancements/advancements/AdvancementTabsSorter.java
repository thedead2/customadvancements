package de.thedead2.customadvancements.advancements;

import betteradvancements.gui.BetterAdvancementTab;
import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.*;

public enum AdvancementTabsSorter {
    ALPHABETICALLY{
        @Override
        protected <T> void sort(List<T> tabList) {
            tabList.sort(Comparator.comparing(t -> {
                if(t instanceof AdvancementTab advancementTab) return advancementTab.getTitle().getString();
                else if (t instanceof BetterAdvancementTab advancementTab) return advancementTab.getTitle().getString();
                else throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());
            }));
        }
    },
    DEFINED_LIST {
        @Override
        protected <T> void sort(List<T> tabList) {
            List<T> advancementTabs = new ArrayList<>();
            var list = ConfigManager.getSortedAdvancementList();
            list.forEach(resourceLocation -> getAdvancementTabFor(resourceLocation, tabList).ifPresent(advancementTabs::add));

            if(advancementTabs.size() == tabList.size()){
                tabList.clear();
                tabList.addAll(advancementTabs);
            }
            else {
                advancementTabs.forEach(advancementTab -> {
                    tabList.remove(advancementTab);
                    tabList.add(advancementTabs.indexOf(advancementTab), advancementTab);
                });
            }
        }

        private <T> Optional<T> getAdvancementTabFor(ResourceLocation resourceLocation, List<T> tabList){
            for (T t : tabList){
                ResourceLocation resourceLocation1;
                if(t instanceof AdvancementTab advancementTab){
                    resourceLocation1 = advancementTab.getAdvancement().getId();
                }
                else if(t instanceof BetterAdvancementTab advancementTab){
                    resourceLocation1 = advancementTab.getAdvancement().getId();
                }
                else throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());

                if(resourceLocation.equals(resourceLocation1)) return Optional.of(t);
            }
            return Optional.empty();
        }
    },
    UNSORTED{
        @Override
        protected <T> void sort(List<T> tabList) {}
    };

    public <T> void sortAdvancementTabs(Map<Advancement, T> tabs){
        List<T> tabList = new ArrayList<>(tabs.values());
        this.sort(tabList);
        tabs.clear();
        tabList.forEach(t -> {
            if(t instanceof AdvancementTab advancementTab){
                advancementTab.index = tabList.indexOf(t);
                tabs.put(advancementTab.getAdvancement(), t);
            }
            else if(t instanceof BetterAdvancementTab advancementTab){
                try {
                    var clazz = advancementTab.getClass();
                    Field indexField = clazz.getDeclaredField("index");
                    indexField.setAccessible(true);
                    indexField.set(advancementTab, tabList.indexOf(t));
                }
                catch(NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                tabs.put(advancementTab.getAdvancement(), t);
            }
            else throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());
        });
    }

    protected abstract <T> void sort(List<T> tabList);
}
