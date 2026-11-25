package model.signalID;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class AdjustmentSignalID extends SignalIdentifier {
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o instanceof AdjustmentSignalID) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public String toString() {
		return "Adjustment channel";
	}

}