package de.thedead2.customadvancements.generator;

public interface IAdvancementGenerator {

    void createDisplayInfo();

    void createRewards();

    void resolveParent();

    void createCriteria();

    void createRequirements();

    void resolveId();

    void build();


}
