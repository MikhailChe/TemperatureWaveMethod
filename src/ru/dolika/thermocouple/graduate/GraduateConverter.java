package ru.dolika.thermocouple.graduate;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

public class GraduateConverter {

	JFileChooser fileChooser;

	public GraduateConverter(Component parent) {
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChooser.setDialogTitle("Выберите фалй с градуировкой");
		int status = fileChooser.showOpenDialog(parent);
		if (status != JFileChooser.APPROVE_OPTION)
			return;
		if (fileChooser.getSelectedFile() == null)
			return;

		Graduate graduate = null;

		graduate = GraduateFactory.forBinary(fileChooser.getSelectedFile().getAbsolutePath());

		if (graduate == null) {
			graduate = GraduateFactory.forFile(fileChooser.getSelectedFile().getAbsolutePath());
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

		ArrayList<Double> values = new ArrayList<Double>();
		for (String line : tVtgLines) {
			line.replaceAll(",", ".");

			Double voltage = null;
			try {
				voltage = Double.valueOf(line);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (voltage == null)
				continue;

			values.add(graduate.getTemperature(voltage, 273 + 22));
		}

		try (PrintStream ps = new PrintStream(fileChooser.getSelectedFile())) {
			for (Double value : values) {
				ps.println(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
