package ru.dolika.experimentAnalyzer.zeroCrossing;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

public class ZeroCrossingFactory {

	private static HashMap<String, ZeroCrossing> cache = new HashMap<String, ZeroCrossing>();

	public synchronized static ZeroCrossing forFile(String filename) {
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
					return null;
				}
				try {
					System.out.println("Actually opening a file " + filename);
					ZeroCrossing crossing = new ZeroCrossing(filename);
					cache.put(filename, crossing);
					return crossing;
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

}
