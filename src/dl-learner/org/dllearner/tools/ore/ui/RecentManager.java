package org.dllearner.tools.ore.ui;


public class RecentManager {
	private static RecentManager instance;

	public static synchronized RecentManager getInstance() {
		if (instance == null) {
			instance = new RecentManager();
		}
		return instance;
	}
	
	
}
