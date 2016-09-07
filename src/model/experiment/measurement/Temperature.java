package model.experiment.measurement;

import java.io.IOException;
import java.io.Serializable;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;

import model.experiment.signalID.DCsignalID;
import model.experiment.signalID.SignalIdentifier;

/**
 * Объет температуры. Хранит в себе данные о температуре образца, а также
 * информацию об объекте (идентификаторе сигнала) с помощью которого эта
 * температура была получена
 * 
 * @author Mike
 *
 */

public class Temperature implements Serializable {

	/**
	* 
	*/
	private static final long	serialVersionUID	= 4120156102719235528L;
	/**
	 * Значение температуры
	 */

	public double				value;

	public double				signalLevel;
	/**
	 * Идентификатор канала данных, с помощью которых была вычислена эта
	 * температура
	 * 
	 * @see DCsignalID
	 * @see SignalIdentifier
	 * 
	 */
	@XmlElement
	public DCsignalID			signalID;

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

	public String getHeader() {
		return "Температура";
	}

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		JAXB.marshal(this, out);
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		JAXB.unmarshal(in, this.getClass());
	}

}
