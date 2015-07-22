package ru.dolika.thermocouple.graduate;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.HashMap;

public class GraduateFactory {

	private static HashMap<String, Graduate> cache = new HashMap<String, Graduate>();

	public synchronized static Graduate forFile(String filename)
			throws FileNotFoundException {
		if (filename == null) {
			throw new NullPointerException();
		}
		if (cache != null) {
			synchronized (cache) {
				if (cache.containsKey(filename) && cache.get(filename) != null) {
					return cache.get(filename);
				}
				cache.remove(filename);
				if (Files.notExists(new File(filename).toPath())) {
					throw new FileNotFoundException(filename);
				}
				try {
					System.out.println("Actually opening a file " + filename);
					Graduate grads = new Graduate(filename);
					cache.put(filename, grads);
					return grads;
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}
}