package ru.dolika.thermocouple.graduate;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Graduate {
	private NavigableMap<Double, Double> shifts;

	private HashMap<Double, Double> answerMap;

	protected Graduate() {
		shifts = Collections
				.synchronizedNavigableMap(new TreeMap<Double, Double>());
		answerMap = new HashMap<Double, Double>();
	}

	protected Graduate(String filename) throws IllegalArgumentException {
		this();
		Scanner s;
		try {

			// TODO: implement this thing
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
	}

	public double getTemperatureKelvin(double zeroTemp) {
		return 0;
		// TODO: as
	}

	public double getTemperatureCelsius(double zeroTemp) {
		return getTemperatureKelvin(zeroTemp) - 273.15;
	}

}
