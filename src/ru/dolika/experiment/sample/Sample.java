package ru.dolika.experiment.sample;

import java.util.ArrayList;

import ru.dolika.experiment.measurement.Measurement;

public class Sample {

	public String name;
	public double length;
	public String comments;

	public ArrayList<Measurement> measurements;

	public Sample() {
		measurements = new ArrayList<Measurement>();
		comments = "Default_comment";
		name = "Default_name";
	}

}
