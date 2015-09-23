package ru.dolika.experiment.measurement;

import ru.dolika.experimentAnalyzer.signalID.BaseSignalID;

public class TemperatureConductivity {

	public BaseSignalID signalID;
	public double phase;
	public double amplitude;
	public double kappa;
	public double tCond;

	public TemperatureConductivity() {
		signalID = null;
	}

	@Override
	public String toString() {

		return String.format("%.0f\t%.3f\t%.4e", amplitude, kappa, tCond);
	}

}
