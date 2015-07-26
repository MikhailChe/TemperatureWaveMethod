package ru.dolika.experimentAnalyzer;

import java.awt.GridLayout;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.dolika.experimentAnalyzer.drawing.JDrawingTabsPlane;

public class Main {
	static final String LAST_FILE = "experiment_storage_lastfile";
	static Preferences prefs = Preferences.userNodeForPackage(Main.class);

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Drawer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JDrawingTabsPlane main = new JDrawingTabsPlane();
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(main);

		JMenuBar bar = new JMenuBar();

		// Settings Menu, S - Mnemonics
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		bar.add(settingsMenu);

		// Settings -> showIndicies, I - Mnemonic
		JCheckBoxMenuItem showIndiciesMenuItem = new JCheckBoxMenuItem(
				"Show Indicies", DrawingPlane.shouldShowIndicies);
		showIndiciesMenuItem.setMnemonic(KeyEvent.VK_I);
		settingsMenu.add(showIndiciesMenuItem);

		// Settings -> filter, I - Mnemonic
		JCheckBoxMenuItem shouldFilterMenuItem = new JCheckBoxMenuItem(
				"Should Filter", DrawingPlane.shouldFilter);
		shouldFilterMenuItem.setMnemonic(KeyEvent.VK_F);
		settingsMenu.add(shouldFilterMenuItem);

		frame.setJMenuBar(bar);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		ExperimentReader ereader;

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

			int chooserVal = chooser.showOpenDialog(frame);

			if (chooserVal == JFileChooser.APPROVE_OPTION) {
				prefs.put(LAST_FILE, chooser.getSelectedFile().toString());
				selectedFile = chooser.getSelectedFile().toPath();
			} else {
				System.exit(0);
				return;
			}
		} else {

			selectedFile = new File(args[0]).toPath();
		}
		try {
			ereader = new ExperimentReader(selectedFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
			return;
		}
		frame.setTitle(selectedFile.toString() + ", частота: "
				+ ereader.getExperimentFrequency());
		main.loadData(ereader, 0, true);
		showIndiciesMenuItem.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Object o = e.getSource();
				if (o instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
					if (i.isSelected()) {
						DrawingPlane.shouldShowIndicies = true;
					} else {
						DrawingPlane.shouldShowIndicies = false;
					}
					main.repaint();

				}

			}
		});
		shouldFilterMenuItem.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Object o = e.getSource();
				if (o instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
					if (i.isSelected()) {
						DrawingPlane.shouldFilter = true;
					} else {
						DrawingPlane.shouldFilter = false;
					}
					main.repaint();
				}
			}
		});
	}
}
