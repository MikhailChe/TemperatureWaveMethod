package view.experiment.folderWatch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.experiment.Analyzer.TWMComputer;
import debug.Debug;
import model.experiment.measurement.Diffusivity;
import model.experiment.measurement.Measurement;
import model.experiment.workspace.Workspace;
import view.MemorableDirectoryChooser;
import view.measurements.MeasurementViewer;

public class FolderWatch extends JDialog implements Runnable {
	private static final long	serialVersionUID	= 1831549678406783975L;

	// private Workspace workspace;

	public File[]				filesInFolder;
	public File					folder;

	final MeasurementViewer		measurementViewer	= new MeasurementViewer();
	final Container				numbersContainer	= new Container();

	final JPanel				signalLevelPanel	= new JPanel();
	final JLabel				signalLevelLabel	= new JLabel(
			"Здесь будет термоЭДС");

	final JPanel				temperaturePanel	= new JPanel();
	final JLabel				temperatureLabel	= new JLabel(
			"Здесь будет температура");

	final List<JTDiffLabelSet>	tCondPanels			= new ArrayList<>();

	public static FolderWatch factory(JFrame parent)
			throws FileNotFoundException {
		Workspace workspace = Workspace.getInstance();
		if (workspace.getSample() == null) {
			JOptionPane.showMessageDialog(parent,
					"Не был выбран файл образца.\nПожалуйста закройте это окно и выберите образец или создайте новый",
					"Ошибка образца", JOptionPane.ERROR_MESSAGE);
			throw new FileNotFoundException("Не был выбран файл образца.");
		}
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(
				FolderWatch.class);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Выберите папку с данными");

		File folder;
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			Debug.println("Approved");
			folder = fileChooser.getSelectedFile();
			if (folder == null) {
				throw new FileNotFoundException();
			}
		} else {
			throw new FileNotFoundException();
		}
		Debug.println("Returning new FolderWatch");
		return new FolderWatch(parent, folder);
	}

	private FolderWatch(JFrame parent, File folder) {
		super(parent, false);
		Debug.println("Called constructor");
		this.folder = folder;
		Debug.println("Setting folder");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				isClosing = true;
			}

			@Override
			public void windowClosing(WindowEvent e) {
				isClosing = true;

			}
		});
		Debug.println("Added listener");
		SwingUtilities.invokeLater(() -> {

			this.setTitle("Я смотрю за " + folder.getAbsolutePath());
			signalLevelPanel
					.setBorder(BorderFactory
							.createTitledBorder("Уровень сигнала"));
			signalLevelPanel.add(signalLevelLabel);
			temperaturePanel
					.setBorder(BorderFactory.createTitledBorder("Температура"));
			temperaturePanel.add(temperatureLabel);

			numbersContainer.setLayout(new GridLayout(0, 2, 16, 16));

			numbersContainer.add(signalLevelPanel);
			numbersContainer.add(temperaturePanel);

			this.getContentPane().setLayout(new BorderLayout(16, 16));
			this.getContentPane().add(numbersContainer, BorderLayout.NORTH);
			this.getContentPane().add(measurementViewer);
			SwingUtilities.invokeLater(() -> this.pack());

			new Thread(this).start();
		});
	}

	public void checkNewFile() {
		File[] files = folder.listFiles(pathname -> {
			return pathname.getName().matches("^[0-9]+.txt$");
		});
		if (files != null) {
			if (filesInFolder == null) {
				if (files.length >= 1) {
					filesInFolder = files;
					for (File f : filesInFolder) {
						updateValuesForFile(f);
						if (isClosing) {
							return;
						}
					}
				}
				return;
			}

			if (filesInFolder.length != files.length) {
				if (files.length >= 1) {
					filesInFolder = files;
					updateValuesForFile(
							filesInFolder[filesInFolder.length - 1]);
				}
				return;
			}
		}
	}

	public void updateValuesForFile(File f) {
		Debug.println("updating values for file " + f);
		TWMComputer exc = new TWMComputer(f);
		Measurement m = exc.call();
		if (m.temperature == null || m.temperature.isEmpty()) {
			signalLevelLabel.setText("Температура неизвестна");
			temperatureLabel.setText("Температура неизвестна");
		} else {
			signalLevelLabel.setText(String.format("%+.3f мВ",
					m.temperature.get(0).signalLevel * 1000));
			temperatureLabel.setText(
					String.format("%+.0f K", m.temperature.get(0).value));
		}
		List<Diffusivity> tConds = m.diffusivity;
		if (tConds.size() != tCondPanels.size()) {
			Debug.println("Sizes differ");
			for (JTDiffLabelSet set : tCondPanels) {
				numbersContainer.remove(set);
			}
			tCondPanels.clear();
			for (int i = 0; i < tConds.size(); i++) {
				JTDiffLabelSet set = new JTDiffLabelSet(i);
				tCondPanels.add(set);
				numbersContainer.add(set);
			}
		}
		for (int i = 0; i < tConds.size(); i++) {
			Diffusivity tCond = tConds.get(i);
			tCondPanels.get(i).updateValues(tCond);
		}
		measurementViewer.addMeasurement(m);

	}

	boolean isClosing = false;

	@Override
	public void run() {
		while (this.isDisplayable()) {
			if (Thread.interrupted()) {
				return;
			}
			if (isClosing)
				return;
			checkNewFile();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
