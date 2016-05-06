package ru.dolika.experiment.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.filechooser.FileNameExtensionFilter;

import ru.dolika.experiment.measurement.Measurement;

public class Sample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5445072467730687777L;
	public String name;
	public double length;
	public String comments;

	public static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("Файл образца (*.smpl)",
			"smpl");

	public ArrayList<Measurement> measurements;

	public Sample() {
		measurements = new ArrayList<Measurement>();
		comments = "Default_comment";
		name = "Default_name";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Имя:\t" + name + "\n");
		sb.append("Коментарий:\t" + comments + "\n");
		sb.append("Длина:\t" + length + "\n");

		sb.append("Измерения\n");
		for (Measurement m : measurements) {
			sb.append(m.toString() + "\n");
		}
		return sb.toString();
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

				if (o1.temperature.get(0).value == o2.temperature.get(0).value) {
					return 0;
				}
				if (o1.temperature.get(0).value > o2.temperature.get(0).value) {
					return 1;
				}
				return -1;
			}
		});
	}

}
