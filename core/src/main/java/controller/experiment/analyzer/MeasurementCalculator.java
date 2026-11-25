package controller.experiment.analyzer;

import static controller.experiment.analyzer.PhaseUtils.truncateNegative;
import static controller.experiment.analyzer.PhaseUtils.truncatePositive;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.experiment.reader.ExperimentFileReader;
import model.analyzer.SignalParameters;
import model.measurement.Diffusivity;
import model.measurement.Measurement;
import model.measurement.Temperature;
import model.phaseAdjust.PhaseAdjust;
import model.signalID.AdjustmentSignalID;
import model.signalID.BaseSignalID;
import model.signalID.DCsignalID;
import model.signalID.SignalIdentifier;

public final class MeasurementCalculator {
	public record SampleProperties(double lengthMeters, double density) {}

	private final SampleProperties sample;
	private final List<SignalIdentifier> signalIDs;

	public MeasurementCalculator(SampleProperties sample, List<SignalIdentifier> signalIDs) {
		this.sample = sample;
		this.signalIDs = unmodifiableList(new ArrayList<>(signalIDs));
	}

	public Measurement calculate(Path file, long measuredAtMillis) throws IOException {
		return calculate(new ExperimentFileReader(file, measuredAtMillis));
	}

	public Measurement calculate(ExperimentFileReader reader) {
		Measurement result = new Measurement();
		result.time = reader.getTime();
		int columnCount = reader.getColumnCount();
		if (columnCount <= 1) {
			return result;
		}

		result.frequency = reader.getExperimentFrequency();
		double[][] signals = reader.getCroppedData();
		int frequencyIndex = reader.getCroppedDataPeriodsCount();
		for (int channel = 1; channel < Math.min(columnCount, signalIDs.size()); channel++) {
			SignalIdentifier id = signalIDs.get(channel);
			if (id == null) {
				continue;
			}
			SignalParameters signal = getSignalParameters(signals[channel], frequencyIndex, result.frequency);
			switch (id) {
				case BaseSignalID base -> addDiffusivity(channel, signal, base, result);
				case DCsignalID dc -> addTemperature(signal, dc, result);
				case AdjustmentSignalID adjustment -> addPhaseMeasurement(channel, signal, result);
				default -> {}
			}
		}
		return result;
	}

	public static SignalParameters getSignalParameters(double[] signal, int frequencyIndex, double frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, frequencyIndex);

		double phase = FFT.getArgument(fourierForFreq, 0) + Math.PI / 2.0;

		double amplitude = 2.0 * FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffset = Arrays.stream(signal).parallel().average().orElse(Double.NaN);

		SignalParameters params = new SignalParameters(phase, amplitude, nullOffset, frequency);

		return params;
	}

	private void addDiffusivity(int channel, SignalParameters signal, BaseSignalID id, Measurement result) {
		PhaseAdjust adjustment = id.phaseAdjust;
		if (adjustment == null) {
			return;
		}
		if (sample == null) {
			throw new IllegalStateException("Sample properties are required for diffusivity calculation");
		}
		double shift = adjustment.getCurrentShift(signal.getFrequency());
		double shiftDegrees = toDegrees(truncateNegative(toRadians(id.inverse ? shift + 180 : shift)));
		Diffusivity diffusivity = getPhysicalProperties(signal, shiftDegrees);
		diffusivity.signalID = id;
		diffusivity.channelNumber = channel;
		result.diffusivity.add(diffusivity);
	}

	private static void addTemperature(SignalParameters signal, DCsignalID id, Measurement result) {
		Temperature temperature = new Temperature();
		temperature.signalLevel = id.getVoltage(signal.getNullOffset());
		temperature.value = id.getTemperature(temperature.signalLevel * 1000.0);
		result.temperature.add(temperature);
	}

	private static void addPhaseMeasurement(int channel, SignalParameters signal, Measurement result) {
		Diffusivity phaseMeasurement = new Diffusivity();
		phaseMeasurement.amplitude = signal.getAmplitude();
		phaseMeasurement.phase = -signal.getPhase();
		phaseMeasurement.frequency = signal.getFrequency();
		phaseMeasurement.channelNumber = channel;
		result.diffusivity.add(phaseMeasurement);
	}

	private Diffusivity getPhysicalProperties(SignalParameters signal, double shiftDegrees) {

		final double signalAngle = truncateNegative(signal.getPhase());

		double targetAngle = -signalAngle;

		double adjustedAngle = truncatePositive(targetAngle - Math.toRadians(shiftDegrees));

		final double preKappaAngle = truncatePositive(adjustedAngle - Math.PI / 4.0);

		final double kappa = Math.sqrt(2) * (preKappaAngle);



		final double omega = 2.0d * Math.PI * signal.getFrequency();
		final double length = sample.lengthMeters();
		double diffusivity = (omega * length * length) / (kappa * kappa);

		final double HEAT_FLUX = 1E8;

		double capacitance = HEAT_FLUX * kappa / (signal.getAmplitude() * sample.density() * length * omega
				* Math.sqrt(Math.pow(Math.sinh(preKappaAngle), 2) + Math.pow(Math.sin(preKappaAngle), 2)));

		Diffusivity tCond = new Diffusivity();

		tCond.amplitude = signal.getAmplitude();
		tCond.kappa = kappa;
		tCond.phase = adjustedAngle;
		tCond.diffusivity = diffusivity;
		tCond.initSignalParams = signal;
		tCond.frequency = signal.getFrequency();
		tCond.capacitance = capacitance;
		return tCond;
	}

}
