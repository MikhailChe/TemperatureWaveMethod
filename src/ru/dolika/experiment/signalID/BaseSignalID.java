package ru.dolika.experiment.signalID;

import java.io.File;

import ru.dolika.experiment.zeroCrossing.ZeroCrossing;
import ru.dolika.experiment.zeroCrossing.ZeroCrossingFactory;

/**
 * Класс идентификатора базового сигнала
 * 
 * @author Mikey
 *
 */
public class BaseSignalID extends SignalIdentifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5580000396824926562L;
	public ZeroCrossing zc;

	public BaseSignalID() {
		super();
	}

	/**
	 * Конструктор для идентфикатора канала базового сигнала.
	 * 
	 * @param file
	 *            файл с юстировкой
	 */
	public BaseSignalID(File file) {
		this();
		zc = ZeroCrossingFactory.forFile(file);
	}

}
