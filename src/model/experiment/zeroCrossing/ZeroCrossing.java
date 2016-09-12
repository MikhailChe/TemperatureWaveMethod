package model.experiment.zeroCrossing;

import static java.util.Collections.synchronizedNavigableMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Класс юстировки. Хранит в себе информацию о юстировке нулевой фазы
 * 
 * @author Mikey
 *
 */
public class ZeroCrossing {
	final public static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл юстировки текстовы (*.zc)", "zc");

	final public File forFile;

	final private NavigableMap<Double, Double> shifts;

	final private Map<Double, Double> answerMap;

	/**
	 * Защищенный конструктор для создания юстировки из файла
	 * 
	 * @param filename
	 *            текстовый файл с юстировкой
	 * @throws IllegalArgumentException
	 */
	protected ZeroCrossing(File filename) throws IllegalArgumentException {
		shifts = synchronizedNavigableMap(new TreeMap<Double, Double>());
		answerMap = new HashMap<Double, Double>();

		Scanner s;
		try {
			s = new Scanner(new BufferedInputStream(new FileInputStream(
					filename)));

			while (s.hasNext()) {
				double key = 0;
				if (s.hasNextDouble()) {
					key = s.nextDouble();
				} else {
					key = s.nextInt();
				}
				double value = 0;

				if (s.hasNextDouble()) {
					value = s.nextDouble();
				} else {
					value = s.nextInt();
				}
				shifts.put(key, value);
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.forFile = filename;
	}

	/**
	 * Функиця вычисляет сдвиг фазы основываясь на существующих данных и
	 * интерполируя их
	 * 
	 * @param frequency
	 *            частота в Гц
	 * @return значение текущего сдвига фаз для выбранной частоты
	 */
	public synchronized double getCurrentShift(double frequency) {
		if (answerMap.containsKey(frequency)) {
			Double answer = answerMap.get(frequency);
			if (answer != null) {
				return answer;
			}
		}
		if (shifts.containsKey(frequency)) {
			Double value = shifts.get(frequency);
			if (value == null) {
				throw new NullPointerException();
			}
			answerMap.put(frequency, ((double) (value)));
			return value;
		} else {
			Double nearestHigherKey = shifts.higherKey(frequency);
			Double nearsetLowerKey = shifts.lowerKey(frequency);
			Double nearestHigherValue = null;
			Double nearestLowerValue = null;
			if (nearsetLowerKey == null && nearestHigherKey == null) {
				throw new NullPointerException();
			} else if (nearsetLowerKey == null) {
				nearestHigherValue = shifts.get(nearestHigherKey);
				if (nearestHigherValue == null) {
					throw new NullPointerException();
				}
				answerMap.put(frequency, nearestHigherValue);
				return nearestHigherValue;
			} else if (nearestHigherKey == null) {
				nearestLowerValue = shifts.get(nearsetLowerKey);
				if (nearestLowerValue == null) {
					throw new NullPointerException();
				}
				answerMap.put(frequency, nearestLowerValue);
				return nearestLowerValue;
			} else {
				nearestHigherValue = shifts.get(nearestHigherKey);
				nearestLowerValue = shifts.get(nearsetLowerKey);
				if (nearestHigherValue == null || nearestLowerValue == null) {
					throw new NullPointerException();
				}
				double diff = nearestHigherKey - nearsetLowerKey;
				if (diff == 0) {
					throw new NullPointerException();
				}
				double lowerDiff = frequency - nearsetLowerKey;
				double higherDiff = nearestHigherKey - frequency;
				double lowerK = 1 - (lowerDiff / diff);
				double higherK = 1 - (higherDiff / diff);
				double value = nearestLowerValue * lowerK + nearestHigherValue
						* higherK;
				answerMap.put(frequency, value);
				return value;
			}
		}
	}
}