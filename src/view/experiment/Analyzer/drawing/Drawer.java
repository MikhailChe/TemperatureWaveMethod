package view.experiment.Analyzer.drawing;

import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import controller.experiment.analyzer.TWMComputer;
import controller.experiment.reader.ExperimentFileReader;
import model.analyzer.SignalParameters;
import view.MemorableDirectoryChooser;

public class Drawer {

    public static Path choosePath(String[] args, Component parent) {
	Path selectedFile = null;
	if (args.length == 0) {
	    MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(Drawer.class);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
		selectedFile = chooser.getSelectedFile().toPath();
	    } else {
		return null;
	    }
	} else {
	    selectedFile = new File(args[0]).toPath();
	}
	return selectedFile;
    }

    public static ExperimentFileReader getEreader(Path selectedFile) {
	ExperimentFileReader ereader;
	try {
	    ereader = new ExperimentFileReader(selectedFile);
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
	return ereader;
    }

    public static void openNewTabs(final ExperimentFileReader reader, final JDrawingTabsPlane plane) {
	if (reader == null) {
	    return;
	}
	for (double[] data : reader.getCroppedData()) {

	    final int FREQ_INDEX = reader.getCroppedDataPeriodsCount();

	    SignalParameters params = TWMComputer.getSignalParameters(data, FREQ_INDEX,
		    reader.getExperimentFrequency());

	    double angle = params.phase;
	    double amplitude = params.amplitude;
	    double zeroAmplitudeShift = params.nullOffset;

	    double[] accordingWave = new double[data.length];
	    for (int i = 0; i < data.length; i++) {
		accordingWave[i] = Math
			.sin(2.0 * Math.PI * reader.getCroppedDataPeriodsCount() * ((double) i / (double) data.length)
				+ angle)
			* amplitude + zeroAmplitudeShift;
	    }

	    double[] zeroCrossageLine = new double[data.length];
	    for (int i = 0; i < zeroCrossageLine.length; i++) {
		zeroCrossageLine[i] = zeroAmplitudeShift;
	    }

	    plane.addSignalTab(new double[][] { data, accordingWave, zeroCrossageLine }, null);
	}
    }

    public static void main(String[] args) {
	JFrame frame = new JFrame("Рисователь");
	frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	JDrawingTabsPlane main = new JDrawingTabsPlane();

	if (args.length > 0) {
	    Path selectedFile = choosePath(args, frame);
	    if (selectedFile == null) {
		System.exit(0);
	    }
	    ExperimentFileReader ereader = getEreader(selectedFile);
	    if (ereader == null) {
		System.exit(0);
	    }
	    openNewTabs(ereader, main);
	}

	frame.getContentPane().setLayout(new GridLayout(1, 1));
	frame.getContentPane().add(main);

	JMenuBar bar = new JMenuBar();

	// File menu, F - Mnemonics
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(VK_F);
	bar.add(fileMenu);

	// File -> open, O - mnemonics
	JMenuItem fileOpenMenuItem = new JMenuItem("Open...", VK_O);
	fileMenu.add(fileOpenMenuItem);

	fileOpenMenuItem.addActionListener(e -> {
	    Path selectedFile = choosePath(args, frame);
	    if (selectedFile != null) {
		ExperimentFileReader ereader = getEreader(selectedFile);
		openNewTabs(ereader, main);
	    }
	});

	// Settings Menu, S - Mnemonics
	JMenu settingsMenu = new JMenu("Настройки");
	settingsMenu.setMnemonic(VK_S);
	bar.add(settingsMenu);

	// Settings -> showIndicies, I - Mnemonic
	JCheckBoxMenuItem showIndiciesMenuItem = new JCheckBoxMenuItem("Show Indicies",
		JGraphImagePlane.shouldShowIndicies);
	showIndiciesMenuItem.setMnemonic(VK_I);
	settingsMenu.add(showIndiciesMenuItem);

	frame.setJMenuBar(bar);
	frame.pack();
	frame.setVisible(true);
	frame.setExtendedState(Frame.MAXIMIZED_BOTH);

	showIndiciesMenuItem.addChangeListener(e -> {
	    Object o = e.getSource();
	    if (o instanceof JCheckBoxMenuItem) {
		JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
		JGraphImagePlane.shouldShowIndicies = i.isSelected();
		SwingUtilities.invokeLater(main::repaint);
	    }
	});

    }
}
