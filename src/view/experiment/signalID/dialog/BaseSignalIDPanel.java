package view.experiment.signalID.dialog;

import static debug.Debug.println;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import debug.JExceptionHandler;
import model.phaseAdjust.PhaseAdjust;
import model.phaseAdjust.PhaseAdjustFactory;
import model.signalID.BaseSignalID;
import view.MemorableDirectoryChooser;
import view.experiment.phaseAdjust.PhaseAdjustViewerPanel;

public class BaseSignalIDPanel extends SignalIDPanel {
	private static final long serialVersionUID = 7193781950090874574L;

	PhaseAdjustViewerPanel phaseAdjustPanel;

	public BaseSignalIDPanel(BaseSignalID id) {
		super();
		setBorder(new CompoundBorder(new EmptyBorder(16, 16, 16, 16),
				new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
						"Канал переменного сигнала", TitledBorder.LEADING, TitledBorder.TOP, null,
						new Color(0, 0, 0))));
		if (id.phaseAdjust != null)
			this.fileNameField.setText(id.phaseAdjust.forFile.toString());
		this.fileOpenButtton.addActionListener(e -> {
			MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(DCSignalIDPanel.class);
			chooser.setDialogTitle("Выберите файл юстировки");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			chooser.addChoosableFileFilter(PhaseAdjust.extensionFilter);
			chooser.setFileFilter(PhaseAdjust.extensionFilter);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				chooser.saveCurrentSelection();
				File file = chooser.getSelectedFile();
				PhaseAdjust newzc = PhaseAdjustFactory.forFile(file);
				id.phaseAdjust = newzc;
				phaseAdjustPanel.setZeroCrossing(id.phaseAdjust);
				this.fileNameField.setText(id.phaseAdjust.forFile.toString());
			}
		});

		phaseAdjustPanel = new PhaseAdjustViewerPanel(id.phaseAdjust);
		phaseAdjustPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				println(e);
				try {
					JDialog zcDialog = new JDialog((JDialog) SwingUtilities.getWindowAncestor(BaseSignalIDPanel.this),
							id.phaseAdjust.forFile.toString(), true);
					zcDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					zcDialog.getContentPane().add(new PhaseAdjustViewerPanel(id.phaseAdjust));
					zcDialog.pack();
					zcDialog.setSize(640, 480);
					zcDialog.setVisible(true);
				} catch (NullPointerException exc) {
					JExceptionHandler.showException(exc);
				}
			}
		});
		this.channelInfoPanel.add(phaseAdjustPanel);

		final JCheckBox checkbox = new JCheckBox(new AbstractAction("Инверсия") {
			private static final long serialVersionUID = 7907647301140283578L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof AbstractButton) {
					id.inverse = ((AbstractButton) e.getSource()).isSelected();
				}

			}
		});
		checkbox.setSelected(id.inverse);

		this.channelInfoPanel.add(checkbox);

	}

}
