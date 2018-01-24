package view.experiment.folderWatch;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
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

    TitledBorder border = new TitledBorder("Канал неизвестный");

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

	border.setTitle("Канал №" + channelNumber);

	setBorder(border);
	add(argumentPanel);
	add(kappaPanel);
	add(amplitudePanel);
	add(diffusivityPanel);
    }

    public void updateValues(Diffusivity tDiffus) {
	if (tDiffus == null) {
	    argumentLabel.setText("Фаза неизвестна");
	    kappaLabel.setText("kappa неизвестна");
	    amplitudeLabel.setText("Амплитуда неизвестна");
	    diffusivityLabel.setText("Температуропроводность неизвестна");
	} else {
	    argumentLabel.setText(String.format("%+.3f", tDiffus.phase));
	    kappaLabel.setText(String.format("%+.3f", tDiffus.kappa));
	    amplitudeLabel.setText(String.format("%.0f", tDiffus.amplitude));
	    diffusivityLabel.setText(String.format("%.3e", tDiffus.diffusivity));
	    border.setTitle("Канал №" + tDiffus.channelNumber);
	}
    }
}
