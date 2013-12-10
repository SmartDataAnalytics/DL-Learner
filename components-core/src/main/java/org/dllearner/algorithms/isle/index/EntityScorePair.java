package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

/**
 * Represents a scored entity. The score is produced from the path used to retrieve it from the candidates tree.
 * @author Daniel Fleischhacker
 */
public class EntityScorePair implements Comparable<EntityScorePair> {
    @Override
    public String toString() {
        return entity + " : " + score;
    }

    private Entity entity;
    private Double score;

    @Override
    public int compareTo(EntityScorePair o) {
        int val = score.compareTo(o.score);

        if (val == 0) {
            val = entity.getURI().toString().compareTo(o.entity.getURI().toString());
        }

        return val;
    }

    public EntityScorePair(Entity entity, Double score) {
        this.entity = entity;
        this.score = score;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityScorePair that = (EntityScorePair) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null) {
            return false;
        }
        if (score != null ? !score.equals(that.score) : that.score != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (score != null ? score.hashCode() : 0);
        return result;
    }
}
