package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import ru.dolika.experimentAnalyzer.BatcherLaunch;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;

	ExpLauncher() {
		super("Launcher");

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("����");
		menuBar.add(fileMenu);
		JMenu fileOpen = new JMenu("�������");
		fileMenu.add(fileOpen);

		JMenuItem fileOpenProject = new JMenuItem("������...");
		fileOpen.add(fileOpenProject);
		this.setJMenuBar(menuBar);

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
