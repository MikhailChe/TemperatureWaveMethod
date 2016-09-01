package ru.dolika.experiment.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.filechooser.FileNameExtensionFilter;

import ru.dolika.experiment.measurement.Measurement;

public class Sample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5445072467730687777L;
	private String name;
	private double length;
	private String comment;

	private transient final static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл образца (*.smpl)", "smpl");

	public List<Measurement> measurements = new ArrayList<>();;

	public Sample() {
		setComment("Default_comment");
		setName("Default_name");
	}

	/**
	 * @return the extensionfilter
	 */
	public static FileNameExtensionFilter getExtensionfilter() {
		return extensionFilter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;

		if (!(o instanceof Sample))
			return false;
		Predicate<Function<Sample, Object>> equalizer = (
				Function<Sample, Object> f) -> {
			return f.apply(this).equals(f.apply((Sample) o));
		};
		return equalizer.test(Sample::getLength)
				&& equalizer.test(Sample::getName)
				&& equalizer.test(Sample::getComment);
	}

	@Override
	public int hashCode() {
		return 0;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Имя:\t" + getName() + "\n");
		sb.append("Коментарий:\t" + getComment() + "\n");
		sb.append("Длина:\t" + getLength() + "\n");

		sb.append("Измерения\n");
		for (Measurement m : measurements) {
			sb.append(m.toString() + "\n");
		}
		return sb.toString();
	}

	public void sort() {
		measurements.sort((o1, o2) -> {
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
		});
	}

}
