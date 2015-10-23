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

	double zeroTemperature = 273 + 22;

	double gain = 42;
	final double ADCgain = 32767.5;
	final double ADCmaxVoltage = 10;

	public double getVoltage(double ADCvalue) {
		return ((ADCvalue / ADCgain) * ADCmaxVoltage) / gain;
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
