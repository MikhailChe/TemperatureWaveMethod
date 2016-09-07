package model.experiment.measurement;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.bind.JAXB;

import org.junit.Test;

import model.experiment.signalID.DCsignalID;
import model.thermocouple.graduate.Graduate;

public class TemperatureTest {
	@Test
	public void testXmlWrite() {
		Temperature t = new Temperature();

		t.signalID = new DCsignalID();
		Graduate grad = new Graduate();
		t.signalID.setGraduate(grad);

		t.signalLevel = 16.489;
		t.value = 578.85;

		JAXB.marshal(t, new File("testTemperature.xml"));

		Temperature deser = JAXB.unmarshal(new File("testTemperature.xml"),
				Temperature.class);

		assertEquals(t.signalLevel, deser.signalLevel, 0.01);
		assertEquals(t.value, deser.value, 0.01);
	}
}
