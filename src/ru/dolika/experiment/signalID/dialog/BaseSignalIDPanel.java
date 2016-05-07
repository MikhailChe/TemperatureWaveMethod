package ru.dolika.experiment.signalID.dialog;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ru.dolika.experiment.signalID.BaseSignalID;
import ru.dolika.experiment.zeroCrossing.ZeroCrossing;
import ru.dolika.experiment.zeroCrossing.ZeroCrossingFactory;
import ru.dolika.experiment.zeroCrossing.ZeroCrossingViewerPanel;
import ru.dolika.ui.MemorableDirectoryChooser;

public class BaseSignalIDPanel extends SignalIDPanel {
	private static final long serialVersionUID = 7193781950090874574L;

	ZeroCrossingViewerPanel zcPanel;

	public BaseSignalIDPanel(BaseSignalID id) {
		super(id);
		setBorder(new CompoundBorder(new EmptyBorder(16, 16, 16, 16),
				new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
						"Канал переменного сигнала", TitledBorder.LEADING, TitledBorder.TOP, null,
						new Color(0, 0, 0))));
		if (id.zc != null)
			this.fileNameField.setText(id.zc.forFile.toString());
		this.fileOpenButtton.addActionListener(e -> {
			MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(DCSignalIDPanel.class);
			chooser.setDialogTitle("Выберите файл юстировки");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			chooser.addChoosableFileFilter(ZeroCrossing.extensionFilter);
			chooser.setFileFilter(ZeroCrossing.extensionFilter);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				chooser.saveCurrentSelection();
				File file = chooser.getSelectedFile();
				ZeroCrossing newzc = ZeroCrossingFactory.forFile(file);
				id.zc = newzc;
				zcPanel.setZeroCrossing(id.zc);
				this.fileNameField.setText(id.zc.forFile.toString());
			}
		});

		zcPanel = new ZeroCrossingViewerPanel(id.zc);
		zcPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JDialog zcDialog = new JDialog((JDialog) SwingUtilities.getWindowAncestor(BaseSignalIDPanel.this),
						id.zc.forFile.toString(), true);
				zcDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				zcDialog.getContentPane().add(new ZeroCrossingViewerPanel(id.zc));
				zcDialog.pack();
				zcDialog.setSize(640, 480);
				zcDialog.setVisible(true);
			}
		});
		this.channelInfoPanel.add(zcPanel);

	}

}
