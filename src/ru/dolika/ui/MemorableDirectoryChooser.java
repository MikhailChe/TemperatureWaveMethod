package ru.dolika.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

public class MemorableDirectoryChooser extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5847949653692605158L;

	private static final String LAST_FOLDER = "memFileChooser_lastDirectory";

	private Preferences prefs;

	public MemorableDirectoryChooser(Class<?> classname) {
		super();
		this.setFileSelectionMode(DIRECTORIES_ONLY);
		this.setMultiSelectionEnabled(true);
		this.setPreferredSize(new Dimension(640, 480));

		this.getActionMap().get("viewTypeDetails").actionPerformed(null);

		try {
			prefs = Preferences.userNodeForPackage(classname);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		{
			String lastFolder = prefs.get(LAST_FOLDER, null);
			if (lastFolder != null) {
				try {
					File dir = new File(new File(lastFolder).getCanonicalPath());
					if (dir.isDirectory()) {
						setCurrentDirectory(dir);
						// setSelectedFile(dir);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveCurrentSelection() {
		if (prefs != null) {
			File f = getSelectedFile();
			System.out.println("saving directory. current file: " + f);
			if (!f.isDirectory()) {
				f = f.getParentFile();
				System.out.println("It's not a directory, so file is " + f);
			}
			if (f != null) {
				try {
					prefs.put(LAST_FOLDER, f.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public int showDialog(Component parent, String approveButtonText) {
		int status = super.showDialog(parent, approveButtonText);
		if (status == APPROVE_OPTION) {
			saveCurrentSelection();
		}
		return status;
	}

}
