package org.dllearner.tools.protege;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

public class DLLearnerPreferences {
	private static DLLearnerPreferences instance;

    private static final String KEY = "org.dllearner";

    private static final String TRACKING = "CheckConsistencyWhileLearning";


    public static synchronized DLLearnerPreferences getInstance() {
        if(instance == null) {
            instance = new DLLearnerPreferences();
        }
        return instance;
    }

    private Preferences getPrefs() {
        return PreferencesManager.getInstance().getApplicationPreferences(KEY);
    }


    public boolean isCheckConsistencyWhileLearning() {
        return getPrefs().getBoolean(TRACKING, true);
    }


    public void setCheckConsistencyWhileLearning(boolean checkConsistencyWhileLearning) {
        getPrefs().putBoolean(TRACKING, checkConsistencyWhileLearning);
    }

}
