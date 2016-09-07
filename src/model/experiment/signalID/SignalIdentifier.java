package model.experiment.signalID;

import java.io.Serializable;

public class SignalIdentifier implements Serializable {
	private static final long serialVersionUID = 3649178817602291964L;

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o instanceof SignalIdentifier) return true;
		return false;
	}
}
