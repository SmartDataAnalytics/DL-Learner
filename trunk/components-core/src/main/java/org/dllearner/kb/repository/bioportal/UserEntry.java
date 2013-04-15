package org.dllearner.kb.repository.bioportal;

public class UserEntry {
    private String userId;
    private String isOwner;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserAclEntry [isOwner=" + isOwner + ", userId=" + userId + "]";
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
