package experiment.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class SampleTest {

	@Test
	public final void testSampleEquals() {
		Sample s1 = new Sample();
		s1.setName("OK");
		s1.setLength(0.001127);
		s1.setComment("Commentary");

		Sample s2 = new Sample();
		s2.setName("OK");
		s2.setLength(0.001127);
		s2.setComment("Commentary");

		Sample diffName = new Sample();
		diffName.setName("NOTOK");
		diffName.setLength(0.001127);
		diffName.setComment("Commentary");

		Sample diffLength = new Sample();
		diffLength.setName("OK");
		diffLength.setLength(0.001128);
		diffLength.setComment("Commentary");

		Sample diffComment = new Sample();

		diffComment.setName("OK");
		diffComment.setLength(0.001127);
		diffComment.setComment("WHO R U?");

		assertEquals(s1, s2);
		assertNotEquals(s1, diffName);
		assertNotEquals(s2, diffName);

		assertNotEquals(s1, diffLength);
		assertNotEquals(s2, diffLength);

		assertNotEquals(s1, diffComment);
		assertNotEquals(s2, diffComment);

	}

}
