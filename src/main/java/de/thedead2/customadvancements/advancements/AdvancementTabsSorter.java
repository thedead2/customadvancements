package de.thedead2.customadvancements.advancements;

import betteradvancements.common.gui.BetterAdvancementTab;
import com.google.common.collect.ImmutableList;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.*;

import static de.thedead2.customadvancements.util.core.ModHelper.BA_COMPATIBILITY;


public enum AdvancementTabsSorter {

    ALPHABETICALLY {
        @Override
        protected <T> void sort(List<T> tabList) {
            tabList.sort(Comparator.comparing(t -> {
                if (t instanceof AdvancementTab advancementTab) {
                    return advancementTab.getTitle().getString();
                }
                else if (BA_COMPATIBILITY.get() && t instanceof BetterAdvancementTab advancementTab) {
                    return advancementTab.getTitle().getString();
                }
                else {
                    throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());
                }
            }));
        }
    },

    DEFINED_LIST {
        @Override
        protected <T> void sort(List<T> tabList) {
            List<T> advancementTabs = new ArrayList<>();
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


        private <T> Optional<T> getAdvancementTabFor(ResourceLocation resourceLocation, List<T> tabList) {
            for (T t : tabList) {
                ResourceLocation advancementId;

                if (t instanceof AdvancementTab advancementTab) {
                    advancementId = advancementTab.getAdvancement().getId();
                }
                else if (BA_COMPATIBILITY.get() && t instanceof BetterAdvancementTab advancementTab) {
                    advancementId = advancementTab.getAdvancement().getId();
                }
                else {
                    throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());
                }

                if (resourceLocation.equals(advancementId)) {
                    return Optional.of(t);
                }
            }

            return Optional.empty();
        }
    },

    UNSORTED {
        @Override
        protected <T> void sort(List<T> tabList) {}
    };


    public <T> void sortAdvancementTabs(Map<Advancement, T> tabs) {
        List<T> tabList = new ArrayList<>(tabs.values());

        this.sort(tabList);
        tabs.clear();

        tabList.forEach(t -> {
            if (t instanceof AdvancementTab advancementTab) {
                advancementTab.index = tabList.indexOf(t);

                tabs.put(advancementTab.getAdvancement(), t);
            }
            else if (BA_COMPATIBILITY.get() && t instanceof BetterAdvancementTab advancementTab) {
                try {
                    var clazz = advancementTab.getClass();
                    Field indexField = clazz.getDeclaredField("index");

                    indexField.setAccessible(true);
                    indexField.set(advancementTab, tabList.indexOf(t));
                }
                catch (NoSuchFieldException | IllegalAccessException e) {
                    CrashHandler.getInstance().handleException("Failed to sort advancement tabs of BetterAdvancementsScreen!", "AdvancementTabsSorter", e, Level.ERROR);
                }

                tabs.put(advancementTab.getAdvancement(), t);
            }
            else {
                throw new IllegalArgumentException("Unknown Advancement Tab Type: " + t.getClass());
            }
        });
    }


    protected abstract <T> void sort(List<T> tabList);
}
