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
	public PhaseAdjust phaseAdjust;

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
		phaseAdjust = PhaseAdjustFactory.forFile(file);
	}

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(BaseSignalID.class, this, o, Arrays.asList(a -> a.phaseAdjust));
	}

	@Override
	public int hashCode() {
		return HashCoder.hashCode(phaseAdjust);
	}

	@Override
	public String toString() {
		return "Base signal";
	}
}
