package ru.dolika.experiment.measurement;

import ru.dolika.experimentAnalyzer.signalID.DCsignalID;

public class Temperature {

	public double value;
	public DCsignalID signalID;

	public Temperature() {
		value = 0;
		signalID = null;
	}

	@Override
	public String toString() {
		return String.format("%.1f", value);
	}
}
