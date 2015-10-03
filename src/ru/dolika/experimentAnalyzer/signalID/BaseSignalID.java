package ru.dolika.experimentAnalyzer.signalID;

import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossing;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossingFactory;

public class BaseSignalID extends SignalIdentifier {

	public ZeroCrossing zc;

	public BaseSignalID() {

	}

	public BaseSignalID(String filename, ZeroCrossing z) {
		zc = ZeroCrossingFactory.forFile(filename);
	}

}
