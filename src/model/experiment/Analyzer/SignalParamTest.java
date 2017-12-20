package model.experiment.Analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;

public class SignalParamTest {
	@SuppressWarnings("static-method")
	@Test
	public void jaxification() throws IOException {
		SignalParameters params = new SignalParameters(2, 7,
				.15);
		Path path = Files.createTempFile(null, null);
		System.out.println("Created temp file: " + path);
		JAXB.marshal(params, path.toFile());
		Files.lines(path).forEach(System.out::println);

		SignalParameters unmarch = JAXB.unmarshal(
				path.toFile(), SignalParameters.class);

		Files.deleteIfExists(path);

		System.out.println((unmarch.equals(params)
				? "equals"
				: "not equals"));
		Assert.assertEquals(params, unmarch);
	}
}
