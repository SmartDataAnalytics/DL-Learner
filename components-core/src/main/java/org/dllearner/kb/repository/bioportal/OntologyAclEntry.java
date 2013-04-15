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
