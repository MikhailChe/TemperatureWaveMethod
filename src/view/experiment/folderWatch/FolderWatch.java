package view.experiment.folderWatch;

import static debug.Debug.println;
import static java.awt.BorderLayout.NORTH;
import static java.util.Arrays.asList;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import controller.experiment.analyzer.TWMComputer;
import model.measurement.Diffusivity;
import model.measurement.Measurement;
import model.workspace.Workspace;
import view.MemorableDirectoryChooser;
import view.measurements.MeasurementViewer;

public class FolderWatch extends JInternalFrame implements Runnable {
	private static final long serialVersionUID = 1831549678406783975L;

	final public Set<File> filesInFolder = new HashSet<>();
	public File folder;

	final MeasurementViewer measurementViewer = new MeasurementViewer();
	final JComponent numbersContainer = new JPanel();
	final JComponent labelSetContainer = new JPanel();

	final JPanel signalLevelPanel = new JPanel();
	final JLabel signalLevelLabel = new JLabel("Здесь будет термоЭДС");

	final JPanel temperaturePanel = new JPanel();
	final JLabel temperatureLabel = new JLabel("Здесь будет температура");

	final Map<Integer, JTDiffLabelSet> tDiffusPanels = new HashMap<>();

	public static FolderWatch factory(JFrame parent) {
		Workspace workspace = Workspace.getInstance();
		if (workspace.getSample() == null) {
			JOptionPane.showMessageDialog(parent,
					"Не был выбран файл образца.\n"
							+ "Пожалуйста закройте это окно и выберите образец или создайте новый",
					"Ошибка образца", ERROR_MESSAGE);
			return null;
		}
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(FolderWatch.class);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Выберите папку с данными");

		File folder;
		if (fileChooser.showOpenDialog(parent) == APPROVE_OPTION) {
			folder = fileChooser.getSelectedFile();
			if (folder == null || !folder.exists()) {
				return null;
			}
		} else {
			return null;
		}
		println("Returning new FolderWatch");
		return new FolderWatch(folder);
	}

	private Thread updater;

