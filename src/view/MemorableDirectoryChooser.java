package view;

import static debug.JExceptionHandler.showException;
import static java.lang.Thread.currentThread;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import debug.Debug;

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

		SwingUtilities.invokeLater(() -> {
			try {
				MemorableDirectoryChooser c = MemorableDirectoryChooser.this;
				ActionMap map = c.getActionMap();
				Action a = map.get("viewTypeDetails");
				a.actionPerformed(null);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});

		try {
			prefs = Preferences.userNodeForPackage(classname);
		} catch (Exception e) {
			Debug.println(e.getLocalizedMessage());
			e.printStackTrace();
			return;
		}

		{
			String lastFolder = prefs.get(LAST_FOLDER, null);
			if (lastFolder != null) {
				try {
					File dir = new File(
							new File(lastFolder).getCanonicalPath());
					if (dir.isDirectory()) {
						setCurrentDirectory(dir);
						// setSelectedFile(dir);
					}
				} catch (Exception e) {
					showException(currentThread(), e);
				}
			}
		}
	}

	public void saveCurrentSelection() {
		if (prefs != null) {
			File f = getSelectedFile();
			if (f == null)
				return;
			if (!f.exists())
				return;

			System.out.println("saving directory. current file: " + f);

			if (!f.isDirectory()) {
				f = f.getParentFile();
				System.out.println("It's not a directory, so file is " + f);
			}
			try {
				prefs.put(LAST_FOLDER, f.getCanonicalPath());
				prefs.flush();
			} catch (Exception e) {
				showException(currentThread(), e);
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
