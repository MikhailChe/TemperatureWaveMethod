package model.thermocouple.graduate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import debug.JExceptionHandler;

public class GraduateFactory {
	public synchronized static Graduate forBinary(File file) {
		if (file == null)
			return null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			Object o = ois.readObject();
			if (o instanceof Graduate) {
				Graduate g = (Graduate) o;
				return g;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized static Graduate forTextFile(Path file) {
		if (file == null)
			return null;
		try {
			System.out.println("Открываю текстовый файл градуировки: " + file);
			Graduate grads = GraduateFactory.parseTextFile(file);
			return grads;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Graduate parseTextFile(Path path) {
		Graduate g = new Graduate();
		g.name = path.getFileName().toString();
		try {
			List<String> fileLines = Files.readAllLines(path);

			int currentTemperature = 0;
			for (String singleLine : fileLines) {

				List<String> vtgValStrings = Arrays.asList(singleLine.replaceAll(",", ".").split("\t"));

				double innerTemperature = currentTemperature;
				double innerTemperatureIncrement = 10.0 / vtgValStrings.size();
				// TODO: This can be rewriten as java8 code
				for (String voltageStr : vtgValStrings) {

					try {
						Double voltage = Double.valueOf(voltageStr);
						if (voltage != null) {
							g.grads.put(voltage, innerTemperature);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						innerTemperature += innerTemperatureIncrement;
					}
				}

				currentTemperature += 10;

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return g;
	}

	/**
	 * Сохранить градуировку в объектный файл
	 * 
	 * @param file
	 *            файл, в который нужно записать граудировку
	 */

	public static void saveBinary(File file, Graduate g) {
		if (file == null)
			throw new NullPointerException("file is null");
		g.name = file.getName();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(g);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e);
			e.printStackTrace();
		}
	}

}