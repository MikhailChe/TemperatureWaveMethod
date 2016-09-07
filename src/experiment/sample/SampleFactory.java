package experiment.sample;

import static debug.Debug.isDebug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

import debug.Debug;
import view.MemorableDirectoryChooser;

public class SampleFactory {

	public static Sample getSample() {
		return new Sample();
	}

	public static Sample forBinary(String filename) {
		Debug.println("Opening samplefile " + filename);

		try (ObjectInputStream ois = new ObjectInputStream(
				new ProgressMonitorInputStream(null, "Открытие",
						new FileInputStream(filename)))) {
			Object o = ois.readObject();
			if (o instanceof Sample) {
				Sample sample = (Sample) o;
				Debug.println("Opened sample binary");
				if (isDebug())
					if (sample.getName() == null)
						Debug.println("Sample name empty (null)");
					else
						Debug.println("Sample name: " + sample.getName());

				return sample;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Debug.println("Problems openning sample file");
		return null;
	}

	public static File saveSample(String filename, final Sample sample) {
		if (filename == null) {
			MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(
					SampleFactory.class);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.resetChoosableFileFilters();
			FileNameExtensionFilter filter = Sample.getExtensionfilter();
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
			try (ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(filename))) {
				oos.writeObject(sample);
				return new File(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
