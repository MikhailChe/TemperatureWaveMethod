package ru.dolika.experiment.Analyzer;

public class SignalParameters {
	public Double phase = null;
	public Double amplitude = null;
	public Double nullOffset = null;

	public SignalParameters() {

	}

	public SignalParameters(double phase) {
		this();
		this.phase = phase;
	}

	public SignalParameters(double phase, double amplitude) {
		this(phase);
		this.amplitude = amplitude;
	}

	public SignalParameters(double phase, double amplitude, double nullOffset) {
		this(phase, amplitude);
		this.nullOffset = nullOffset;
	}
}
