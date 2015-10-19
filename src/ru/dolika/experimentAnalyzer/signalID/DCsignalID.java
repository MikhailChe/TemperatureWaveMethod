package ru.dolika.experimentAnalyzer.signalID;

import ru.dolika.thermocouple.graduate.Graduate;
import ru.dolika.thermocouple.graduate.GraduateFactory;

public class DCsignalID extends SignalIdentifier {

	Graduate graduate;

	public DCsignalID() {
		graduate = GraduateFactory.forFile("config/grad/VR5-20.txt");
	}

	public DCsignalID(Graduate grad) {
		if (grad == null)
			throw new NullPointerException();
		graduate = grad;
	}

	double zeroTemperature = 20;

	double gain = 1;
	final double ADCgain = 6553.5;

	public double getVoltage(double ADCvalue) {
		return (ADCvalue / ADCgain) / gain;
	}

	public double getTemperature(double voltage) {
		if (graduate == null)
			return -1;
		return graduate.getTemperature(voltage, zeroTemperature);
	}

	public void setGain(double gain) {
		if (gain != 0) {
			this.gain = gain;
		}
	}

	public static void main(String... args) {
		DCsignalID sid = new DCsignalID();
		System.out.println(sid.getVoltage(32766));
		System.out.println(sid.getTemperature(sid.getVoltage(32766)));
	}
}
