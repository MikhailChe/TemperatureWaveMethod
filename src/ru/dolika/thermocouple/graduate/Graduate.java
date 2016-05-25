package ru.dolika.thermocouple.graduate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.filechooser.FileNameExtensionFilter;

import ru.dolika.debug.JExceptionHandler;

/**
 * 
 * Класс градуировки термопары. Предполагается, что значение термо-ЭДС вводится
 * в миливольтах Выдаваемое значение температуры в градусах
 * 
 * @author Mikey
 * 
 */

public class Graduate implements Serializable {

	public static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл градуировки бинарный (*.gradbin)", "gradbin");

	private static final long serialVersionUID = 1347461243070602565L;
	private NavigableMap<Double, Double> grads;
	private HashMap<Double, Double> answerMap;

	public File fromFile;

	/**
	 * Конструктор градуировки
	 */
	protected Graduate() {
		grads = Collections.synchronizedNavigableMap(new TreeMap<Double, Double>());
		answerMap = new HashMap<Double, Double>();
	}

	/**
	 * Конструктор градуировки по имени файла
	 * 
	 * @param filename
	 * @throws IllegalArgumentException
	 */
	protected Graduate(File file) throws IllegalArgumentException {
		this();

		try {
			List<String> fileLines = Files.readAllLines(file.toPath());

			int currentTemperature = 0;
			for (String singleLine : fileLines) {
				singleLine = singleLine.replaceAll(",", ".");
				String[] vtgValStrings = singleLine.split("\t");

				double innerTemperature = currentTemperature;
				double innerTemperatureIncrement = 10.0 / vtgValStrings.length;

				for (String voltageStr : vtgValStrings) {

					try {
						Double voltage = Double.valueOf(voltageStr);
						if (voltage != null) {
							grads.put(voltage, innerTemperature);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						innerTemperature += innerTemperatureIncrement;
					}
				}

				currentTemperature += 10;

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		fromFile = file;
	}

	/**
	 * Получить температуру по напряжению. Напряжение вводится в миливольтах
	 * 
	 * @param voltage
	 *            Напряжение в миливольтах
	 * @param zeroTemp
	 *            Температура холодных концов
	 * 
	 * @return Разница температуры между горячими и холодными концами в
	 *         кельвинах
	 */
	public double getTemperature(double voltage, double zeroTemp) {

		if (answerMap.containsKey(voltage)) {
			Double val = answerMap.get(voltage);
			if (val != null) {
				return val + zeroTemp;
			}
		}
		if (grads.containsKey(voltage)) {
			Double val = grads.get(voltage);
			answerMap.put(voltage, val);
			if (val != null) {
				return answerMap.get(voltage) + zeroTemp;
			}
		} else {
			Double nearestHigherKey = grads.higherKey(voltage);
			Double nearsetLowerKey = grads.lowerKey(voltage);
			Double nearestHigherValue = null;
			Double nearestLowerValue = null;
			if (nearsetLowerKey == null && nearestHigherKey == null) {
				throw new NullPointerException();
			} else if (nearsetLowerKey == null) {
				nearestHigherValue = grads.get(nearestHigherKey);
				if (nearestHigherValue == null) {
					throw new NullPointerException();
				}
				answerMap.put(voltage, nearestHigherValue);
				return nearestHigherValue + zeroTemp;
			} else if (nearestHigherKey == null) {
				nearestLowerValue = grads.get(nearsetLowerKey);
				if (nearestLowerValue == null) {
					throw new NullPointerException();
				}
				answerMap.put(voltage, nearestLowerValue);
				return nearestLowerValue + zeroTemp;
			} else {
				nearestHigherValue = grads.get(nearestHigherKey);
				nearestLowerValue = grads.get(nearsetLowerKey);
				if (nearestHigherValue == null || nearestLowerValue == null) {
					throw new NullPointerException();
				}
				double diff = nearestHigherKey - nearsetLowerKey;
				if (diff == 0) {
					throw new NullPointerException();
				}
				double lowerDiff = voltage - nearsetLowerKey;
				double higherDiff = nearestHigherKey - voltage;
				double lowerK = 1 - (lowerDiff / diff);
				double higherK = 1 - (higherDiff / diff);
				double value = nearestLowerValue * lowerK + nearestHigherValue * higherK;
				answerMap.put(voltage, value);
				return value + zeroTemp;
			}
		}

		return 0 + zeroTemp;
	}

	/**
	 * Сохранить градуировку в объектный файл
	 * 
	 * @param file
	 *            файл, в который нужно записать граудировку
	 */
	public void save(File file) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e);
			e.printStackTrace();
		}
	}

}
