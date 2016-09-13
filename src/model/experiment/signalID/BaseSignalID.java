package model.experiment.signalID;

import java.io.File;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.HashCoder;
import controller.lambda.Predicates;
import model.experiment.zeroCrossing.ZeroCrossing;
import model.experiment.zeroCrossing.ZeroCrossingFactory;

/**
 * Класс идентификатора базового сигнала
 * 
 * @author Mikey
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BaseSignalID extends SignalIdentifier {
	@XmlElement
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

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(BaseSignalID.class, this,
		        o,
		        Arrays.asList(a -> a.zc));
	}

	@Override
	public int hashCode() {
		return HashCoder.hashCode(zc);
	}

	@Override
	public String toString() {
		return "Base signal";
	}
}
