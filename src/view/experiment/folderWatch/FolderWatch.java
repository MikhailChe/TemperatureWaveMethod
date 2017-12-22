package view.experiment.folderWatch;

import static debug.Debug.println;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.experiment.Analyzer.TWMComputer;
import model.experiment.measurement.Diffusivity;
import model.experiment.measurement.Measurement;
import model.experiment.workspace.Workspace;
import view.MemorableDirectoryChooser;
import view.measurements.MeasurementViewer;

public class FolderWatch extends JDialog implements Runnable {
	private static final long serialVersionUID = 1831549678406783975L;

	// private Workspace workspace;

	final public Set<File> filesInFolder = new HashSet<>();
	public File folder;

	final MeasurementViewer measurementViewer = new MeasurementViewer();
	final Container numbersContainer = new Container();

	final JPanel signalLevelPanel = new JPanel();
	final JLabel signalLevelLabel = new JLabel("Здесь будет термоЭДС");

	final JPanel temperaturePanel = new JPanel();
	final JLabel temperatureLabel = new JLabel("Здесь будет температура");

	final List<JTDiffLabelSet> tCondPanels = new ArrayList<>();

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
		return new FolderWatch(parent, folder);
	}

	private Thread updater;

	private FolderWatch(JFrame parent, File folder) {
		super(parent, false);
		setName("Онлайн." + folder.getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		println("Вызван конструктор FolderWatch. " + folder);
		this.folder = folder;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				println(e);
				isClosing = true;
			}
		});

		SwingUtilities.invokeLater(() -> {

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

			this.getContentPane().setLayout(new BorderLayout(16, 16));
			this.getContentPane().add(numbersContainer, NORTH);
			this.getContentPane().add(measurementViewer);
			SwingUtilities.invokeLater(this::pack);

			updater = new Thread(this);
			updater.setDaemon(true);
			updater.start();
		});
	}

	public void checkNewFile() {
		List<File> files = new ArrayList<>(
				Arrays.asList(
						folder.listFiles(pathname -> pathname.getName().matches("^[0-9]+.txt$"))));
		files.removeAll(filesInFolder);

		if (!files.isEmpty()) {
			files.stream().parallel().forEach(f -> {
				if (!isClosing)
					updateValuesForFile(f);
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
			List<Diffusivity> tConds = m.diffusivity;
			if (tConds.size() != tCondPanels.size()) {
				println("Sizes differ");
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
		});
	}

	boolean isClosing = false;

	@Override
	public void run() {
		while (!isClosing) {
			if (Thread.interrupted()) {
				return;
			}
			checkNewFile();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				println(e.getLocalizedMessage());
				return;
			}
		}
	}
}
