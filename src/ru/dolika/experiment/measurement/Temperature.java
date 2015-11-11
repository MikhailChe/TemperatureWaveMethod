package ru.dolika.experiment.measurement;

import java.io.Serializable;

import ru.dolika.experiment.signalID.DCsignalID;
import ru.dolika.experiment.signalID.SignalIdentifier;

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
	private static final long serialVersionUID = 4120156102719235528L;
	/**
	 * Значение температуры
	 */
	public double value;
	public double signalLevel;
	/**
	 * Идентификатор канала данных, с помощью которых была вычислена эта
	 * температура
	 * 
	 * @see DCsignalID
	 * @see SignalIdentifier
	 * 
	 */
	public DCsignalID signalID;

	public Temperature() {
		value = 0;
		signalID = null;
	}

	@Override
	public String toString() {
		return String.format("%.8f", value);
	}
}
