package de.thedead2.customadvancements.advancements;

import de.thedead2.customadvancements.util.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public enum AdvancementTabsSorter {
    ALPHABETICALLY{
        @Override
        protected void sort(List<AdvancementTab> tabList) {
            tabList.sort(Comparator.comparing(o -> o.getTitle().getString()));
        }
    },
    DEFINED_LIST {
        @Override
        protected void sort(List<AdvancementTab> tabList) {
            List<AdvancementTab> advancementTabs = new ArrayList<>();
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

        private Optional<AdvancementTab> getAdvancementTabFor(ResourceLocation resourceLocation, List<AdvancementTab> tabList){
            for (AdvancementTab advancementTab : tabList){
                ResourceLocation resourceLocation1 = advancementTab.getAdvancement().getId();
                if(resourceLocation1.equals(resourceLocation)) return Optional.of(advancementTab);
            }
            return Optional.empty();
        }
    },
    UNSORTED{
        @Override
        protected void sort(List<AdvancementTab> tabList) {}
    };

    public void sortAdvancementTabs(Map<Advancement, AdvancementTab> tabs){
        List<AdvancementTab> tabList = new ArrayList<>(tabs.values());
        this.sort(tabList);
        tabs.clear();
        tabList.forEach(advancementTab -> {
            advancementTab.index = tabList.indexOf(advancementTab);
            tabs.put(advancementTab.getAdvancement(), advancementTab);
        });
    }

    protected abstract void sort(List<AdvancementTab> tabList);
}
