package model.experiment.measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.HashCoder;
import controller.lambda.Predicates;

/**
 * Класс измерений. Хранит в себе одну точку измерения. Каждой точке присвоено
 * время, частота измерения, массив возможных вычисленных температур и массив
 * вычисленных значений коэффициента температуропроводности
 * 
 * @author Mike
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Measurement {
	/**
	 * Время измерения
	 */
	@XmlElement
	public long time;
	/**
	 * Частота эксперимента
	 */
	@XmlAttribute
	public double frequency;
	/**
	 * Массив температур
	 * 
	 * @see Temperature
	 */
	@XmlElement
	public List<Temperature> temperature;
	/**
	 * Массив значений температуропроводности
	 * 
	 * @see Diffusivity
	 */
	@XmlElement
	public List<Diffusivity> diffusivity;

	public Measurement() {
		time = System.currentTimeMillis();
		temperature = new ArrayList<>();
		diffusivity = new ArrayList<>();
	}

	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("Частота;");
		for (Temperature t : temperature) {
			if (t != null)
				sb.append(Temperature.getHeader() + ";");
		}
		for (Diffusivity t : diffusivity) {
			if (t != null)
				sb.append(Diffusivity.getHeader() + ";");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%4.1f;", frequency));
		for (Temperature t : temperature) {
			if (t != null)
				sb.append(t.toString() + ";");
		}
		for (Diffusivity t : diffusivity) {
			if (t != null)
				sb.append(t.toString() + ";");
		}
		return sb.toString();
	}

	/**
	 * Считывает измерение из файла
	 * 
	 * @param filename
	 *            Строка с полным или относительным путём к файлу с измерением
	 * @return объект измерений из файла или <b>null</b> если файла не существует
	 *         или он не содержит измерение
	 */
	public static Measurement forBinary(String filename) {

		try (ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(
						filename))) {
			Object o = ois.readObject();
			if (o instanceof Measurement) {
				return (Measurement) o;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Создаёт новый оъект измерений
	 * 
	 * @return новый пустой объект измерений
	 * @see Measurement
	 */

	public static Measurement getMeasurement() {
		return new Measurement();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof Measurement))
			return false;
		Predicate<Function<Measurement, Object>> eq = Predicates
				.equalizer(this, (Measurement) o);
		return eq.test(a -> a.frequency)
				&& eq.test(a -> a.diffusivity)
				&& eq.test(a -> a.temperature)
				&& eq.test(a -> a.time);
	}

	@Override
	public int hashCode() {
		return HashCoder.hashCode(frequency, diffusivity,
				temperature, time);
	}
}
