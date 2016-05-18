package ru.dolika.experiment.measurement;

import java.io.Serializable;

import ru.dolika.experiment.Analyzer.SignalParameters;
import ru.dolika.experiment.signalID.BaseSignalID;
import ru.dolika.experiment.signalID.SignalIdentifier;

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

	public double frequency;
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

	public SignalParameters initSignalParams;

	public TemperatureConductivity() {
		signalID = null;
	}

	@Override
	public String toString() {

		return String.format("%.0f;%.3f;%.3f;%.3f;%.3f;%.4e", amplitude,
				initSignalParams == null ? 0 : Math.toDegrees(initSignalParams.phase),
				signalID == null ? 0 : signalID.zc.getCurrentShift(frequency), Math.toDegrees(phase), kappa, tCond);
	}

	public String getHeader() {
		return "Амплитуда;Начальная фаза;Нулеваая фаза;Скорректированная фаза;Каппа;Температуропроводность";
	}

}
