package controller.experiment.analyzer;

public class PhaseUtils {
	/**
	 * @param value
	 * @return positive angle (from 0 to 2 * Pi)
	 */
	public static double truncatePositive(double value) {
		while (value < 0) {
			value += Math.PI * 2.0;
		}
		while (value > Math.PI * 2.0) {
			value -= Math.PI * 2.0;
		}
		return value;
	}

	/**
	 * @param value
	 * @return negative angle (from 0 to -2 * Pi)
	 */
	public static double truncateNegative(double value) {
		while (value > 0) {
			value -= Math.PI * 2.0;
		}
		while (value < -Math.PI * 2.0) {
			value += Math.PI * 2.0;
		}
		return value;
	}
}
