package org.dllearner.utilities;

import java.util.Collection;

public interface HasProgressMonitor<P extends ProgressMonitor> {
    /**
     * @return the current registered progress monitors
     */
    Collection<P> progressMonitors();

    /**
     * Register the given progress monitor.
     * @param mon the progress monitor
     * @return <code>true</code> (as specified by {@link java.util.Collection#add(Object)})
     */
    default boolean addProgressMonitor(P mon) {
        return progressMonitors().add(mon);
    }

    /**
     * Unregister the given progress monitor.
     * @param mon the progress monitor
     * @return <code>true</code> (as specified by {@link java.util.Collection#remove(Object)})
     */
    default boolean removeProgressMonitor(P mon) {
        return progressMonitors().remove(mon);
    }
}