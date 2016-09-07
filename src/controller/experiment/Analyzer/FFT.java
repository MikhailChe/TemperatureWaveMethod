package controller.experiment.Analyzer;

import java.util.stream.IntStream;

/**
 * @author Mikhail
 *
 */
/**
 * @author Mikhail
 *
 */
public class FFT {

	public static double[] getFourierForIndex(double[] realData, int index) {
		double real = 0, imag = 0;
		double N = realData.length;
		double twoPiIndexDivN = (2 * Math.PI * index) / N;
		real = IntStream.range(0, realData.length)
				.mapToDouble(i -> Math.cos((twoPiIndexDivN * i)) * realData[i])
				.parallel()
				.sum();

		imag = -IntStream.range(0, realData.length)
				.mapToDouble(i -> Math.sin((twoPiIndexDivN * i)) * realData[i])
				.parallel()
				.sum();

		return new double[] { real, imag };
	}

	/**
	 * @param fftdata
	 * @return array of real parts of fftdata array
	 */
	public static double[] getReal(double[] fftdata) {
		double[] real = new double[fftdata.length / 2];
		for (int i = 0; i < real.length; i++) {
			real[i] = fftdata[2 * i];
		}
		return real;
	}

	/**
	 * @param fftdata
	 * @return array of imaginary parts of fftdata array
	 */
	public static double[] getImag(double[] fftdata) {
		double[] imag = new double[fftdata.length / 2];
		for (int i = 0; i < imag.length; i++) {
			imag[i] = fftdata[2 * i + 1];
		}
		return imag;
	}

	/**
	 * @param fftdata
	 * @param index
	 * @return argument (phase) for <b>index</b>th element of fftdata
	 */
	public static double getArgument(double[] fftdata, int index) {
		if (fftdata == null)
			return 0;
		return Math.atan2(fftdata[index * 2 + 1], fftdata[index * 2]);
	}

	/**
	 * @param fftdata
	 * @param index
	 * @return abs (absolute value) for <b>index</b>th element of fftdata
	 */
	public static double getAbs(double[] fftdata, int index) {
		if (fftdata == null)
			return 0;

		return Math.sqrt(fftdata[index * 2] * fftdata[index * 2]
				+ fftdata[index * 2 + 1] * fftdata[index * 2 + 1]);
	}

	/**
	 * @param fftdata
	 * @return array of absolute values for fftdata
	 */
	public static double[] getAbs(double[] fftdata) {
		double[] output = new double[fftdata.length / 2];
		for (int i = 0; i < output.length; i++) {
			output[i] = Math.sqrt(fftdata[i * 2] * fftdata[i * 2]
					+ fftdata[i * 2 + 1] * fftdata[i * 2 + 1]);
		}
		return output;
	}

	/**
	 * @param fftindex
	 * @param sampleRate
	 * @param maxIndex
	 *            maximum index of FFT, meaning that at this index frequency
	 *            should be equal to sampleRate
	 * @return frequency of fftindex according to sampleRate and maxIndex;
	 */
	public static double getFreqency(int fftindex, double sampleRate,
			int maxIndex) {
		return sampleRate * fftindex / maxIndex;
	}

	/**
	 * @param freq
	 * @param sampleRate
	 * @param maxIndex
	 * @return fft index for particular <b>freq</b>uency at a particular sample
	 *         rate
	 */
	public static int getIndex(double freq, double sampleRate, int maxIndex) {
		return (int) Math.round(freq * maxIndex / sampleRate);
	}

	/**
	 * @param input
	 * @return array of some sort of fft data (like absolute values) without
	 *         mirroring effect
	 */
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
