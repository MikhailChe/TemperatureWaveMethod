package view;

import javax.swing.filechooser.FileNameExtensionFilter;

public final class CalibrationFileFilters {
	public static final FileNameExtensionFilter PHASE_ADJUST = new FileNameExtensionFilter(
	    "Файл юстировки текстовый (*.zc)", "zc");
	public static final FileNameExtensionFilter THERMOCOUPLE = new FileNameExtensionFilter(
			"Файл градуировки бинарный (*.gradbin)", "gradbin");

	private CalibrationFileFilters() {}
}
