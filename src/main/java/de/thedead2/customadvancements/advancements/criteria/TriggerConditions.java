package de.thedead2.customadvancements.advancements.criteria;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.advancements.CriterionTrigger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.thedead2.customadvancements.advancements.criteria.CriteriaConditionsIdentifier.ALL_PREDICATES;

public class TriggerConditions {

    private final Trigger trigger;
    private final Map<Predicate.Key, Predicate> predicates;

    private TriggerConditions(Trigger trigger, Map<Predicate.Key, Predicate> predicates){
        this.trigger = trigger;
        this.predicates = predicates;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Set<Predicate.Key> getKeys() {
        return predicates.keySet();
    }

    public Collection<Predicate> getValues() {
        return predicates.values();
    }

    public Map<Predicate.Key, Predicate> getPredicates() {
        return predicates;
    }

    public CriterionTrigger<?> getCriterionTrigger(){
        return trigger.getCriterionTrigger();
    }

    public static TriggerConditions fromTrigger(Trigger trigger) {
        Map<Predicate.Key, Predicate> map = new HashMap<>();
        trigger.getPredicateKeys().forEach(key -> {
            map.put(key, ALL_PREDICATES.get(key));
        });

        return new TriggerConditions(trigger, map);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("trigger", trigger)
                .add("predicates", predicates)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerConditions that = (TriggerConditions) o;
        return Objects.equal(getTrigger(), that.getTrigger()) && Objects.equal(getPredicates(), that.getPredicates());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTrigger(), getPredicates());
    }
}
