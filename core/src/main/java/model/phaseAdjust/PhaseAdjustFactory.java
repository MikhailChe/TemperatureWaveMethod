package model.phaseAdjust;

import static java.util.Collections.synchronizedMap;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;

/**
 * Фактори для создания юстировочного класса
 * 
 * @author Mikey
 *
 */
public class PhaseAdjustFactory {

	final private static Map<File, WeakReference<PhaseAdjust>> cache = synchronizedMap(new Hashtable<>());

	/**
	 * Выдаёт файл юстировки. Если файл уже открывался, то берется файл из кэша,
	 * иначе считывается новый файл
	 * 
	 * @param file
	 *            файл с юстировкой
	 * @return новый объект класса юстировки или уже существующий объект из кэша
	 */
	public static PhaseAdjust forFile(File file) {
		if (file == null)
			throw new NullPointerException();
		if (cache == null)
			return null;

		return cache.compute(file, (f, r) -> {
			if (r == null || r.get() == null) {
				return new WeakReference<>(new PhaseAdjust(file));
			}
			return r;
		}).get();
	}
}
