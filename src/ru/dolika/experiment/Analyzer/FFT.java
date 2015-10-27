package ru.dolika.experiment.Analyzer;

public class FFT {

	public static double[] getFourierForIndex(double[] realData, int index) {
		double real = 0, imag = 0;

		double N = realData.length;
		double twoPiIndexDivN = (2 * Math.PI * index) / N;
		for (int i = 0; i < realData.length; i++) {
			real += Math.cos((twoPiIndexDivN * i)) * realData[i];
			imag -= Math.sin((twoPiIndexDivN * i)) * realData[i];
		}
		return new double[] { real, imag };
	}

	public static double[] getReal(double[] array) {
		double[] real = new double[array.length / 2];
		for (int i = 0; i < real.length; i++) {
			real[i] = array[2 * i];

		}
		return real;
	}

	public static double[] getImag(double[] array) {
		double[] imag = new double[array.length / 2];
		for (int i = 0; i < imag.length; i++) {
			imag[i] = array[2 * i + 1];
		}
		return imag;
	}

	public static double getArgument(double[] input, int index) {
		return Math.atan2(input[index * 2 + 1], input[index * 2]);
	}

	public static double getAbs(double[] input, int index) {
		if (input == null)
			return 0;

		return Math.sqrt(input[index * 2] * input[index * 2]
				+ input[index * 2 + 1] * input[index * 2 + 1]);
	}

	public static double[] getAbs(double[] input) {
		double[] output = new double[input.length / 2];
		for (int i = 0; i < output.length; i++) {
			output[i] = Math.sqrt(input[i * 2] * input[i * 2]
					+ input[i * 2 + 1] * input[i * 2 + 1]);
		}
		return output;
	}

	public static double getFreqency(int index, double sampleRate, int maxIndex) {
		return sampleRate * index / maxIndex;
	}

	public static int getIndex(double freq, double sampleRate, int maxIndex) {
		return (int) Math.round(freq * maxIndex / sampleRate);
	}

	public static double[] normalizeArray(double[] input) {
		double[] output = new double[input.length / 2];
		output[0] = input[0] / input.length;
		for (int i = 1; i < output.length; i++) {
			output[i] = (input[i] + input[input.length - i]) / input.length;
		}
		return output;
	}

	public static int[] discreteHaarWaveletTransform(int[] input) {
		// This function assumes that input.length=2^n, n>1
		int[] output = new int[input.length];

		for (int length = input.length >> 1;; length >>= 1) {
			// length = input.length / 2^n, WITH n INCREASING to
			// log(input.length) / log(2)
			for (int i = 0; i < length; ++i) {
				int sum = input[i * 2] + input[i * 2 + 1];
				int difference = input[i * 2] - input[i * 2 + 1];
				output[i] = sum;
				output[length + i] = difference;
			}
			if (length == 1) {
				return output;
			}

			// Swap arrays to do next iteration
			System.arraycopy(output, 0, input, 0, length << 1);
		}
	}

}
