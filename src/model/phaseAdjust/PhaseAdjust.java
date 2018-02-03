package model.phaseAdjust;

import static debug.Debug.println;
import static java.util.Collections.synchronizedNavigableMap;
import static javax.xml.bind.annotation.XmlAccessType.NONE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;

/**
 * Класс юстировки. Хранит в себе информацию о юстировке нулевой фазы
 * 
 * @author Mikey
 *
 */
@XmlAccessorType(NONE)
public class PhaseAdjust {
    final public static FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
	    "Файл юстировки текстовый (*.zc)", "zc");

    @XmlAttribute
    final public File forFile;
    @XmlElement
    final private NavigableMap<Double, Double> shifts;

    final private Map<Double, Double> answerMap;

    /**
     * Защищенный конструктор для создания юстировки из файла
     * 
     * @param filename
     *            текстовый файл с юстировкой
     * @throws IllegalArgumentException
     */
    public PhaseAdjust() {
	this(new File(""));
    }

    public PhaseAdjust(File filename) throws IllegalArgumentException {
	this.forFile = filename;
	shifts = synchronizedNavigableMap(new TreeMap<Double, Double>());
	answerMap = new Hashtable<>();

    }

    private void initialize() {
	if (!shifts.isEmpty())
	    return;

	try (Scanner s = new Scanner(new BufferedInputStream(new FileInputStream(forFile)))) {

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
	    println(e.getLocalizedMessage());
	    System.err.println(this.getClass().getName() + " : file not found " + forFile);
	}
    }

    /**
     * Функиця вычисляет сдвиг фазы основываясь на существующих данных и
     * интерполируя их
     * 
     * @param frequency
     *            частота в Гц
     * @return значение текущего сдвига фаз для выбранной частоты
     */
    public synchronized double getCurrentShift(double freq) {
	return answerMap.computeIfAbsent(freq, frequency -> {
	    if (shifts.isEmpty())
		initialize();
	    if (shifts.containsKey(frequency)) {
		Double value = shifts.get(frequency);
		if (value == null) {
		    throw new NullPointerException();
		}
		return value;
	    }
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
		return nearestHigherValue;
	    } else if (nearestHigherKey == null) {
		nearestLowerValue = shifts.get(nearsetLowerKey);
		if (nearestLowerValue == null) {
		    throw new NullPointerException();
		}
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
		double value = nearestLowerValue * lowerK + nearestHigherValue * higherK;

		return value;
	    }
	});
    }

    public double maxShift() {
	if (shifts.isEmpty())
	    initialize();
	return shifts.values().stream().mapToDouble(a -> a).max().orElse(Double.NaN);
    }

    public double minShift() {
	if (shifts.isEmpty())
	    initialize();
	return shifts.values().stream().mapToDouble(a -> a).min().orElse(Double.NaN);
    }

    public double minFrequency() {
	if (shifts.isEmpty())
	    initialize();
	return shifts.firstKey();
    }

    public double maxFrequency() {
	if (shifts.isEmpty())
	    initialize();
	return shifts.lastKey();
    }

    @Override
    public boolean equals(Object o) {
	return Predicates.areEqual(PhaseAdjust.class, this, o, Arrays.asList(a -> a.forFile));
    }

    @Override
    public int hashCode() {
	return forFile.hashCode();
    }

    @Override
    public String toString() {
	return "AdjustmentFile: " + forFile;
    }
}