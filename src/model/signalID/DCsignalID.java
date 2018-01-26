package model.signalID;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.HashCoder;
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
	double			zeroTemperature		= 273 + 22;

	// gain for 1100 = 682
	// gain for 0100 = 271

	@XmlElement
	double			amplifierGain		= 270;

	@XmlElement
	final double	adcMaxVoltageCode	= 32767.5;

	@XmlElement
	final double	adcMaxVoltage		= 10;

	public double getVoltage(double ADCvalue) {
		return ((ADCvalue / adcMaxVoltageCode)
		        * adcMaxVoltage)
		        / amplifierGain;
	}

	public double getTemperature(double voltage) {
		if (graduate == null)
		    throw new NullPointerException(
		            "Cannot convert voltage to temperature. No graduate information found.");
		return graduate.getTemperature(voltage,
		        zeroTemperature);
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
		return Predicates.areEqual(DCsignalID.class, this,
		        o, Arrays.asList(DCsignalID::getGraduate,
		                a -> a.amplifierGain,
		                a -> a.zeroTemperature,
		                a -> a.adcMaxVoltageCode,
		                a -> a.adcMaxVoltage));
	}

	@Override
	public int hashCode() {
		return HashCoder.hashCode(graduate, amplifierGain,
		        zeroTemperature);
	}

	@Override
	public String toString() {
		return Utils.stringOfObject(adcMaxVoltage,
		        adcMaxVoltageCode, amplifierGain,
		        zeroTemperature, graduate);
	}
}
