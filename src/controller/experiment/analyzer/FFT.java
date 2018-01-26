package controller.experiment.analyzer;

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

	/**
	 * Функция производит вычисление мнимой и действительной части входного сигнала
	 * realData на частоте index. This function calculates complex number, that
	 * describes input signal realData at relative frequency index
	 * 
	 * @param realData
	 *            входной сигнал
	 * @param realData
	 *            input signal
	 * @param index
	 *            частота (относительно длины входного сигнала)
	 * @param index
	 *            frequency (relative to input signal length)
	 * 
	 * 
	 * @return complex number, that shows signal parameters.
	 * @return комлпексное число, пока зывающее параметры сигнала
	 */
	public static double[] getFourierForIndex(final double[] realData, final int index) {
		double real = 0, imag = 0;
		final int N = realData.length;
		final double twoPiIndexDivN = (2.0 * Math.PI * index) / N;
		real = +IntStream.range(0, N).parallel().mapToDouble(i -> Math.cos(twoPiIndexDivN * i) * realData[i]).sum();
		imag = -IntStream.range(0, N).parallel().mapToDouble(i -> Math.sin(twoPiIndexDivN * i) * realData[i]).sum();
		return new double[] { real, imag };
	}

	/**
	 * Функция выделяет действительную часть из массива, возвращаемого
	 * преобразованием фурье. This function filters out real part of an array, that
	 * was returned by FFT
	 * 
	 * @param fftdata
	 *            Входные данные. Формат - одномерный массив, где 2*n элементы -
	 *            действительные, а (2*n)+1 элементы - мнимые
	 * @param fftdata
	 *            Input data of the following format: 1-d array, where 2*n elements
	 *            - real part of complex number, (2*n)+1 - imaginary
	 * @return array of real parts of fftdata array
	 * @return массив только действительных чисел из массива комплексных
	 */
	public static double[] getReal(double[] fftdata) {
		double[] real = new double[fftdata.length / 2];
		for (int i = 0; i < real.length; i++) {
			real[i] = fftdata[2 * i];
		}
		return real;
	}

	/**
	 * Функция выделяет мнимую часть из массива, возвращаемого преобразованием
	 * фурье.
	 * 
	 * This function filters out imaginary part of an array, that was returned by
	 * FFT
	 * 
	 * @param fftdata
	 *            Входные данные. Формат - одномерный массив, где 2*n элементы -
	 *            действительные, а (2*n)+1 элементы - мнимые
	 * @param fftdata
	 *            Input data of the following format: 1-d array, where 2*n elements
	 *            - real part of complex number, (2*n)+1 - imaginary
	 * @return array of imaginary parts of fftdata array
	 * @return массив только мнимыхчисел из массива комплексных
	 */
	public static double[] getImag(double[] fftdata) {
		double[] imag = new double[fftdata.length / 2];
		for (int i = 0; i < imag.length; i++) {
			imag[i] = fftdata[2 * i + 1];
		}
		return imag;
	}

	/**
	 * Вычисляет аргумент (угол) одного комплексного числа с индексом index из
	 * массива комплексных чисел fftdata
	 * 
	 * Computer argument (angle) of index'th number from array if complex numbers
	 * fftdata
	 * 
	 * @param fftdata
	 *            Массив комплексных чисел, где 2*n - действительная часть числа,
	 *            (2*n)+1 - мнимая часть числа
	 * @param fftdata
	 *            Array of complex number, where 2*n - real part of number, (2*n)+1
	 *            - imaginary part of it
	 * @param index
	 *            индекс в массиве комплексных чисел
	 * @param index
	 *            index in an array of complex numbers
	 * 
	 * @return argument (phase in radians) for <b>index</b>th element of fftdata
	 *         <br/>
	 *         аргумент (фаза в радианах) index-ого элемента из массива fftdata
	 */
	public static double getArgument(double[] fftdata, int index) {
		if (fftdata == null)
			return 0;
		return Math.atan2(fftdata[index * 2 + 1], fftdata[index * 2]);
	}

	/**
	 * Вычисляет модуль (длину) одного комплексного числа с индексом index из
	 * массива комплексных чисел fftdata
	 * 
	 * Computer absolute value (length) of index'th number from array if complex
	 * numbers fftdata
	 * 
	 * @param fftdata
	 *            Массив комплексных чисел, где 2*n - действительная часть числа,
	 *            (2*n)+1 - мнимая часть числа
	 * @param fftdata
	 *            Array of complex number, where 2*n - real part of number, (2*n)+1
	 *            - imaginary part of it
	 * @param index
	 *            индекс в массиве комплексных чисел
	 * @param index
	 *            index in an array of complex numbers
	 * 
	 * @return abs (length) for <b>index</b>th element of fftdata <br/>
	 *         модуль (длину) index-ого элемента из массива fftdata
	 */
	public static double getAbs(double[] fftdata, int index) {
		if (fftdata == null)
			return 0;

		return Math.sqrt(fftdata[index * 2] * fftdata[index * 2] + fftdata[index * 2 + 1] * fftdata[index * 2 + 1]);
	}

	/**
	 * Вычисляет массив модулей (длин) комплексных чисел из массива fftdata
	 * 
	 * Computes an array of absolute values of complex numbers in fftdata array
	 * 
	 * @param fftdata
	 *            массив комплексных чисел, где 2*n - действительная часть
	 *            комплексного числа, 2*n+1- мнимая часть
	 * @param fftdata
	 *            array of complex numbers, where 2*n - real part of complex
	 *            numbers, 2*n+1- imaginary part
	 * @return массив модулей комплексных чисел fftdata<br/>
	 *         array of absolute values for fftdata
	 */
	public static double[] getAbs(double[] fftdata) {
		double[] output = new double[fftdata.length / 2];
		for (int i = 0; i < output.length; i++) {
			output[i] = Math.sqrt(fftdata[i * 2] * fftdata[i * 2] + fftdata[i * 2 + 1] * fftdata[i * 2 + 1]);
		}
		return output;
	}

	/**
	 * Выдаёт частоту в Герцах, основываясь на частоте дискретизации и относительной
	 * частоте (количестве периодов в цифровом сигнале)
	 * 
	 * @param fftindex
	 *            количество периодов (относительная частота)
	 * @param sampleRate
	 *            частота дискретизации сигнала
	 * @param maxIndex
	 *            максимальное количество периодов цифровом сигнале, то есть такое
	 *            количество периодов при котором частота равна частоте
	 *            дискретизации
	 * 
	 *            maximum index of FFT, meaning that at this index frequency should
	 *            be equal to sampleRate
	 * @return frequency of fftindex according to sampleRate and maxIndex;
	 */
	public static double getFreqency(int fftindex, double sampleRate, int maxIndex) {
		return sampleRate * fftindex / maxIndex;
	}

	/**
	 * @param freq
	 * @param sampleRate
	 * @param maxIndex
	 * @return fft index for particular <b>freq</b>uency at a particular sample rate
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
