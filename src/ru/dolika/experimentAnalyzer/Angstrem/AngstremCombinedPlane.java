package ru.dolika.experimentAnalyzer.Angstrem;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import ru.dolika.experimentAnalyzer.ExperimentReader;
import ru.dolika.experimentAnalyzer.FFT;

public class AngstremCombinedPlane extends JPanel {

	private final static int ARG = 0;
	private final static int AMP = 1;

	private static final long serialVersionUID = -7130655782816705495L;

	public AngstremCombinedPlane() {
		super();
		setLayout(new GridLayout(5, 2, 5, 5));

		// ���� �������
		JFormattedTextField periodSeconds = new JFormattedTextField(
				NumberFormat.getInstance());
		periodSeconds.setText("86400");
		add(new JLabel("������ ����� (�������)"));
		add(periodSeconds);

		// ���� ��������� ����������
		JFormattedTextField intervalSeconds = new JFormattedTextField(
				NumberFormat.getInstance());
		intervalSeconds.setText("1800");
		add(new JLabel("�������� ��������� (�������)"));
		add(intervalSeconds);

		// ���������� ����� ���������
		JFormattedTextField distanceMilimeters = new JFormattedTextField(
				NumberFormat.getInstance());
		distanceMilimeters.setText("200");
		add(new JLabel("���������� ����� ��������� (��)"));
		add(distanceMilimeters);

		// ����� �����
		JFileChooser fileChooser = new JFileChooser();
		JButton fileChooserOpenButton = new JButton("�������");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		JLabel chosenFileName = new JLabel("�������� ����");
		fileChooserOpenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int action = fileChooser
						.showOpenDialog(AngstremCombinedPlane.this);
				if ((action & JFileChooser.APPROVE_OPTION) == JFileChooser.APPROVE_OPTION) {
					chosenFileName.setText(fileChooser.getSelectedFile()
							.toString());
				}
			}
		});
		add(fileChooserOpenButton);
		add(chosenFileName);

		// ���������
		add(new JLabel());
		JButton calculateButton = new JButton("���������");

		calculateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					periodSeconds.commitEdit();
				} catch (ParseException e1) {
					JOptionPane.showMessageDialog(AngstremCombinedPlane.this,
							"������ � �������", "������ ������� ������",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				try {
					intervalSeconds.commitEdit();
				} catch (ParseException e1) {
					JOptionPane.showMessageDialog(AngstremCombinedPlane.this,
							"������ � ���������", "������ ������� ������",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				try {
					distanceMilimeters.commitEdit();
				} catch (ParseException e1) {
					JOptionPane.showMessageDialog(AngstremCombinedPlane.this,
							"������ � ����������", "������ ������� ������",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				calculateResult(((Long) periodSeconds.getValue()).longValue(),
						((Long) intervalSeconds.getValue()).longValue(),
						((Long) distanceMilimeters.getValue()).longValue(),
						fileChooser.getSelectedFile().toPath());
			}
		});
		add(calculateButton);
	}

	public void calculateResult(long measurementPeriod,
			long mesaurementInterval, long measurementDistance, Path file) {
		ExperimentReader ereader = null;
		try {
			ereader = new ExperimentReader(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ereader == null)
			return;
		// get data from experiment reader
		double[][] data = ereader.getInitialData();
		if (data.length <= 0)
			// TODO: error handler maybe?
			return;
		// number of samples in a single period, so we can crop this thing
		int samplesInOnePeriod = (int) (measurementPeriod / mesaurementInterval);
		int totalAmountOfPeriods = data[0].length / samplesInOnePeriod;

		double[][][] subdividedData = subdivideData(data, totalAmountOfPeriods,
				samplesInOnePeriod);
		double[][][] fftVals = new double[subdividedData.length][subdividedData[0].length][2];
		fillInFftVals(subdividedData, samplesInOnePeriod, fftVals);
		System.out.println();
		// outputCalculatedSineWave(subdividedData, totalAmountOfPeriods,
		// fftVals);
		/*
		 * System.out.println("Amount of period: " +
		 * (subdividedData[0][0].length / samplesInOnePeriod));
		 * System.out.println("#\tday\tphase\tamplitude");
		 */

		calculateDiffusivity(measurementPeriod, measurementDistance, fftVals);
		System.out.println("\r\n\r\n");
	}

	/**
	 * @param measurementPeriod
	 * @param measurementDistance
	 * @param fftVals
	 */
	private void calculateDiffusivity(long measurementPeriod,
			long measurementDistance, double[][][] fftVals) {
		double frequency = 1.0 / measurementPeriod;
		double omega = 2.0 * Math.PI * frequency;
		double l = measurementDistance / 1000.0;

		System.out.println();
		String[] columnNames = { "������", "������",
				"����������� ����������������������", "����������",
				"����� ����", "����1", "����2" };
		Vector<Object[]> data = new Vector<Object[]>();
		for (int channel1 = 0; channel1 < fftVals.length; channel1++) {
			for (int channel2 = channel1 + 1; channel2 < fftVals.length; channel2++) {
				for (int periodNumber = 0; periodNumber < fftVals[channel1].length; periodNumber++) {
					// getting argument for first one
					double phi1 = fftVals[channel1][periodNumber][ARG];

					// getting argument for second one
					double phi2 = fftVals[channel2][periodNumber][ARG];

					double deltaPhi = phi1 - phi2;
					double temperatureDiffusivity = getTDiffusivity(omega, l
							* (channel2 - channel1), deltaPhi);
					data.add(new Object[] {
							"" + (channel1 + 1) + " <->" + (channel2 + 1) + "",
							periodNumber + 1,
							new Double(temperatureDiffusivity),
							new Double(l * (channel2 - channel1)),
							new Double(deltaPhi), new Double(phi1),
							new Double(phi2) });
				}
			}
		}

		UIManager.getDefaults().setDefaultLocale(Locale.getDefault());

		JTable table = new JTable((Object[][]) data.toArray(new Object[data
				.size()][]), columnNames);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		JFrame frame = new JFrame("������� ������");
		frame.setContentPane(scrollPane);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private double[][][] subdivideData(double[][] data,
			int totalAmountOfPeriods, int samplesInOnePeriod) {
		double[][][] subdividedData = new double[data.length][][];
		for (int channel = 0; channel < data.length; channel++) {
			subdividedData[channel] = new double[totalAmountOfPeriods][];
			for (int periodNumber = 0; periodNumber < totalAmountOfPeriods; periodNumber++) {
				subdividedData[channel][periodNumber] = Arrays.copyOfRange(
						data[channel],
						(int) (periodNumber * samplesInOnePeriod),
						(int) ((periodNumber + 1) * samplesInOnePeriod));
			}

			subdividedData[channel] = new double[1][];
			subdividedData[channel][0] = Arrays.copyOf(data[channel],
					totalAmountOfPeriods * samplesInOnePeriod);

		}
		return subdividedData;
	}

	private double NormalizePhase(double phase) {

		while (phase > 2.0 * Math.PI) {
			phase -= Math.PI;
		}
		while (phase < 0) {
			phase += Math.PI;
		}
		return phase;
	}

	public double getTDiffusivity(double omega, double l, double phi) {
		return (omega * (l * l)) / (2.0 * ((phi) * (phi)));
	}

	public void outputCalculatedSineWave(double[][][] subdividedData,
			int totalAmountOfPeriods, double[][][] fftVals) {
		for (int periodNumber = 0; periodNumber < fftVals[0].length; periodNumber++) {
			for (int sample = 0; sample < subdividedData[0][periodNumber].length; sample++) {
				System.out.println();
				for (int channel = 0; channel < fftVals.length; channel++) {
					double val = Math
							.cos(2.0
									* Math.PI
									* (totalAmountOfPeriods / subdividedData[channel].length)
									* sample
									/ subdividedData[channel][periodNumber].length
									+ fftVals[channel][periodNumber][ARG]) * 100;
					System.out.print(val + "\t");
				}
			}
		}
	}

	public void fillInFftVals(double[][][] subdividedData,
			int samplesInOnePeriod, double[][][] fftVals) {
		for (int channel = 0; channel < subdividedData.length; channel++) {
			for (int periodNumber = 0; periodNumber < subdividedData[channel].length; periodNumber++) {
				double[] fftSample = FFT.getFourierForIndex(
						subdividedData[channel][periodNumber],
						subdividedData[channel][periodNumber].length
								/ samplesInOnePeriod);

				/*
				 * System.out.print((i + 1)); System.out.print("\t");
				 * System.out.print((j + 1)); System.out.print("\t");
				 */

				fftVals[channel][periodNumber][ARG] = NormalizePhase(FFT
						.getArgument(fftSample, 0));
				/*
				 * System.out.print(fftVals[i][j][0]); System.out.print("\t");
				 */
				fftVals[channel][periodNumber][AMP] = 2.0
						* FFT.getAbs(fftSample, 0)
						/ subdividedData[channel][periodNumber].length;
				/*
				 * System.out.print(fftVals[i][j][1]); System.out.print("\r\n");
				 */
			}
		}
	}

	public int combinations(int num) {
		if (num < 0) {
			return 0;
		}
		if (num == 0) {
			return 0;
		}
		if (num == 1) {
			return 0;
		}
		return (num - 1) + combinations(num - 1);
	}
}