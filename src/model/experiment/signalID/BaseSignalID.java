package model.experiment.signalID;

import java.io.File;

import javax.xml.bind.annotation.XmlElement;

import model.experiment.zeroCrossing.ZeroCrossing;
import model.experiment.zeroCrossing.ZeroCrossingFactory;

/**
 * Класс идентификатора базового сигнала
 * 
 * @author Mikey
 *
 */
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
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof BaseSignalID))
			return false;
		if (this.zc != null)
			if (this.zc.equals(((BaseSignalID) o).zc))
				return true;
		return false;
	}

}
