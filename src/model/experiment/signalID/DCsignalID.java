package model.experiment.signalID;

import model.thermocouple.graduate.Graduate;

public class DCsignalID extends SignalIdentifier {
	private static final long serialVersionUID = 8360006961756985177L;

	Graduate graduate;

	public DCsignalID() {
		// graduate = GraduateFactory.forFile(new
		// File("config/grad/VR-5-20-list.txt"));
	}

	public DCsignalID(Graduate grad) {
		if (grad == null)
			throw new NullPointerException();
		graduate = grad;
	}

	double zeroTemperature = 273 + 22;

	// gain for 1100 = 682
	// gain for 0100 = 271
	double gain = 270; // /2.51
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

	public Graduate getGraduate() {
		return graduate;
	}

	public Graduate setGraduate(Graduate newgrad) {
		this.graduate = newgrad;
		return this.graduate;
	}
}
