package model.sample;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;
import model.measurement.Measurement;

@XmlAccessorType(XmlAccessType.NONE)
public class Sample {

	@XmlAttribute
	private String name = "Default_name";
	@XmlAttribute
	private double length = 0;
	@XmlAttribute
	private double density = 0;
	private String comment = "Default_comment";

	private PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

	private transient final static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл образца (*.smpl)", "smpl");

	@XmlElement
	public List<Measurement> measurements = new ArrayList<>();

	Sample() {
		super();
		setComment("Default_comment");
		setName("Default_name");
		setLength(0D);
		setDensity(1000D);
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
		String oldName = this.name;
		this.name = name;
		mPcs.firePropertyChange("name", oldName, this.name);
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		double oldLength = this.length;
		this.length = length;
		mPcs.firePropertyChange("length", oldLength, this.length);
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		double oldDensity = this.density;
		this.density = density;
		mPcs.firePropertyChange("density", oldDensity, this.density);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		mPcs.firePropertyChange("comment", oldComment, this.comment);
	}

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(Sample.class, this, (Sample) o,
				Arrays.asList(Sample::getLength, Sample::getName, Sample::getComment));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.comment, this.length);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Имя:\t" + getName() + "\n");
		sb.append("Коментарий:\t" + getComment() + "\n");
		sb.append("Длина:\t" + getLength() + "\n");

		sb.append("Измерения\n");
		for (Measurement m : measurements) {
			try {
				sb.append(m.toString() + "\n");
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		mPcs.addPropertyChangeListener(listener);

	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		mPcs.removePropertyChangeListener(listener);
	}

}
