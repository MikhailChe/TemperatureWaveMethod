package controller.experiment.analyzer;

public class PhaseUtils {
	private static final double FULL_TURN = Math.PI * 2.0;
	/**
	 * @param value
	 * @return positive angle (from 0 to 2 * Pi)
	 */
	public static double truncatePositive(double value) {
		if (value >= 0 && value <= FULL_TURN) {
			return value;
		}
		double angle = value % FULL_TURN;
		if (angle == 0) {
			return value > 0 ? FULL_TURN : 0;
		}
		return angle < 0 ? angle + FULL_TURN : angle;
	}

	/**
	 * @param value
	 * @return negative angle (from 0 to -2 * Pi)
	 */
	public static double truncateNegative(double value) {
		if (value <= 0 && value >= -FULL_TURN) {
			return value;
		}
		return -truncatePositive(-value);
	}
}
