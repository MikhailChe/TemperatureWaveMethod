package model.experiment.measurement;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;

import model.experiment.Analyzer.SignalParameters;
import model.experiment.signalID.BaseSignalID;
import model.experiment.signalID.DCsignalID;
import model.experiment.zeroCrossing.ZeroCrossing;
import model.thermocouple.graduate.Graduate;

public class MeasurementsTest {
	@Test
	public void diffusivityjaxbification() throws IOException {
		Diffusivity march = new Diffusivity();
		march.amplitude = 100;
		march.diffusivity = 12E-7;
		march.frequency = 15;
		march.kappa = 3.2;
		march.phase = 16;

		march.initSignalParams = new SignalParameters(18, 32, .12);
		march.signalID = new BaseSignalID();
		march.signalID.zc = new ZeroCrossing(new File("Hello.zc"));

		Path path = Files.createTempFile(null, null);
		System.out.println("Created temp file: " + path);
		JAXB.marshal(march, path.toFile());
		Files.lines(path).forEach(System.out::println);

		Diffusivity unmarch = JAXB.unmarshal(path.toFile(), Diffusivity.class);

		Files.deleteIfExists(path);
		System.out.println((unmarch.equals(march) ? "equals" : "not equals"));
		Assert.assertEquals(march, unmarch);
	}

	@Test
	public void temperatureJaxbification() throws IOException {
		Temperature march = new Temperature();

		march.signalLevel = 0.0025;
		march.value = 32;
		march.signalID = new DCsignalID();
		march.signalID.setGraduate(new Graduate());

		Path path = Files.createTempFile(null, null);
		System.out.println("Created temp file: " + path);
		JAXB.marshal(march, path.toFile());
		Files.lines(path).forEach(System.out::println);

		Temperature unmarch = JAXB.unmarshal(path.toFile(), Temperature.class);

		Files.deleteIfExists(path);
		assertEquals(march.signalID, unmarch.signalID);

		assertEquals(march.signalLevel, unmarch.signalLevel, .01);
		assertEquals(march.value, unmarch.value, 0.01);
		System.out.println((unmarch.equals(march) ? "equals" : "not equals"));
		assertEquals(march, unmarch);
	}

	@Test
	public void measurementJabxification() throws IOException {
		Measurement march = new Measurement();

		march.frequency = 7;

		march.diffusivity.add(new Diffusivity());
		march.diffusivity.add(new Diffusivity());

		march.temperature.add(new Temperature());
		march.temperature.add(new Temperature());

		Path path = Files.createTempFile(null, null);
		System.out.println("Created temp file: " + path);
		JAXB.marshal(march, path.toFile());
		Files.lines(path).forEach(System.out::println);

		Measurement unmarch = JAXB.unmarshal(path.toFile(), Measurement.class);

		Files.deleteIfExists(path);
		System.out.println((unmarch.equals(march) ? "equals" : "not equals"));
		Assert.assertEquals(march, unmarch);
	}

}
