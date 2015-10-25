package ru.dolika.experimentAnalyzer.zeroCrossing;

import java.io.File;
import java.util.HashMap;

/**
 * Фактори для создания юстировочного класса
 * 
 * @author Mikey
 *
 */
public class ZeroCrossingFactory {

	private static HashMap<File, ZeroCrossing> cache = new HashMap<File, ZeroCrossing>();

	/**
	 * Выдаёт файл юстировки. Если файл уже открывался, то берется файл из кэша,
	 * иначе считывается новый файл
	 * 
	 * @param file
	 *            файл с юстировкой
	 * @return новый объект класса юстировки или уже существующий объект из кэша
	 */
	public synchronized static ZeroCrossing forFile(File file) {
		if (file == null) {
			throw new NullPointerException();
		}
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
					ZeroCrossing crossing = new ZeroCrossing(file);
					cache.put(file, crossing);
					return crossing;
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

}
