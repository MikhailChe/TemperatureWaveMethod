package model.experiment.Analyzer;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;

@XmlAccessorType(XmlAccessType.NONE)
public class SignalParameters implements Cloneable {

	@XmlElement
	final public double	phase;
	@XmlElement
	final public double	amplitude;
	@XmlElement
	final public double	nullOffset;

	public SignalParameters() {
		this(0);
	}

	public SignalParameters(double phase) {
		this(phase, 0);
	}

	public SignalParameters(double phase,
	        double amplitude) {
		this(phase, amplitude, 0);
	}

	public SignalParameters(double phase, double amplitude,
	        double nullOffset) {
		this.phase = phase;
		this.amplitude = amplitude;
		this.nullOffset = nullOffset;
	}

	@Override
	public SignalParameters clone() {
		return new SignalParameters(phase, amplitude,
		        nullOffset);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof SignalParameters)) return false;
		Predicate<Function<SignalParameters, Object>> eq = Predicates
		        .equalizer(this, (SignalParameters) o);
		return eq.test(a -> a.amplitude)
		        && eq.test(a -> a.nullOffset)
		        && eq.test(a -> a.phase);
	}

	@Override
	public int hashCode() {
		return Double.hashCode(amplitude)
		        + Double.hashCode(nullOffset)
		        + Double.hashCode(phase);
	}
}
