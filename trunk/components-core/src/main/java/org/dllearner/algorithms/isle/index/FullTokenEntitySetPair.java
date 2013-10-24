package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 * A pair consisting of a full string token and the corresponding entities
 */
public class FullTokenEntitySetPair {
    private String fullToken;
    private Set<Entity> entitySet;

    public FullTokenEntitySetPair(String fullToken) {
        this.fullToken = fullToken;
        this.entitySet = new HashSet<Entity>();
    }

    public String getFullToken() {
        return fullToken;
    }

    public Set<Entity> getEntitySet() {
        return entitySet;
    }

    public void addEntity(Entity entity) {
        entitySet.add(entity);
    }
}
