package ru.dolika.thermocouple.graduate;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Graduate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1347461243070602565L;
	private NavigableMap<Double, Double> grads;
	private HashMap<Double, Double> answerMap;

	protected Graduate() {
		grads = Collections
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

			int intKey = 0;

			while (s.hasNext()) {

				double value = 0;
				if (s.hasNextDouble()) {
					value = s.nextDouble();
				} else {
					value = s.nextInt();
				}
				grads.put(intKey / 10.0, value);
				intKey += 1;
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	

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
				double value = nearestLowerValue * lowerK + nearestHigherValue
						* higherK;
				answerMap.put(voltage, value);
				return value + zeroTemp;
			}
		}

		return 0 + zeroTemp;
	}

}
