package ru.dolika.experiment.measurement;

import java.io.Serializable;

import ru.dolika.experimentAnalyzer.signalID.DCsignalID;

public class Temperature implements Serializable {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4120156102719235528L;
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
