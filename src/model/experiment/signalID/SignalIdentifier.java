package model.experiment.signalID;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso(value = { DCsignalID.class, BaseSignalID.class,
		AdjustmentSignalID.class })
public class SignalIdentifier {

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof SignalIdentifier)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
