package view.experiment.signalID.dialog;

import java.awt.Color;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import model.experiment.signalID.DCsignalID;
import model.thermocouple.graduate.Graduate;
import model.thermocouple.graduate.GraduateFactory;
import view.MemorableDirectoryChooser;

public class DCSignalIDPanel extends SignalIDPanel {

	private static final long serialVersionUID = -4908026409118547828L;

	public DCSignalIDPanel(DCsignalID id) {
		super(id);
		setBorder(new CompoundBorder(new EmptyBorder(16, 16, 16, 16),
				new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
						"Канал постоянной составляющей", TitledBorder.LEADING, TitledBorder.TOP, null,
						new Color(0, 0, 0))));
		if (id.getGraduate() != null) {
			this.fileNameField.setText(id.getGraduate().toString());
		}
		this.fileOpenButtton.addActionListener(e -> {
			MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(DCSignalIDPanel.class);
			chooser.setDialogTitle("Выберите файл градуировки");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			chooser.addChoosableFileFilter(Graduate.extensionFilter);
			chooser.setFileFilter(Graduate.extensionFilter);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				chooser.saveCurrentSelection();
				File file = chooser.getSelectedFile();
				Graduate newgrad = GraduateFactory.forBinary(file);
				id.setGraduate(newgrad);
				this.fileNameField.setText("Градуировка: " + newgrad);
			}
		});

	}

}
