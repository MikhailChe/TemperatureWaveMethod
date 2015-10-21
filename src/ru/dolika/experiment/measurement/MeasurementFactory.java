package ru.dolika.experiment.measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Класс для порождения измерений: новых или из бинарного файла
 * 
 * @author Mike
 *
 */
public class MeasurementFactory {

	/**
	 * Создаёт новый оъект измерений
	 * 
	 * @return новый пустой объект измерений
	 * @see Measurement
	 */
	public static Measurement getMeasurement() {
		return new Measurement();
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

}
