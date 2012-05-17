package org.dllearner.algorithm.tbsl.exploration.exploration_main;

public class Setting {

	private static boolean waitModus;
	private static boolean debugModus;
	private static boolean newIndex;
	private static double LevenstheinMin;
	private static int anzahlAbgeschickterQueries;
	private static double thresholdSelect;
	private static double thresholdAsk;
	private static int moduleStep;
	private static double EsaMin;
	private static boolean loadedProperties;
	private static int version;
	private static boolean saveAnsweredQueries;
	
	
	
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
	public static double getLevenstheinMin() {
		return LevenstheinMin;
	}
	public static void setLevenstheinMin(double levenstheinMin) {
		LevenstheinMin = levenstheinMin;
	}
	public static int getAnzahlAbgeschickterQueries() {
		return anzahlAbgeschickterQueries;
	}
	public static void setAnzahlAbgeschickterQueries(
			int anzahlAbgeschickterQueries) {
		Setting.anzahlAbgeschickterQueries = anzahlAbgeschickterQueries;
	}
	public static double getThresholdSelect() {
		return thresholdSelect;
	}
	public static void setThresholdSelect(double thresholdSelect) {
		Setting.thresholdSelect = thresholdSelect;
	}
	public static double getThresholdAsk() {
		return thresholdAsk;
	}
	public static void setThresholdAsk(double thresholdAsk) {
		Setting.thresholdAsk = thresholdAsk;
	}
	public static int getModuleStep() {
		return moduleStep;
	}
	public static void setModuleStep(int moduleStep) {
		Setting.moduleStep = moduleStep;
	}
	public static double getEsaMin() {
		return EsaMin;
	}
	public static void setEsaMin(double esaMin) {
		EsaMin = esaMin;
	}
	public static boolean isLoadedProperties() {
		return loadedProperties;
	}
	public static void setLoadedProperties(boolean loadedProperties) {
		Setting.loadedProperties = loadedProperties;
	}
	public static int getVersion() {
		return version;
	}
	public static void setVersion(int version) {
		Setting.version = version;
	}
	public static boolean isSaveAnsweredQueries() {
		return saveAnsweredQueries;
	}
	public static void setSaveAnsweredQueries(boolean saveAnsweredQueries) {
		Setting.saveAnsweredQueries = saveAnsweredQueries;
	}

	
}
