package ru.dolika.experimentAnalyzer.signalID;

import java.io.File;

import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossing;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossingFactory;

/**
 * Класс идентификатора базового сигнала
 * 
 * @author Mikey
 *
 */
public class BaseSignalID extends SignalIdentifier {

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
