package model.experiment.measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс измерений. Хранит в себе одну точку измерения. Каждой точке присвоено
 * время, частота измерения, массив возможных вычисленных температур и массив
 * вычисленных значений коэффициента температуропроводности
 * 
 * @author Mike
 * 
 */
public class Measurement implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7462056475933664988L;
	/**
	 * Время измерения
	 */
	public long					time;
	/**
	 * Частота эксперимента
	 */
	public double				frequency;
	/**
	 * Массив температур
	 * 
	 * @see Temperature
	 */
	public List<Temperature>	temperature;
	/**
	 * Массив значений температуропроводности
	 * 
	 * @see Diffusivity
	 */
	public List<Diffusivity>	tCond;

	public Measurement() {
		time = System.currentTimeMillis();
		temperature = new ArrayList<>();
		tCond = new ArrayList<>();
	}

	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("Частота;");
		for (Temperature t : temperature) {
			sb.append(t.getHeader() + ";");
		}
		for (Diffusivity t : tCond) {
			sb.append(t.getHeader() + ";");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%4.1f;", frequency));
		for (Temperature t : temperature) {
			sb.append(t.toString() + ";");
		}
		for (Diffusivity t : tCond) {
			sb.append(t.toString() + ";");
		}
		return sb.toString();
	}

	/**
	 * Считывает измерение из файла
	 * 
	 * @param filename
	 *            Строка с полным или относительным путём к файлу с измерением
	 * @return объект измерений из файла или <b>null</b> если файла не
	 *         существует или он не содержит измерение
	 */
	public static Measurement forBinary(String filename) {

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
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
}
