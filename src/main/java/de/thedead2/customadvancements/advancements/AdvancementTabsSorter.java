package de.thedead2.customadvancements.advancements;

import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.advancements.AdvancementTabGui;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public enum AdvancementTabsSorter {
    ALPHABETICALLY{
        @Override
        protected void sort(List<AdvancementTabGui> tabList) {
            tabList.sort(Comparator.comparing(o -> o.getTitle().getString()));
        }
    },
    DEFINED_LIST {
        @Override
        protected void sort(List<AdvancementTabGui> tabList) {
            List<AdvancementTabGui> advancementTabs = new ArrayList<>();
            com.google.common.collect.ImmutableList<ResourceLocation> list = ConfigManager.getSortedAdvancementList();
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

        private Optional<AdvancementTabGui> getAdvancementTabFor(ResourceLocation resourceLocation, List<AdvancementTabGui> tabList){
            for (AdvancementTabGui advancementTab : tabList){
                ResourceLocation resourceLocation1 = advancementTab.getAdvancement().getId();
                if(resourceLocation1.equals(resourceLocation)) return Optional.of(advancementTab);
            }
            return Optional.empty();
        }
    },
    UNSORTED{
        @Override
        protected void sort(List<AdvancementTabGui> tabList) {}
    };

    public void sortAdvancementTabs(Map<Advancement, AdvancementTabGui> tabs){
        List<AdvancementTabGui> tabList = new ArrayList<>(tabs.values());
        this.sort(tabList);
        tabs.clear();
        tabList.forEach(advancementTab -> {
            advancementTab.index = tabList.indexOf(advancementTab);
            tabs.put(advancementTab.getAdvancement(), advancementTab);
        });
    }

    protected abstract void sort(List<AdvancementTabGui> tabList);
}
