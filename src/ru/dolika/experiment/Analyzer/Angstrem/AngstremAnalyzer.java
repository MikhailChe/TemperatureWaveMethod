package ru.dolika.experiment.Analyzer.Angstrem;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class AngstremAnalyzer extends JFrame {

	private static final long serialVersionUID = -3723796085767048840L;

	public static void main(String[] args) {

		new AngstremAnalyzer();

	}

	public AngstremAnalyzer() {
		super("���������� ������ �� ������ ���������");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(new AngstremCombinedPlane());
		pack();
		setVisible(true);
	}

}
