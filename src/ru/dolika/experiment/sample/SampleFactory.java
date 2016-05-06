package ru.dolika.experiment.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

import ru.dolika.ui.MemorableDirectoryChooser;

public class SampleFactory {

	static boolean debug = true;

	public static Sample getSample() {
		return new Sample();
	}

	public static Sample forBinary(String filename) {
		if (debug)
			System.out.println("Opening samplefile " + filename);

		try (ObjectInputStream ois = new ObjectInputStream(
				new ProgressMonitorInputStream(null, "Открытие", new FileInputStream(filename)))) {
			Object o = ois.readObject();
			if (o instanceof Sample) {
				Sample sample = (Sample) o;
				if (debug)
					System.out.println("Opened sample binary");
				if (debug)
					if (sample.name == null)
						System.out.println("Sample name empty (null)");
					else
						System.out.println("Sample name: " + sample.name);

				return sample;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (debug)
			System.out.println("Problems openning sample file");
		return null;
	}

	public static File saveSample(String filename, final Sample sample) {
		if (filename == null) {
			MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(SampleFactory.class);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.resetChoosableFileFilters();
			FileNameExtensionFilter filter = Sample.extensionFilter;
			chooser.setFileFilter(filter);
			chooser.addChoosableFileFilter(filter);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				chooser.saveCurrentSelection();
				filename = chooser.getSelectedFile().toString();
				if (!filename.toLowerCase().endsWith(".smpl")) {
					filename += ".smpl";
				}
			}

		}
		if (filename != null) {
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
				oos.writeObject(sample);
				return new File(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
