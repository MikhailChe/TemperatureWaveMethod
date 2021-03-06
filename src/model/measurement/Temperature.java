package model.measurement;

import java.util.Arrays;
import java.util.Objects;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;
import model.signalID.DCsignalID;
import model.signalID.SignalIdentifier;

/**
 * Объет температуры. Хранит в себе данные о температуре образца, а также
 * информацию об объекте (идентификаторе сигнала) с помощью которого эта
 * температура была получена
 * 
 * @author Mike
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Temperature {

	/**
	 * Значение температуры
	 */
	@XmlAttribute(name = "kelvins")
	public double value;
	@XmlElement(name = "voltage")
	public double signalLevel;
	/**
	 * Идентификатор канала данных, с помощью которых была вычислена эта температура
	 * 
	 * @see DCsignalID
	 * @see SignalIdentifier
	 * 
	 */
	@XmlElement
	public DCsignalID signalID;

	public Temperature() {
		value = 0;
		signalID = null;
	}

	public Temperature setValue(double value) {
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		return String.format("%.8f", value);
	}

	public static String getHeader() {
		return "Т (К)";
	}

	private void writeObject(java.io.ObjectOutputStream out) {
		JAXB.marshal(this, out);
	}

	private void readObject(java.io.ObjectInputStream in) {
		JAXB.unmarshal(in, this.getClass());
	}

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(Temperature.class, this, o,
				Arrays.asList(a -> a.signalID, a -> a.signalLevel, a -> a.value));
	}

	@Override
	public int hashCode() {
		return Objects.hash(signalID, signalLevel, value);
	}

}
