package model.experiment.zeroCrossing;

import static java.util.Collections.synchronizedMap;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Фактори для создания юстировочного класса
 * 
 * @author Mikey
 *
 */
public class ZeroCrossingFactory {

	final private static Map<File, ZeroCrossing> cache = synchronizedMap(
	        new Hashtable<>());

	/**
	 * Выдаёт файл юстировки. Если файл уже открывался, то берется файл из кэша,
	 * иначе считывается новый файл
	 * 
	 * @param file
	 *            файл с юстировкой
	 * @return новый объект класса юстировки или уже существующий объект из кэша
	 */
	public static ZeroCrossing forFile(File file) {
		if (file == null) throw new NullPointerException();
		if (cache == null) return null;

		return cache.computeIfAbsent(file,
		        ZeroCrossing::new);
	}
}
