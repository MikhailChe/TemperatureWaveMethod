package view.experiment.folderWatch;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import model.measurement.Diffusivity;

public class JTDiffLabelSet extends JPanel {

    private static final long serialVersionUID = -3740896947551054147L;

    final private JPanel argumentPanel = new JPanel();
    final private JLabel argumentLabel = new JLabel("Здесь будет угол");

    final private JPanel kappaPanel = new JPanel();
    final private JLabel kappaLabel = new JLabel("Здесь будет каппа");

    final private JPanel amplitudePanel = new JPanel();
    final private JLabel amplitudeLabel = new JLabel("Здесь будет амплитуда сигнала");

    final private JPanel diffusivityPanel = new JPanel();
    final private JLabel diffusivityLabel = new JLabel("Здесь будет температуропроводность");

    TitledBorder border = new TitledBorder("Канал неизвестный");

    private int channelNumber;    
    public JTDiffLabelSet(int channelNumber) {
	super(new GridLayout(2, 2));
	this.channelNumber = channelNumber;
	border.setTitle("Канал №" + channelNumber);
	
	
	argumentPanel.setBorder(BorderFactory.createTitledBorder("Фаза"));
	argumentPanel.add(argumentLabel);

	kappaPanel.setBorder(BorderFactory.createTitledBorder("kappa"));
	kappaPanel.add(kappaLabel);

	amplitudePanel.setBorder(BorderFactory.createTitledBorder("Амплитуда сигнала"));
	amplitudePanel.add(amplitudeLabel);

	diffusivityPanel.setBorder(BorderFactory.createTitledBorder("Температуропроводность"));
	diffusivityPanel.add(diffusivityLabel);

	
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

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + channelNumber;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	JTDiffLabelSet other = (JTDiffLabelSet) obj;
	if (channelNumber != other.channelNumber)
	    return false;
	return true;
    }
}
