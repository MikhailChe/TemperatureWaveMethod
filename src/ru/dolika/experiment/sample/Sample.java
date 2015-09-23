package ru.dolika.experiment.sample;

import java.util.ArrayList;
import java.util.Comparator;

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

	@Override
	public String toString() {
		return "";
	}

	public void sort() {
		measurements.sort(new Comparator<Measurement>() {

			@Override
			public int compare(Measurement o1, Measurement o2) {
				if (o1 == null) {
					return -1;
				}
				if (o2 == null) {
					return 1;
				}

				if (o1 == o2) {
					return 0;
				}
				if (o1.equals(o2)) {
					return 0;
				}

				if (o1.temperature.get(0).value == o2.temperature.get(0).value)
					return 0;
				if (o1.temperature.get(0).value > o2.temperature.get(0).value) {
					return 1;
				}
				return -1;
			}
		});
	}

}
