package experiment.sample;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.filechooser.FileNameExtensionFilter;

import experiment.measurement.Measurement;

public class Sample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5445072467730687777L;
	private String name = "Default_name";
	private double length = 0;
	private String comment = "Default_comment";

	private PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

	private transient final static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл образца (*.smpl)", "smpl");

	public List<Measurement> measurements = new ArrayList<>();;

	public Sample() {
		super();
		setComment("Default_comment");
		setName("Default_name");
		setLength(0D);
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
		return this.name.hashCode() + this.comment.hashCode()
				+ Double.hashCode(this.length);
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		mPcs.addPropertyChangeListener(listener);

	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		mPcs.removePropertyChangeListener(listener);
	}

}
