package view.experiment.folderWatch;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import model.experiment.measurement.Diffusivity;

public class JTDiffLabelSet extends JPanel {

	private static final long serialVersionUID = -3740896947551054147L;

	JPanel argumentPanel = new JPanel();
	JLabel argumentLabel = new JLabel("Здесь будет угол");

	JPanel kappaPanel = new JPanel();
	JLabel kappaLabel = new JLabel("Здесь будет каппа");

	JPanel amplitudePanel = new JPanel();
	JLabel amplitudeLabel = new JLabel("Здесь будет амплитуда сигнала");

	JPanel diffusivityPanel = new JPanel();
	JLabel diffusivityLabel = new JLabel("Здесь будет температуропроводность");

	public JTDiffLabelSet(int channelNumber) {
		super(new GridLayout(2, 2));

		argumentPanel.setBorder(BorderFactory.createTitledBorder("Фаза"));
		argumentPanel.add(argumentLabel);

		kappaPanel.setBorder(BorderFactory.createTitledBorder("kappa"));
		kappaPanel.add(kappaLabel);

		amplitudePanel.setBorder(BorderFactory.createTitledBorder("Амплитуда сигнала"));
		amplitudePanel.add(amplitudeLabel);

		diffusivityPanel.setBorder(BorderFactory.createTitledBorder("Температуропроводность"));
		diffusivityPanel.add(diffusivityLabel);

		setBorder(new TitledBorder("Канал " + channelNumber));
		add(argumentPanel);
		add(kappaPanel);
		add(amplitudePanel);
		add(diffusivityPanel);
	}

	public void updateValues(Diffusivity tCond) {
		if (tCond == null) {
			argumentLabel.setText("Фаза неизвестна");
			kappaLabel.setText("kappa неизвестна");
			amplitudeLabel.setText("Амплитуда неизвестна");
			diffusivityLabel.setText("Температуропроводность неизвестна");
		} else {
			argumentLabel.setText(String.format("%+.3f", tCond.phase));
			kappaLabel.setText(String.format("%+.3f", tCond.kappa));
			amplitudeLabel.setText(String.format("%.0f", tCond.amplitude));
			diffusivityLabel.setText(String.format("%.3e", tCond.tCond));
		}
	}
}