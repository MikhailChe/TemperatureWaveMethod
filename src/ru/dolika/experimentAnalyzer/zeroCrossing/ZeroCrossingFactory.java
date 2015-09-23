package ru.dolika.experimentAnalyzer.zeroCrossing;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.HashMap;

public class ZeroCrossingFactory {

	public static void main(String[] args) {
		try {
			ZeroCrossing newAmp = ZeroCrossingFactory.forFile("newAmp.txt");
			for (double i = 1; i <= 4; i += 0.1) {
				System.out.println(i + "\t" + newAmp.getCurrentShift(i));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, ZeroCrossing> cache = new HashMap<String, ZeroCrossing>();

	public synchronized static ZeroCrossing forFile(String filename)
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
