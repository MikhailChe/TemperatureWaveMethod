package ru.dolika.experiment.sample;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public class SampleSettingsDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6024215129687729185L;
	Sample sample = null;
	JTextField name = new JTextField();
	JTextField comment = new JTextField();
	JFormattedTextField length = null;

	JButton okButton = new JButton("OK");

	public SampleSettingsDialog(JFrame parent, Sample sample) {
		super(parent, true);
		if (sample == null) {
			throw new IllegalArgumentException();
		}
		this.sample = sample;

		NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
		format.setMinimumFractionDigits(3);
		format.setMaximumFractionDigits(6);
		format.setMinimumIntegerDigits(0);
		format.setMaximumIntegerDigits(0);

		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		length = new JFormattedTextField(formatter);

		Container pane = this.getContentPane();
		BoxLayout layout = new BoxLayout(pane, BoxLayout.Y_AXIS);

		pane.setLayout(layout);
		pane.setMinimumSize(new Dimension(400, 200));
		pane.setPreferredSize(new Dimension(400, 200));

		JPanel namePanel = new JPanel(new BorderLayout(8, 8));
		namePanel.setBorder(BorderFactory.createTitledBorder("Название образца"));
		namePanel.add(name);

		JPanel commentPanel = new JPanel(new BorderLayout(8, 8));
		commentPanel.setBorder(BorderFactory.createTitledBorder("Комментарий"));
		commentPanel.add(comment);

		JPanel lengthPanel = new JPanel(new BorderLayout(8, 8));
		lengthPanel.setBorder(BorderFactory.createTitledBorder("Толщина образца (метры)"));
		lengthPanel.add(length);

		pane.add(namePanel);
		pane.add(commentPanel);
		pane.add(lengthPanel);

		pane.add(okButton);

		pack();
		setResizable(false);

		setLocationRelativeTo(parent);

		name.setText(sample.name);
		comment.setText(sample.comments);
		length.setValue(sample.length);

		okButton.addActionListener(e -> {
			sample.name = name.getText();
			sample.comments = comment.getText();
			try {
				length.commitEdit();
				Object o = length.getValue();
				if (o instanceof Double) {
					Double val = (Double) o;
					sample.length = val.doubleValue();
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			status = OK_BUTTON;
			setVisible(false);
			dispose();
		});
	}

	private int status = CANCEL_BUTTON;

	public static final int OK_BUTTON = 0;
	public static final int CANCEL_BUTTON = 1;

	public static int showSampleSettings(JFrame parent, Sample sample) {
		SampleSettingsDialog d = new SampleSettingsDialog(parent, sample);
		d.setVisible(true);
		d.dispose();
		return d.status;
	}

}
