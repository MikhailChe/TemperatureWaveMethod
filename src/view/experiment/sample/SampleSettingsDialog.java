package view.experiment.sample;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.NumberFormatter;

import model.sample.Sample;

public class SampleSettingsDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6024215129687729185L;
	Sample sample = null;
	JTextField name = new JTextField();
	JTextField comment = new JTextField();
	JFormattedTextField length = null;
	NumberFormatter lengthFormatter = null;
	JFormattedTextField density = null;

	JButton okButton = new JButton("OK");

	public SampleSettingsDialog(JFrame parent, Sample sample) {
		super(parent, true);
		if (sample == null) {
			throw new IllegalArgumentException();
		}
		this.sample = sample;

		NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
		format.setGroupingUsed(true);
		format.setMaximumFractionDigits(6);

		format.setRoundingMode(RoundingMode.HALF_EVEN);
		format.setParseIntegerOnly(false);

		lengthFormatter = new NumberFormatter(format);
		lengthFormatter.setAllowsInvalid(true);
		lengthFormatter.setCommitsOnValidEdit(false);

		length = new JFormattedTextField(lengthFormatter);
		length.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

		density = new JFormattedTextField(NumberFormat.getNumberInstance());
		density.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

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

		JPanel densityPanel = new JPanel(new BorderLayout(8, 8));
		densityPanel.setBorder(BorderFactory.createTitledBorder("Плотность образца"));
		densityPanel.add(density);

		pane.add(namePanel);
		pane.add(commentPanel);
		pane.add(lengthPanel);
		pane.add(densityPanel);

		pane.add(okButton);

		pack();
		setResizable(false);

		setLocationRelativeTo(parent);

		name.setText(sample.getName());
		comment.setText(sample.getComment());
		length.setValue(sample.getLength());
		density.setValue(sample.getDensity());

		okButton.addActionListener(e -> {
			sample.setName(name.getText());
			sample.setComment(comment.getText());
			try {
				System.out.println("ALlow invalid?" + lengthFormatter.getAllowsInvalid());
				System.out.println(lengthFormatter.stringToValue(length.getText()));
				length.commitEdit();
				Object o = length.getValue();
				if (o instanceof Number) {
					sample.setLength(((Number) o).doubleValue());
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
				return;
			}
			try {
				density.commitEdit();
				Object o = density.getValue();
				if (o instanceof Number) {
					sample.setDensity(((Number) o).doubleValue());
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
				return;
			}

			status = OK_BUTTON;
			setVisible(false);
			dispose();
		});
		getRootPane().setDefaultButton(okButton);

		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");

		Action actionListener = new AbstractAction("ESCAPE") {

			private static final long serialVersionUID = 6195750866228672072L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", actionListener);

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
