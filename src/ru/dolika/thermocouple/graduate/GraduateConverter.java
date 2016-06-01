package ru.dolika.thermocouple.graduate;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;

/**
 * Преобразователь градуировки из термоЭДС в температуру с помощью файла
 * градуировки. Используется GUI. <b>Внимание</b>, нет проверки на наличие
 * графической среды, так что могут быть сбои
 * 
 * @author Mikey
 *
 */
public class GraduateConverter {

	JFileChooser fileChooser;

	public GraduateConverter(Component parent) {
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChooser.setDialogTitle("Выберите файл с градуировкой");
		int status = fileChooser.showOpenDialog(parent);
		if (status != JFileChooser.APPROVE_OPTION)
			return;
		if (fileChooser.getSelectedFile() == null)
			return;

		Graduate graduate = null;

		graduate = GraduateFactory.forBinary(fileChooser.getSelectedFile());

		if (graduate == null) {
			graduate = GraduateFactory.forFile(fileChooser.getSelectedFile());
		}

		if (graduate == null)
			return;

		fileChooser.setDialogTitle("Какой файл преобзовать?");
		status = fileChooser.showOpenDialog(parent);
		if (status != JFileChooser.APPROVE_OPTION)
			return;
		if (fileChooser.getSelectedFile() == null)
			return;

		List<String> tVtgLines = null;

		try {
			tVtgLines = Files.readAllLines(fileChooser.getSelectedFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (tVtgLines == null)
			return;

		List<Double> values = new ArrayList<Double>();
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
		for (String line : tVtgLines) {
			Double voltage = 0d;
			try {
				Number n = numberFormat.parse(line);
				voltage = n.doubleValue();
			} catch (ParseException e1) {
				e1.printStackTrace();
			} finally {
				values.add(graduate.getTemperature(voltage, 22 + 273));
			}
		}

		try (PrintStream ps = new PrintStream(fileChooser.getSelectedFile())) {
			for (Double value : values) {
				ps.println(String.format(Locale.getDefault(), "%.3f", value));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
