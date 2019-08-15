package org.dllearner.utilities;

public interface ProgressMonitor {

    void startedProcess(String message);

    void finishedProcess(String message);

    void updateProgress(int done, int total, String message);

}

