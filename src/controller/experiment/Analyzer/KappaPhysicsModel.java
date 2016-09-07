package controller.experiment.Analyzer;

public class KappaPhysicsModel {

	static double Bio = 0.4;

	public static double angleFor(double kappa) {
		double b1, b2, b3, b4;

		double kappaOverSqrt2 = kappa / Math.sqrt(2);

		b1 = Math.sinh(kappaOverSqrt2) * Math.cos(kappaOverSqrt2);
		b2 = Math.cosh(kappaOverSqrt2) * Math.sin(kappaOverSqrt2);
		b3 = Math.cosh(kappaOverSqrt2) * Math.cos(kappaOverSqrt2);
		b4 = Math.sinh(kappaOverSqrt2) * Math.sin(kappaOverSqrt2);

		double nominator = Bio * Bio * (b1 - b2)
				- 2.0 * kappaOverSqrt2 * (2.0 * Bio) * b4
				- kappa * kappa * (b1 + b2);
		double denominator = Bio * Bio * (b1 + b2)
				+ 2.0 * kappaOverSqrt2 * (2.0 * Bio) * b3
				+ kappa * kappa * (b1 - b2);

		double val = Math.atan2(nominator, denominator);
		if (val > 0) {
			val -= Math.PI * 2.0;
		}
		return val;
	}

	public static double searchKappaFor(double angle, double precision) {
		if (angle > 0) {
			return -1;
		}

		double kappaMin = 0;
		double kappaMax = 6;
		double kappa = 3;

		double angleForKappa = angleFor(kappa);
		int iterator = 0;
		while (Math.abs(angleForKappa - angle) > precision) {
			if (iterator > 100) {
				break;
			}
			if (angleForKappa < angle) {
				kappaMax = kappa;
			} else {
				kappaMin = kappa;
			}
			kappa = (kappaMax + kappaMin) / 2.0;
			angleForKappa = angleFor(kappa);
			iterator++;
		}

		return kappa;
	}
}
