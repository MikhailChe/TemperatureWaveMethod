package ru.dolika.experimentAnalyzer;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.dolika.experimentAnalyzer.drawing.JDrawingTabsPlane;
import ru.dolika.experimentAnalyzer.drawing.JGraphImagePlane;

public class Main {
	static final String LAST_FILE = "experiment_storage_lastfile";
	static Preferences prefs = Preferences.userNodeForPackage(Main.class);

	public static Path fileOpen(String[] args, Component parent) {
		Path selectedFile = null;
		if (args.length == 0) {
			JFileChooser chooser = new JFileChooser(Main.class
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath());
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			{
				String lastFile = prefs.get(LAST_FILE, null);
				if (lastFile != null) {
					try {
						File f = new File(new File(lastFile).getCanonicalPath());
						chooser.setSelectedFile(f);
					} catch (Exception e) {

					}
				}
			}

			int chooserVal = chooser.showOpenDialog(parent);

			if (chooserVal == JFileChooser.APPROVE_OPTION) {
				prefs.put(LAST_FILE, chooser.getSelectedFile().toString());
				selectedFile = chooser.getSelectedFile().toPath();
			} else {
				return null;
			}
		} else {
			selectedFile = new File(args[0]).toPath();
		}
		return selectedFile;
	}

	public static ExperimentReader getEreader(Path selectedFile) {
		ExperimentReader ereader;
		try {
			ereader = new ExperimentReader(selectedFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return ereader;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Drawer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JDrawingTabsPlane main = new JDrawingTabsPlane();

		if (args.length > 0) {
			Path selectedFile = fileOpen(args, frame);
			if (selectedFile == null) {
				System.exit(0);
			}
			ExperimentReader ereader = getEreader(selectedFile);
			if (ereader == null) {
				System.exit(0);
			}
			for (double[] data : ereader.getCroppedData()) {
				main.addSignalTab(new double[][] { data }, null);
			}
		}

		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(main);

		JMenuBar bar = new JMenuBar();

		// File menu, F - Mnemonics
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		bar.add(fileMenu);

		// File -> open, O - mnemonics
		JMenuItem fileOpenMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		fileMenu.add(fileOpenMenuItem);

		fileOpenMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Path selectedFile = fileOpen(args, frame);
				if (selectedFile != null) {
					ExperimentReader ereader = getEreader(selectedFile);
					if (ereader != null) {
						for (double[] data : ereader.getCroppedData()) {
							main.addSignalTab(new double[][] { data }, null);
						}
					}
				}

			}
		});

		// Settings Menu, S - Mnemonics
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		bar.add(settingsMenu);

		// Settings -> showIndicies, I - Mnemonic
		JCheckBoxMenuItem showIndiciesMenuItem = new JCheckBoxMenuItem(
				"Show Indicies", JGraphImagePlane.shouldShowIndicies);
		showIndiciesMenuItem.setMnemonic(KeyEvent.VK_I);
		settingsMenu.add(showIndiciesMenuItem);

		frame.setJMenuBar(bar);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		showIndiciesMenuItem.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Object o = e.getSource();
				if (o instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
					if (i.isSelected()) {
						JGraphImagePlane.shouldShowIndicies = true;
					} else {
						JGraphImagePlane.shouldShowIndicies = false;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							main.repaint();
						}
					});

				}

			}
		});

	}
}
