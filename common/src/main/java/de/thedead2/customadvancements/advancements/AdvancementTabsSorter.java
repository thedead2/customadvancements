package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ImmutableList;
import de.thedead2.customadvancements.util.ConfigManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.ResourceLocation;

import java.util.*;


public enum AdvancementTabsSorter {

    ALPHABETICALLY {
        @Override
        protected void sort(List<AdvancementTab> tabList) {
            tabList.sort(Comparator.comparing(advancementTab -> advancementTab.getTitle().getString()));
        }
    },

    DEFINED_LIST {
        @Override
        protected void sort(List<AdvancementTab> tabList) {
            List<AdvancementTab> advancementTabs = new ArrayList<>();
            ImmutableList<ResourceLocation> sortedAdvancementList = ConfigManager.getSortedAdvancementList();

            sortedAdvancementList.forEach(resourceLocation -> getAdvancementTabFor(resourceLocation, tabList).ifPresent(advancementTabs::add));

            if (advancementTabs.size() == tabList.size()) {
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


        private Optional<AdvancementTab> getAdvancementTabFor(ResourceLocation resourceLocation, List<AdvancementTab> tabList) {
            for (AdvancementTab advancementTab : tabList) {
                ResourceLocation advancementId = advancementTab.getRootNode().holder().id();

                if (resourceLocation.equals(advancementId)) {
                    return Optional.of(advancementTab);
                }
            }

            return Optional.empty();
        }
    },

    UNSORTED {
        @Override
        protected void sort(List<AdvancementTab> tabList) {}
    };


    public void sortAdvancementTabs(Map<AdvancementHolder, AdvancementTab> tabs) {
        List<AdvancementTab> tabList = new ArrayList<>(tabs.values());

        this.sort(tabList);
        tabs.clear();

        tabList.forEach(advancementTab -> {
            advancementTab.index = tabList.indexOf(advancementTab);

            tabs.put(advancementTab.getRootNode().holder(), advancementTab);
        });
    }


    protected abstract void sort(List<AdvancementTab> tabList);
}
