package ru.dolika.experiment.folderWatch;

import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ru.dolika.experiment.Analyzer.ExperimentComputer;
import ru.dolika.experiment.measurement.Measurement;
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

	JPanel signalLevelPanel = new JPanel();
	JLabel signalLevelLabel = new JLabel("Здесь будет термоЭДС");

	JPanel temperaturePanel = new JPanel();
	JLabel temperatureLabel = new JLabel("Здесь будет температура");

	JPanel argumentPanel = new JPanel();
	JLabel argumentLabel = new JLabel("Здесь будет угол");

	JPanel kappaPanel = new JPanel();
	JLabel kappaLabel = new JLabel("Здесь будет каппа");

	JPanel amplitudePanel = new JPanel();
	JLabel amplitudeLabel = new JLabel("Здесь будет амплитуда сигнала");

	JPanel diffusivityPanel = new JPanel();
	JLabel diffusivityLabel = new JLabel("Здесь будет температуропроводность");

	public FolderWatch(JFrame parent, Workspace workspace) throws Exception {
		super(parent, false);
		this.setTitle("Я смотрю за тобой!");
		this.workspace = workspace;
		if (workspace.sample == null) {
			JOptionPane
					.showMessageDialog(
							parent,
							"Не был выбран файл образца.\nПожалуйста закройте это окно и выберите образец или создайте новый",
							"Ошибка образца", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		}
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(
				this.getClass());
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Выберите папку с данными");

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
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

		signalLevelPanel.setBorder(BorderFactory
				.createTitledBorder("Уровень сигнала"));
		signalLevelPanel.add(signalLevelLabel);

		temperaturePanel.setBorder(BorderFactory
				.createTitledBorder("Температура"));
		temperaturePanel.add(temperatureLabel);

		argumentPanel.setBorder(BorderFactory.createTitledBorder("Фаза"));
		argumentPanel.add(argumentLabel);

		kappaPanel.setBorder(BorderFactory.createTitledBorder("kappa"));
		kappaPanel.add(kappaLabel);

		amplitudePanel.setBorder(BorderFactory
				.createTitledBorder("Амплитуда сигнала"));
		amplitudePanel.add(amplitudeLabel);

		diffusivityPanel.setBorder(BorderFactory
				.createTitledBorder("Температуропроводность"));
		diffusivityPanel.add(diffusivityLabel);

		this.getContentPane().setLayout(new GridLayout(3, 2, 16, 16));

		this.getContentPane().add(signalLevelPanel);
		this.getContentPane().add(temperaturePanel);
		this.getContentPane().add(argumentPanel);
		this.getContentPane().add(kappaPanel);
		this.getContentPane().add(amplitudePanel);
		this.getContentPane().add(diffusivityPanel);

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

		if (filesInFolder == null) {
			filesInFolder = files;
			updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
			return;
		}

		if (filesInFolder.length != files.length) {
			filesInFolder = files;
			updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
			return;
		}
	}

	public void updateValuesForFile(File f) {
		ExperimentComputer exc = new ExperimentComputer(f, workspace);
		Measurement m = exc.call();
		if (m.temperature == null || m.temperature.isEmpty()) {
			signalLevelLabel.setText("Температура неизвестна");
			temperatureLabel.setText("Температура неизвестна");
		} else {
			signalLevelLabel.setText(String.format("%+.3f мВ",
					m.temperature.get(0).signalLevel * 1000));
			temperatureLabel.setText(String.format("%+.0f K",
					m.temperature.get(0).value));
		}
		if (m.tCond == null || m.tCond.isEmpty()) {
			argumentLabel.setText("Фаза неизвестна");
			kappaLabel.setText("kappa неизвестна");
			amplitudeLabel.setText("Амплитуда неизвестна");
			diffusivityLabel.setText("Температуропроводность неизвестна");

		} else {
			argumentLabel.setText(String.format("%+.3f", m.tCond.get(0).phase));
			kappaLabel.setText(String.format("%+.3f", m.tCond.get(0).kappa));
			amplitudeLabel.setText(String.format("%.0f",
					m.tCond.get(0).amplitude));
			diffusivityLabel
					.setText(String.format("%.3e", m.tCond.get(0).tCond));
		}
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
		// TODO Auto-generated method stub

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
