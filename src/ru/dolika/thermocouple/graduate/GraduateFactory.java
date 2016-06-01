package ru.dolika.thermocouple.graduate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Optional;

public class GraduateFactory {

	private static HashMap<File, Graduate> cache = new HashMap<File, Graduate>();

	public synchronized static Graduate forBinary(File file) {

		if (file == null)
			return null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			Object o = ois.readObject();
			if (o instanceof Graduate) {
				Graduate g = (Graduate) o;
				if (g != null) {
					g.fromFile = file;
				}
				return g;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized static Graduate forFile(File file) {

		if (file == null)
			return null;
		if (cache != null) {
			synchronized (cache) {
				if (cache.containsKey(file) && cache.get(file) != null) {
					return cache.get(file);
				}
				cache.remove(file);
				if (!file.exists()) {
					return null;
				}
				try {
					System.out.println("Actually opening a file " + file);
					Graduate grads = new Graduate(file);
					cache.put(file, grads);
					return grads;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}
}