package tests;

import static controller.experiment.Analyzer.PhaseUtils.truncateNegative;
import static controller.experiment.Analyzer.PhaseUtils.truncatePositive;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhaseUtilsTest {

	@SuppressWarnings("static-method")
	@Test
	public void testTruncatePositive() {
		double test1 = Math.toRadians(-90);
		double testOutput1 = truncatePositive(test1);

		assertEquals(Math.toRadians(-90 + 360), testOutput1, 0.001);

		double test2 = Math.toRadians(450);
		double testOutput2 = truncatePositive(test2);

		assertEquals(Math.toRadians(450 - 360), testOutput2, 0.001);

		assertEquals(0, truncatePositive(0), 0.001);
		assertEquals(1, truncatePositive(1), 0.001);

	}

	@SuppressWarnings("static-method")
	@Test
	public void testTruncateNegative() {
		double test1 = Math.toRadians(+90);
		double testOutput1 = truncateNegative(test1);

		assertEquals(Math.toRadians(+90 - 360), testOutput1, 0.001);

		double test2 = Math.toRadians(-450);
		double testOutput2 = truncateNegative(test2);

		assertEquals(Math.toRadians(-450 + 360), testOutput2, 0.001);

		assertEquals(0, truncateNegative(0), 0.001);
		assertEquals(-1, truncateNegative(-1), 0.001);

	}

}
