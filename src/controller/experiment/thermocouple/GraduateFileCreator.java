package controller.experiment.thermocouple;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import model.thermocouple.graduate.Graduate;
import model.thermocouple.graduate.GraduateFactory;
import view.CalibrationFileFilters;
import view.MemorableDirectoryChooser;

public class GraduateFileCreator implements Runnable {

	Component parent;

	public GraduateFileCreator(Component parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(this.getClass());
		chooser.setDialogTitle("Выберите текстовый файл градуировки");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			chooser.saveCurrentSelection();
			File inputFile = chooser.getSelectedFile();

			chooser.resetChoosableFileFilters();
			chooser.addChoosableFileFilter(CalibrationFileFilters.THERMOCOUPLE);
			chooser.setFileFilter(CalibrationFileFilters.THERMOCOUPLE);

			if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				chooser.saveCurrentSelection();
				File outputFile = chooser.getSelectedFile();
				
				if (!outputFile.toString().toLowerCase().endsWith("." + CalibrationFileFilters.THERMOCOUPLE.getExtensions()[0])) {
					outputFile = new File(outputFile.toString() + "." + CalibrationFileFilters.THERMOCOUPLE.getExtensions()[0]);
				}
				GraduateFactory.saveBinary(outputFile, GraduateFactory.forTextFile(inputFile.toPath()));
			}
		}

	}

}
