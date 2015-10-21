package ru.dolika.experiment.measurement;

import java.io.Serializable;

import ru.dolika.experimentAnalyzer.signalID.BaseSignalID;
import ru.dolika.experimentAnalyzer.signalID.SignalIdentifier;

/**
 * Класс, хранящий в себе значение температуропроводности
 * 
 * @author Mike
 *
 */
public class TemperatureConductivity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6849674773117070919L;
	/**
	 * Объект класса, идентифицирующего канал, на котором проводились измерения
	 * 
	 * @see BaseSignalID
	 * @see SignalIdentifier
	 */
	public BaseSignalID signalID;
	/**
	 * Фаза сигнала
	 */
	public double phase;
	/**
	 * Амплитуда сигнала
	 */
	public double amplitude;
	/**
	 * коэффициент каппа
	 */
	public double kappa;
	/**
	 * Значение коэффициента температуропроводности
	 */
	public double tCond;

	public TemperatureConductivity() {
		signalID = null;
	}

	@Override
	public String toString() {

		return String.format("%.0f\t%.3f\t%.4e", amplitude, kappa, tCond);
	}

}