	private FolderWatch(File folder) {
		super("Онлайн." + folder.getName(), true, true, true, true);

		addMenu();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		println("Вызван конструктор FolderWatch. " + folder);
		this.folder = folder;
		addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				println(e);
				isClosing = true;
			}

		});

		temperatureLabel.setFont(temperatureLabel.getFont().deriveFont(temperatureLabel.getFont().getSize() * 2f));
		signalLevelLabel.setFont(signalLevelLabel.getFont().deriveFont(signalLevelLabel.getFont().getSize() * 2f));

		this.setTitle("Я смотрю за " + folder.getAbsolutePath());
		signalLevelPanel.setBorder(createTitledBorder("Уровень сигнала"));
		signalLevelPanel.add(signalLevelLabel);
		temperaturePanel.setBorder(createTitledBorder("Температура"));
		temperaturePanel.add(temperatureLabel);

		numbersContainer.setLayout(new GridLayout(0, 2, 16, 16));

		numbersContainer.add(signalLevelPanel);
		numbersContainer.add(temperaturePanel);

		labelSetContainer.setLayout(new GridLayout(0, 2, 16, 16));

		Container labelsCommon = new Container();
		labelsCommon.setLayout(new BoxLayout(labelsCommon, Y_AXIS));

		labelsCommon.add(numbersContainer);
		labelsCommon.add(labelSetContainer);

		this.getContentPane().setLayout(new BorderLayout(16, 16));
		this.getContentPane().add(labelsCommon, NORTH);
		this.getContentPane().add(measurementViewer);
		SwingUtilities.invokeLater(this::pack);
		SwingUtilities.invokeLater(() -> {
			try {
				this.setMaximum(true);
			} catch (@SuppressWarnings("unused") PropertyVetoException ignore) {
				////
			}
		});

		updater = new Thread(this);
		updater.setDaemon(true);
		updater.start();

	}

	private void addMenu() {
		JMenuBar menubar = new JMenuBar();
		final Icon saveIcon = UIManager.getIcon("FileView.floppyDriveIcon");
		// final Icon openIcon = UIManager.getIcon("FileView.fileIcon");

		JMenuItem save = new JMenuItem("Сохранить", saveIcon);
		save.setIcon(saveIcon);
		save.addActionListener((event) -> {
			try {
				TWMComputer.saveToDirectory(measurementViewer.dataset.getMeasurements(), folder);
			} catch (IOException e) {
				int option = JOptionPane.showConfirmDialog(null, e.getLocalizedMessage() + "\r\nВыбрать новый путь?",
						"Ошибка", JOptionPane.YES_NO_OPTION);
				if ((option & JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
					MemorableDirectoryChooser mdc = new MemorableDirectoryChooser(FolderWatch.class);
					mdc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					mdc.showSaveDialog(null);

					if (mdc.getSelectedFile() != null) {
						try {
							TWMComputer.saveToDirectory(measurementViewer.dataset.getMeasurements(), mdc.getSelectedFile());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}

		});

		menubar.add(save);

		// JMenuItem open = new JMenuItem("Открыть", openIcon);
		// open.addActionListener((event) -> {
		// TWMComputer.saveToFile(measurementViewer.dataset.getMeasurements(), folder);
		//
		// });
		// menubar.add(open);

		this.setJMenuBar(menubar);
	}

	public void checkNewFilesAndUpdate() {

		List<File> files;
		try {
			files = new ArrayList<>(asList(folder.listFiles(pathname -> pathname.getName().matches("^[0-9]+.txt$"))));
		} catch (@SuppressWarnings("unused") NullPointerException e) {
			return;
		}
		files.removeAll(filesInFolder);

		if (!files.isEmpty()) {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			files.parallelStream().forEach(f -> {
				if (!isClosing)
					try {
						updateValuesForFile(f);
					} catch (Exception e) {
						System.err.println(
								"Faile to read diffusivity for file: " + f + "\r\n\t" + e.getLocalizedMessage());
					}
			});
			filesInFolder.addAll(files);
		}
	}

	public void updateValuesForFile(File f) {

		println("Считываю значения из файла " + f);

		Measurement m = new TWMComputer(f).call();

		SwingUtilities.invokeLater(() -> {
			if (m.temperature == null || m.temperature.isEmpty()) {
				signalLevelLabel.setText("Температура неизвестна");
				temperatureLabel.setText("Температура неизвестна");
			} else {
				signalLevelLabel.setText(String.format("%+.3f мВ", m.temperature.get(0).signalLevel * 1000));
				temperatureLabel.setText(String.format("%+.0f K", m.temperature.get(0).value));
			}

			List<Diffusivity> tDiffuss = m.diffusivity;
			Set<JTDiffLabelSet> updatedLabels = new HashSet<>(tDiffuss.size());
			for (int i = 0; i < tDiffuss.size(); i++) {
				Diffusivity tDiffus = tDiffuss.get(i);

				JTDiffLabelSet labelsSet = tDiffusPanels.computeIfAbsent(tDiffus.channelNumber, key -> {
					JTDiffLabelSet ls = new JTDiffLabelSet(key);
					labelSetContainer.add(ls);
					return ls;
				});
				labelsSet.updateValues(tDiffus);
				updatedLabels.add(labelsSet);
			}
			tDiffusPanels.values().stream().filter(val -> !updatedLabels.contains(val))
					.forEach(panel -> panel.updateValues(null));

			measurementViewer.addMeasurement(m);
		});
	}

	boolean isClosing = false;

	@Override
	public void run() {
		while (!isClosing) {
			while (isIcon()) {
				Thread.yield();
				if (isClosing)
					return;
			}
			if (Thread.interrupted())
				return;

			checkNewFilesAndUpdate();
			try {
				java.util.concurrent.TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				println(e.getLocalizedMessage());
				return;
			}
		}
	}
}
