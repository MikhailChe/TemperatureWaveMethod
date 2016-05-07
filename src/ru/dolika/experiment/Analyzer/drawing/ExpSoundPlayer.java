package ru.dolika.experiment.Analyzer.drawing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFileChooser;

import ru.dolika.experiment.Analyzer.ExperimentReader;
import ru.dolika.ui.MemorableDirectoryChooser;

public class ExpSoundPlayer {

	public static void main(String[] args) {
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(ExpSoundPlayer.class);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (fileChooser.getSelectedFile() != null) {
				try {
					ExperimentReader ereader = new ExperimentReader(fileChooser.getSelectedFile().toPath());
					double[][] data = ereader.getOnePeriodSumm();
					int dataColumnIndex = 1;
					double[] dataAtColumn = data[dataColumnIndex];
					AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
					try {
						SourceDataLine sdl = AudioSystem.getSourceDataLine(format);
						byte[] dataForSDL = doubleArrayToByteArray(dataAtColumn, 2, format.isBigEndian());
						while (System.in.available() <= 0) {
							sdl.write(dataForSDL, 0, dataForSDL.length);
						}
					} catch (LineUnavailableException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	public static byte[] doubleArrayToByteArray(double[] arr, int bytesInSample, boolean bigEndian) {
		if (arr == null) {
			return null;
		}
		if (arr.length == 0) {
			return new byte[0];
		}
		if (bytesInSample <= 0) {
			return new byte[0];
		}

		ByteBuffer bb = ByteBuffer.allocate(arr.length * bytesInSample);
		if (bigEndian) {
			bb.order(ByteOrder.BIG_ENDIAN);
		} else {
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}

		if (bytesInSample == 1) {
			for (int i = 0; i < arr.length; i++) {
				bb.put((byte) arr[i]);
			}
		} else if (bytesInSample == 2) {
			for (int i = 0; i < arr.length; i++) {
				bb.putShort((short) arr[i]);

			}
		}

		return bb.array();
	}
}
