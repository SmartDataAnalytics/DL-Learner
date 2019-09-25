/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 * <p>
 * This file is part of DL-Learner.
 * <p>
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.sparql;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.jena.rdf.model.Model;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachingConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator {

    private ConciseBoundedDescriptionGenerator delegatee;

    private static class CacheKey {
        private final String resource;
        private final int depth;
        private final boolean withTypesForLeafs;

        CacheKey(String resource, int depth, boolean withTypesForLeafs) {
            this.resource = resource;
            this.depth = depth;
            this.withTypesForLeafs = withTypesForLeafs;
        }

        static CacheKey of(String resource, int depth, boolean withTypesForLeafs) {
            return new CacheKey(resource, depth, withTypesForLeafs);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return depth == cacheKey.depth &&
                    withTypesForLeafs == cacheKey.withTypesForLeafs &&
                    Objects.equals(resource, cacheKey.resource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, depth, withTypesForLeafs);
        }
    }

    private LoadingCache<CacheKey, Model> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<CacheKey, Model>() {
                        public Model load(CacheKey key) {
                            return delegatee.getConciseBoundedDescription(key.resource, key.depth, key.withTypesForLeafs);
                        }
                    });

    public CachingConciseBoundedDescriptionGenerator(ConciseBoundedDescriptionGenerator cbdGen) {
        this.delegatee = cbdGen;
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
     */
    @Override
    public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
        try {
            return cache.get(CacheKey.of(resource, depth, withTypesForLeafs));
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to computed cached CBD", e);
        }
    }

    @Override
    public void setAllowedPropertyNamespaces(Set<String> namespaces) {
        delegatee.setAllowedPropertyNamespaces(namespaces);
    }

    /* (non-Javadoc)
     * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setAllowedObjectNamespaces(java.util.Set)
     */
    @Override
    public void setAllowedObjectNamespaces(Set<String> namespaces) {
        delegatee.setAllowedObjectNamespaces(namespaces);
    }

    @Override
    public void setAllowedClassNamespaces(Set<String> namespaces) {
        delegatee.setAllowedClassNamespaces(namespaces);
    }

    @Override
    public void setIgnoredProperties(Set<String> properties) {
        delegatee.setIgnoredProperties(properties);
    }
}
