package ru.dolika.experimentAnalyzer;

import java.util.Arrays;

public class Filter {

	public static double[] lowPass(double[] array, double dt, double RC) {

		if (array == null)
			return null;
		if (array.length == 0)
			return new double[0];
		if (RC <= 0)
			return array;
		if (dt <= 0)
			return array;
		double alpha = dt / (RC + dt);
		double mean = 0;
		for (int i = 0; i < array.length; i++) {
			mean += array[i] / array.length;
		}
		array[0] = mean;
		for (int i = 1; i < array.length; i++) {
			array[i] = alpha * array[i] + (1 - alpha) * array[i - 1];
		}
		return array;
	}

	public static double[] inverse(double[] array) {
		if (array == null)
			return null;
		if (array.length == 0)
			return new double[0];
		for (int i = 0; i < array.length / 2; i++) {
			double foo = array[i];
			array[i] = array[array.length - 1 - i];
			array[array.length - 1 - i] = foo;
		}
		return array;
	}

	public static double[] highPass(double[] array, double dt, double RC) {
		if (array == null)
			return null;
		if (array.length == 0)
			return new double[0];
		if (RC <= 0)
			return array;
		if (dt <= 0)
			return array;
		double[] helper = Arrays.copyOf(array, array.length);
		double alpha = RC / (RC + dt);

		for (int i = 1; i < helper.length; i++) {
			array[i] = alpha * array[i - 1] + alpha
					* (helper[i] - helper[i - 1]);
			if (!Double.isFinite(array[i])) {
				array[i] = 0;
			}
		}
		return array;
	}
}
