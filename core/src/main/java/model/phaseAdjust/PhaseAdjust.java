package model.phaseAdjust;

import static java.util.Collections.synchronizedNavigableMap;
import static jakarta.xml.bind.annotation.XmlAccessType.NONE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import controller.lambda.Predicates;

/**
 * Класс юстировки. Хранит в себе информацию о юстировке нулевой фазы
 * 
 * @author Mikey
 *
 */
@XmlAccessorType(NONE)
public class PhaseAdjust {

    @XmlAttribute
    private File forFile;
    @XmlElement
    private NavigableMap<Double, Double> shifts;

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

    public File getFile() {
	return forFile;
    }

    public static PhaseAdjust load(Path file, Locale numberLocale) throws IOException {
	PhaseAdjust adjustment = new PhaseAdjust(file.toFile());
	adjustment.readShifts(numberLocale);
	return adjustment;
    }

    private void readShifts(Locale numberLocale) throws IOException {
	try (Scanner scanner = new Scanner(forFile.toPath(), StandardCharsets.UTF_8).useLocale(numberLocale)) {
	    while (scanner.hasNext()) {
		if (!scanner.hasNextDouble()) {
		    throw new IOException("Invalid phase calibration frequency in " + forFile);
		}
		double frequency = scanner.nextDouble();
		if (!scanner.hasNextDouble()) {
		    throw new IOException("Missing or invalid phase shift in " + forFile);
		}
		double shift = scanner.nextDouble();
		if (!Double.isFinite(frequency) || !Double.isFinite(shift)) {
		    throw new IOException("Non-finite phase calibration value in " + forFile);
		}
		shifts.put(frequency, shift);
	    }
	    if (scanner.ioException() != null) {
		throw scanner.ioException();
	    }
	    if (shifts.isEmpty()) {
		throw new IOException("Empty phase calibration: " + forFile);
	    }
	}
    }

    private void initialize() {
	if (shifts.isEmpty()) {
	    try {
		readShifts(Locale.getDefault(Locale.Category.FORMAT));
	    } catch (IOException e) {
		shifts.clear();
		throw new java.io.UncheckedIOException(e);
	    }
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