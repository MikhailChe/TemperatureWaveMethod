package model.analyzer;

import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;

@XmlAccessorType(XmlAccessType.NONE)
public class SignalParameters implements Cloneable {

    @XmlElement
    private double phase;
    @XmlElement
    private double amplitude;
    @XmlElement
    private double nullOffset;
    @XmlElement
    private double frequency;

    public SignalParameters() {
	this(0);
    }

    public SignalParameters(double phase) {
	this(phase, 0);
    }

    public SignalParameters(double phase, double amplitude) {
	this(phase, amplitude, 0, 0);
    }

    public SignalParameters(double phase, double amplitude, double nullOffset) {
	this(phase, amplitude, nullOffset, 0);
    }

    public SignalParameters(double phase, double amplitude, double nullOffset, double frequency) {
	this.phase = phase;
	this.amplitude = amplitude;
	this.nullOffset = nullOffset;
	this.frequency = frequency;
    }

    public double getPhase() {
	return phase;
    }

    public double getAmplitude() {
	return amplitude;
    }

    public double getNullOffset() {
	return nullOffset;
    }

    public double getFrequency() {
	return frequency;
    }

    @Override
    public SignalParameters clone() {
	return new SignalParameters(phase, amplitude, nullOffset);
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (o == this)
	    return true;
	if (!(o instanceof SignalParameters))
	    return false;
	Predicate<Function<SignalParameters, Object>> eq = Predicates.equalizer(this, (SignalParameters) o);
	return eq.test(a -> a.amplitude) && eq.test(a -> a.nullOffset) && eq.test(a -> a.phase);
    }

    @Override
    public int hashCode() {
	return Double.hashCode(amplitude) + Double.hashCode(nullOffset) + Double.hashCode(phase);
    }
}
