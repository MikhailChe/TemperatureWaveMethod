package model.experiment.signalID;

public class AdjustmentSignalID extends SignalIdentifier {
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o instanceof AdjustmentSignalID) {
			return true;
		}
		return false;
	}
}
