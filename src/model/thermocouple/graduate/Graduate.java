package model.thermocouple.graduate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;

/**
 * 
 * Класс градуировки термопары. Предполагается, что значение термо-ЭДС вводится
 * в миливольтах Выдаваемое значение температуры в градусах
 * 
 * @author Mikey
 * 
 */

public class Graduate implements Serializable {
	static final private long serialVersionUID = 1347461243070602565L;

	static public FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
			"Файл градуировки бинарный (*.gradbin)", "gradbin");
	@XmlElement
	NavigableMap<Double, Double> grads;
	private Map<Double, Double> answerMap;
	@XmlElement
	String name;

	/**
	 * Конструктор градуировки
	 */
	public Graduate() {
		grads = Collections.synchronizedNavigableMap(new TreeMap<>());
		answerMap = Collections.synchronizedMap(new HashMap<>());
	}

	public String getName() {
		return name;
	}

	/**
	 * Получить температуру по напряжению. Напряжение вводится в миливольтах
	 * 
	 * @param voltage
	 *            Напряжение в миливольтах
	 * @param zeroTemp
	 *            Температура холодных концов
	 * 
	 * @return Разница температуры между горячими и холодными концами в кельвинах
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

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(Graduate.class, this, o, Arrays.asList(a -> a.grads));
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
