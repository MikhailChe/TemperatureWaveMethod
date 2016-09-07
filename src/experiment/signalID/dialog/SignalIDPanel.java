package experiment.signalID.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import experiment.signalID.SignalIdentifier;

public class SignalIDPanel extends JPanel {
	private static final long serialVersionUID = 64L;
	SignalIdentifier signalIdentifier;

	protected JTextField fileNameField;
	protected JButton fileOpenButtton;
	protected JPanel channelInfoPanel;

	protected JButton deleteButton;

	public SignalIDPanel(SignalIdentifier id) {
		super();
		setBorder(new CompoundBorder(new EmptyBorder(16, 16, 16, 16),
				new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
						"Канал без сигнала", TitledBorder.LEADING, TitledBorder.TOP, null,
						new Color(0, 0, 0))));

		setToolTipText("Канал");
		setLayout(new BorderLayout(16, 16));
		Dimension size = new Dimension(800, 128);
		setPreferredSize(size);
		setMaximumSize(size);

		JPanel upDownPanel = new JPanel();
		add(upDownPanel, BorderLayout.WEST);
		upDownPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JButton upButton = new JButton("Вверх");
		upDownPanel.add(upButton);

		JButton downButton = new JButton("Вниз");
		upDownPanel.add(downButton);

		JPanel editDeletePanel = new JPanel();
		add(editDeletePanel, BorderLayout.EAST);
		editDeletePanel.setLayout(new GridLayout(0, 1, 0, 0));

		JButton editButton = new JButton("Редактировать");
		editButton.setBackground(Color.ORANGE);
		editDeletePanel.add(editButton);

		deleteButton = new JButton("Удалить");
		deleteButton.setBackground(Color.RED);
		editDeletePanel.add(deleteButton);

		channelInfoPanel = new JPanel();
		add(channelInfoPanel, BorderLayout.CENTER);
		channelInfoPanel.setLayout(new BoxLayout(channelInfoPanel, BoxLayout.LINE_AXIS));

		JPanel adjGradFilePanel = new JPanel();
		channelInfoPanel.add(adjGradFilePanel);
		adjGradFilePanel.setLayout(new BorderLayout(0, 0));

		fileNameField = new JTextField();
		fileNameField.setEditable(false);
		fileNameField.setText("file.");
		fileNameField.setHorizontalAlignment(SwingConstants.LEFT);
		adjGradFilePanel.add(fileNameField, BorderLayout.CENTER);
		fileNameField.setColumns(10);

		fileOpenButtton = new JButton("Обзор");
		adjGradFilePanel.add(fileOpenButtton, BorderLayout.EAST);

	}

}
