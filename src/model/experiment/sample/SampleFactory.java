package model.experiment.sample;

import java.io.File;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import debug.Debug;

public class SampleFactory {

	public static Sample getSample() {
		return new Sample();
	}

	public static Sample forXML(final String filename) {
		try {
			return JAXB.unmarshal(new File(filename), Sample.class);
		} catch (DataBindingException e) {
			Debug.println(e.getLocalizedMessage());
			return null;
		}
	}

	public static File saveSampleXML(final String filename, final Sample sample) {
		if (filename != null) {
			JAXB.marshal(sample, new File(filename));
			return new File(filename);
		}
		throw new NullPointerException("filename is null");
	}
}
