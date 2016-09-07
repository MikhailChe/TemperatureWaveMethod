package experiment.Analyzer;

import java.io.Serializable;

public class SignalParameters implements Serializable, Cloneable {

	private static final long serialVersionUID = 4734481193938264245L;
	final public double phase;
	final public double amplitude;
	final public double nullOffset;

	public SignalParameters() {
		this(0);
	}

	public SignalParameters(double phase) {
		this(phase, 0);
	}

	public SignalParameters(double phase, double amplitude) {
		this(phase, amplitude, 0);
	}

	public SignalParameters(double phase, double amplitude, double nullOffset) {
		this.phase = phase;
		this.amplitude = amplitude;
		this.nullOffset = nullOffset;
	}

	@Override
	public SignalParameters clone() {
		return new SignalParameters(phase, amplitude, nullOffset);
	}
}
