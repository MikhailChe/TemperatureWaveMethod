package ru.dolika.experiment.measurement;

import java.util.ArrayList;

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
		String out = "";
		out += frequency + "\t";
		for (Temperature t : temperature) {
			out += t.toString() + "\t";
		}
		for (TemperatureConductivity t : tCond) {
			out += t.toString() + "\t";
		}
		return out;
	}

}
