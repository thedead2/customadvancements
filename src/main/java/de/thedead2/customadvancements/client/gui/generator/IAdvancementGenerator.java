package de.thedead2.customadvancements.client.gui.generator;

public interface IAdvancementGenerator {

    void createDisplayInfo();

    void createRewards();

    void resolveParent();

    void createCriteria();

    void createRequirements();

    void resolveId();

    void build();


}
