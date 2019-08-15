package org.dllearner.utilities;

/**
 * @author Lorenz Buehmann
 */
public class SilentProgressMonitor implements ProgressMonitor {
    @Override
    public void startedProcess(String message) {}
    @Override
    public void finishedProcess(String message) {}
    @Override
    public void updateProgress(int done, int total, String message) {}
}
