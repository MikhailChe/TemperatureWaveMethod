package ru.dolika.experiment.Analyzer.test;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.dolika.experiment.Analyzer.ExperimentFileReader;

public class EreaderTest {
	static ExperimentFileReader reader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		reader = new ExperimentFileReader(
				new File(EreaderTest.class.getResource("temperatureWaveTestSignal.txt").getFile()).toPath());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testExperimentReader() {
		Assert.assertNotNull(reader);
	}

	@Test
	public final void testGetExperimentFrequency() {
		Assert.assertEquals(1, reader.getExperimentFrequency(), 0.01);
	}

	@Test
	public final void testGetCroppedData() {
		Assert.assertEquals(4, reader.getCroppedData().length);
	}

	@Test
	public final void testGetCroppedDataPeriodsCount() {
		Assert.assertEquals(10, reader.getCroppedDataPeriodsCount());
	}

	@Test
	public final void testGetInitialData() {
		Assert.assertEquals(4, reader.getInitialData().length);
		for (double[] data : reader.getInitialData()) {
			Assert.assertEquals(10000, data.length);

		}
	}

	@Test
	public final void testGetDataColumn() {
		Assert.assertEquals(reader.getDataColumn(0)[0], 0, 0.1);
		Assert.assertEquals(reader.getDataColumn(0)[3], 100, 0.1);

		Assert.assertEquals(reader.getDataColumn(1)[1], 100, 0.1);
		Assert.assertEquals(reader.getDataColumn(2)[1], 6, 0.1);
		Assert.assertEquals(reader.getDataColumn(3)[0], 65536, 1);
	}

	@Test
	public final void testGetColumnCount() {
		Assert.assertEquals(reader.getColumnCount(), 4);
	}

	@Test
	public final void testGetPulseIndicies() {
		int[] pulses = reader.getPulseIndicies();

		Assert.assertEquals(pulses[0], 3);
		Assert.assertEquals(pulses[1], 1003);
		Assert.assertEquals(pulses[2], 2003);
		Assert.assertEquals(pulses[3], 3003);
		Assert.assertEquals(pulses[4], 4003);

		Assert.assertEquals(10, pulses.length);

	}
}
