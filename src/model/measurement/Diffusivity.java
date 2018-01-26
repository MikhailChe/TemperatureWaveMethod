package model.measurement;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;
import model.analyzer.SignalParameters;
import model.signalID.BaseSignalID;
import model.signalID.SignalIdentifier;

/**
 * Класс, хранящий в себе значение температуропроводности
 * 
 * @author Mike
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Diffusivity {
    /**
     * Объект класса, идентифицирующего канал, на котором проводились измерения
     * 
     * @see BaseSignalID
     * @see SignalIdentifier
     */
    @XmlElement
    public BaseSignalID signalID;

    @XmlElement
    public int channelNumber;

    /**
     * Фаза сигнала
     */
    @XmlElement
    public double phase;

    @XmlAttribute
    public double frequency;
    /**
     * Амплитуда сигнала
     */
    @XmlElement
    public double amplitude;
    /**
     * коэффициент каппа
     */
    @XmlElement
    public double kappa;
    /**
     * Значение коэффициента температуропроводности
     */
    @XmlElement
    public double diffusivity;

    @XmlElement
    public double capacitance;

    @XmlElement
    public SignalParameters initSignalParams;

    public Diffusivity() {
	signalID = null;
    }

    public double gettCond() {
	return diffusivity;
    }

    public Diffusivity settCond(double tCond) {
	this.diffusivity = tCond;
	return this;
    }

    @Override
    public String toString() {

	return String.format("%.0f;%.3f;%.3f;%.3f;%.3f;%.4e;%.3f", amplitude,
		initSignalParams == null ? 0 : Math.toDegrees(initSignalParams.phase),
		signalID == null ? 0 : signalID.phaseAdjust.getCurrentShift(frequency), Math.toDegrees(phase), kappa,
		diffusivity, capacitance);
    }

    public String getHeader() {
	return String.format("Амплитуда;φнач;φюст;φ;κ;α(%d);Сp(отн)", channelNumber);
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (o == this)
	    return true;
	if (!(o instanceof Diffusivity))
	    return false;
	Predicate<Function<Diffusivity, Object>> eq = Predicates.equalizer(this, (Diffusivity) o);
	return eq.test(a -> a.amplitude) && eq.test(a -> a.diffusivity) && eq.test(a -> a.frequency)
		&& eq.test(a -> a.initSignalParams) && eq.test(a -> a.kappa) && eq.test(a -> a.phase)
		&& eq.test(a -> a.signalID) && eq.test(a -> a.capacitance);
    }

    @Override
    public int hashCode() {
	return Double.hashCode(amplitude) + Double.hashCode(diffusivity) + Double.hashCode(frequency)
		+ initSignalParams.hashCode() + Double.hashCode(kappa) + Double.hashCode(phase) + signalID.hashCode();
    }
}
