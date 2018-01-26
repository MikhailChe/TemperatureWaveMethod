package model.signalID;

import java.io.File;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.HashCoder;
import controller.lambda.Predicates;
import model.phaseAdjust.PhaseAdjust;
import model.phaseAdjust.PhaseAdjustFactory;

/**
 * Класс идентификатора базового сигнала
 * 
 * @author Mikey
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BaseSignalID extends SignalIdentifier {
	@XmlElement
	public PhaseAdjust zc;
	
	@XmlElement
	public boolean inverse = false;
	
	
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
		zc = PhaseAdjustFactory.forFile(file);
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
