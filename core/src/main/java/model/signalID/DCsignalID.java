package model.signalID;

import java.util.Arrays;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;
import controller.lambda.Utils;
import model.thermocouple.graduate.Graduate;

@XmlAccessorType(XmlAccessType.NONE)
public class DCsignalID extends SignalIdentifier {

	@XmlElement
	private Graduate graduate;

	public DCsignalID() {
		// graduate = GraduateFactory.forFile(new
		// File("config/grad/VR-5-20-list.txt"));
	}

	public DCsignalID(Graduate grad) {
		if (grad == null)
			throw new NullPointerException();
		graduate = grad;
	}

	@XmlElement
	double zeroTemperature = 273 + 18;

	// gain for 1100 = 682
	// gain for 0100 = 271

	@XmlElement
	double amplifierGain = 270;

	private static final double ADC_MAX_VOLTAGE_CODE = 32767.5;
	private static final double ADC_MAX_VOLTAGE = 10;

	@XmlElement
	private double adcMaxVoltageCode = ADC_MAX_VOLTAGE_CODE;

	@XmlElement
	private double adcMaxVoltage = ADC_MAX_VOLTAGE;

	public double getVoltage(double ADCvalue) {
		return ((ADCvalue / ADC_MAX_VOLTAGE_CODE) * ADC_MAX_VOLTAGE) / amplifierGain;
	}

	public double getTemperature(double voltage) {
		if (graduate == null)
			throw new NullPointerException("Cannot convert voltage to temperature. No graduate information found.");
		return graduate.getTemperature(voltage, zeroTemperature);
	}

	public void setGain(double gain) {
		if (gain != 0)
			this.amplifierGain = gain;
	}

	public Graduate getGraduate() {
		return graduate;
	}

	public Graduate setGraduate(Graduate newgrad) {
		this.graduate = newgrad;
		return this.graduate;
	}

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(DCsignalID.class, this, o, Arrays.asList(DCsignalID::getGraduate,
				a -> a.amplifierGain, a -> a.zeroTemperature));
	}

	@Override
	public int hashCode() {
		return Objects.hash(graduate, amplifierGain, zeroTemperature);
	}

	@Override
	public String toString() {
		return Utils.stringOfObject(ADC_MAX_VOLTAGE, ADC_MAX_VOLTAGE_CODE, amplifierGain, zeroTemperature, graduate);
	}
}
