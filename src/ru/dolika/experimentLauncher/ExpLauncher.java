package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experimentAnalyzer.BatcherLaunch;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;
	Sample currentSample = null;
	File sampleFile = null;

	JFileChooser fileChooser = null;

	ExpLauncher() {
		super("Launcher");

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("����");
		menuBar.add(fileMenu);
		JMenu fileOpen = new JMenu("�������");
		fileMenu.add(fileOpen);
		fileChooser = new JFileChooser();
		JMenuItem fileOpenProject = new JMenuItem("������...");
		fileOpenProject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentSample != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(ExpLauncher.this,
							"���� ������� �� ��� ��������.\n������ ��������� ��� ����� ��������� ������?",
							"�� ������ �����������", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						currentSample = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						saveSample();
						currentSample = null;
						System.gc();
					}
				}
				if (currentSample == null) {
					fileChooser.setDialogTitle("�������...");
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(false);
					int option = fileChooser.showOpenDialog(ExpLauncher.this);
					if (option == JFileChooser.APPROVE_OPTION) {
						if (fileChooser.getSelectedFile() != null) {
							SampleFactory.forBinary(fileChooser.getSelectedFile().getAbsolutePath());
							sampleFile = fileChooser.getSelectedFile();
						}
					}
				}
			}
		});
		fileOpen.add(fileOpenProject);

		JMenu fileSave = new JMenu("���������...");
		fileMenu.add(fileSave);

		JMenu fileSaveAs = new JMenu("��������� ���...");
		fileMenu.add(fileSaveAs);

		JMenu toolsMenu = new JMenu("�����������");
		menuBar.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem("����������� ���������");
		toolsMenu.add(prepareZeroCrossing);
		JMenuItem prepareGrads = new JMenuItem("����������� �����������");
		toolsMenu.add(prepareGrads);

		JMenu settingsMenu = new JMenu("���������");
		menuBar.add(settingsMenu);
		JMenuItem chooseChannels = new JMenuItem("������� ������");
		settingsMenu.add(chooseChannels);
		JMenuItem sampleSettings = new JMenuItem("��������� �������");
		settingsMenu.add(sampleSettings);

		setLayout(new BorderLayout(16, 16));

		b = new Button("Push me harder");
		add(b);
		b.addActionListener(new BAC());

		pack();
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
	}

	public void saveSampleAs() {
		File sf = sampleFile;
		sampleFile = null;
		saveSample();
		sampleFile = sf;
	}

	public void saveSample() {
		if (currentSample == null) {
			return;
		}
		if (sampleFile == null)

		{
			fileChooser.setDialogTitle("��������� ���...");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			int option = fileChooser.showSaveDialog(ExpLauncher.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					if (fileChooser.getSelectedFile().exists()) {
						int confirmer = JOptionPane.showConfirmDialog(ExpLauncher.this,
								"���� ��� ����������.\n�� ������ ������������ ���?");
						if (confirmer == JOptionPane.YES_OPTION || confirmer == JOptionPane.OK_OPTION) {
							SampleFactory.saveSample(fileChooser.getSelectedFile().getAbsolutePath(), currentSample);
							currentSample = null;
							sampleFile = null;
						}
					}
				}
			}
		} else

		{
			SampleFactory.saveSample(sampleFile.getAbsolutePath(), currentSample);
			currentSample = null;
			sampleFile = null;
		}
	}

	class BAC implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent evt) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					new BatcherLaunch();
				}
			}).start();
		}

	}
}
