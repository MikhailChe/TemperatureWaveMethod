package controller.experiment.Analyzer.Angstrem;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class AngstremAnalyzer extends JFrame {

	private static final long serialVersionUID = -3723796085767048840L;

	public AngstremAnalyzer() {
		super("Анализатор температуропроводности по методу Ангстрема");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(new AngstremCombinedPlane());
		pack();
		setVisible(true);
	}

}
