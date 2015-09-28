package ru.dolika.experiment.workspace;

import java.io.File;
import java.util.ArrayList;

import ru.dolika.experiment.sample.Sample;
import ru.dolika.experimentAnalyzer.signalID.SignalIdentifier;

public class Workspace {

	private static Workspace instance = null;

	public synchronized static Workspace getInstance() {
		if (instance == null) {
			instance = new Workspace();
		}
		return instance;
	}

	public Sample s = null;
	public File samplefile = null;
	public ArrayList<SignalIdentifier> signalIDs = null;

	private Workspace() {

	}
}
