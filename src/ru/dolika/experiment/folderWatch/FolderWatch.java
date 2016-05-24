package ru.dolika.experiment.folderWatch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ru.dolika.experiment.Analyzer.ExperimentComputer;
import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.MeasurementViewer;
import ru.dolika.experiment.measurement.TemperatureConductivity;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.ui.MemorableDirectoryChooser;

public class FolderWatch extends JDialog implements Runnable, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1831549678406783975L;

	private Workspace workspace;

	public File[] filesInFolder;
	public File folder;

	MeasurementViewer measurementViewer = new MeasurementViewer();

	JPanel signalLevelPanel = new JPanel();
	JLabel signalLevelLabel = new JLabel("Здесь будет термоЭДС");

	JPanel temperaturePanel = new JPanel();
	JLabel temperatureLabel = new JLabel("Здесь будет температура");

	ArrayList<JTDiffLabelSet> tCondPanels = new ArrayList<JTDiffLabelSet>();

	public FolderWatch(JFrame parent) throws Exception {
		super(parent, false);
		this.setTitle("Я смотрю за тобой!");
		this.workspace = Workspace.getInstance();
		if (workspace.getSample() == null) {
			JOptionPane.showMessageDialog(parent,
					"Не был выбран файл образца.\nПожалуйста закройте это окно и выберите образец или создайте новый",
					"Ошибка образца", JOptionPane.ERROR_MESSAGE);
			throw new Exception("No sample file chosen");
		}
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(this.getClass());
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Выберите папку с данными");

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			fileChooser.saveCurrentSelection();
			folder = fileChooser.getSelectedFile();
			if (folder == null) {
				this.setVisible(false);
				this.dispose();
				throw new Exception();
			}
		} else {
			this.setVisible(false);
			this.dispose();
			throw new Exception();
		}

		this.setTitle("Я смотрю за " + folder.getAbsolutePath());

		signalLevelPanel.setBorder(BorderFactory.createTitledBorder("Уровень сигнала"));
		signalLevelPanel.add(signalLevelLabel);

		temperaturePanel.setBorder(BorderFactory.createTitledBorder("Температура"));
		temperaturePanel.add(temperatureLabel);

		Container numbersContainer = new Container();

		numbersContainer.setLayout(new GridLayout(3, 2, 16, 16));

		numbersContainer.add(signalLevelPanel);
		numbersContainer.add(temperaturePanel);

		this.getContentPane().setLayout(new BorderLayout(16, 16));

		this.getContentPane().add(numbersContainer, BorderLayout.NORTH);

		this.getContentPane().add(measurementViewer);

		this.pack();

		new Thread(this).start();

	}

	public void checkNewFile() {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches("^[0-9]+.txt$");
			}
		});
		if (files != null) {
			if (filesInFolder == null) {
				if (files.length >= 1) {
					filesInFolder = files;
					for (File f : filesInFolder) {
						updateValuesForFile(f);
					}
				}
				return;
			}

			if (filesInFolder.length != files.length) {
				if (files.length >= 1) {
					filesInFolder = files;
					updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
				}
				return;
			}
		}
	}

	public void updateValuesForFile(File f) {
		ExperimentComputer exc = new ExperimentComputer(f, workspace);
		Measurement m = exc.call();
		if (m.temperature == null || m.temperature.isEmpty()) {
			signalLevelLabel.setText("Температура неизвестна");
			temperatureLabel.setText("Температура неизвестна");
		} else {
			signalLevelLabel.setText(String.format("%+.3f мВ", m.temperature.get(0).signalLevel * 1000));
			temperatureLabel.setText(String.format("%+.0f K", m.temperature.get(0).value));
		}
		ArrayList<TemperatureConductivity> tConds = m.tCond;
		if (tConds.size() != tCondPanels.size()) {
			for (JTDiffLabelSet set : tCondPanels) {
				this.remove(set);
			}
			tCondPanels.clear();
			for (int i = 0; i < tConds.size(); i++) {
				JTDiffLabelSet set = new JTDiffLabelSet(i);
				tCondPanels.add(set);
			}
		}
		for (int i = 0; i < tConds.size(); i++) {
			TemperatureConductivity tCond = tConds.get(i);
			tCondPanels.get(i).updateValues(tCond);
		}
		measurementViewer.addMeasurement(m);

	}

	boolean isClosing = false;

	@Override
	public void run() {
		while (this.isDisplayable()) {
			if (isClosing)
				break;
			checkNewFile();

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		isClosing = true;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		isClosing = true;

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
