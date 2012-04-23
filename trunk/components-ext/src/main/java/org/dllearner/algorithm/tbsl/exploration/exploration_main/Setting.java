package org.dllearner.algorithm.tbsl.exploration.exploration_main;

public class Setting {

	private static boolean waitModus;
	private static boolean debugModus;
	private static boolean newIndex;
	
	public static boolean isWaitModus() {
		return waitModus;
	}
	public static void setWaitModus(boolean waitModus) {
		Setting.waitModus = waitModus;
	}
	public static boolean isDebugModus() {
		return debugModus;
	}
	public static void setDebugModus(boolean debugModus) {
		Setting.debugModus = debugModus;
	}
	public static boolean isNewIndex() {
		return newIndex;
	}
	public static void setNewIndex(boolean newIndex) {
		Setting.newIndex = newIndex;
	}

	
}
