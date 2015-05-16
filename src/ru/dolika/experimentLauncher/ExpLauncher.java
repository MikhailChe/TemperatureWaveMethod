package ru.dolika.experimentLauncher;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
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
					BatcherLaunch.main(null);
				}
			}).start();
		}

	}
}
