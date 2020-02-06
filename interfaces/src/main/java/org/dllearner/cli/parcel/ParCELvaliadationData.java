package org.dllearner.cli.parcel;

import org.dllearner.utilities.statistics.Stat;

public class ParCELvaliadationData {
	// statistical values
	Stat runtime = new Stat();
	Stat accuracy = new Stat();
	Stat length = new Stat();
	Stat accuracyTraining = new Stat();
	Stat fMeasure = new Stat();
	Stat fMeasureTraining = new Stat();
	
	Stat trainingCompletenessStat = new Stat();
	Stat trainingCorrectnessStat = new Stat();

	Stat testingCompletenessStat = new Stat();
	Stat testingCorrectnessStat = new Stat();
	
	Stat noOfPartialDef = new Stat();
	Stat partialDefinitionLength = new Stat();
}
