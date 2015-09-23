package ru.dolika.experiment.measurement;

import java.util.ArrayList;
import java.util.Locale;

public class Measurement {

	public long time;
	public double frequency;
	public ArrayList<Temperature> temperature;
	public ArrayList<TemperatureConductivity> tCond;

	public Measurement() {
		time = System.currentTimeMillis();
		temperature = new ArrayList<Temperature>();
		tCond = new ArrayList<TemperatureConductivity>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.getDefault(), "%4.1f\t", frequency));

		for (Temperature t : temperature) {
			sb.append(t.toString() + "\t");
		}
		for (TemperatureConductivity t : tCond) {
			sb.append(t.toString() + "\t");
		}
		return sb.toString();
	}
}
