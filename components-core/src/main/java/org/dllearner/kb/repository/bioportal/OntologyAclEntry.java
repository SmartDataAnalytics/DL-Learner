/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.repository.bioportal;

public class OntologyAclEntry {
    private String ontologyId;
    private String isOwner;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OntologyAclEntry [isOwner=" + isOwner + ", ontologyId="
                + ontologyId + "]";
    }

    /**
     * @return the ontologyId
     */
    public String getOntologyId() {
        return ontologyId;
    }

    /**
     * @param ontologyId
     *            the ontologyId to set
     */
    public void setOntologyId(String ontologyId) {
        this.ontologyId = ontologyId;
    }

    /**
     * @return the isOwner
     */
    public String getIsOwner() {
        return isOwner;
    }

    /**
     * @param isOwner
     *            the isOwner to set
     */
    public void setIsOwner(String isOwner) {
        this.isOwner = isOwner;
    }
}
