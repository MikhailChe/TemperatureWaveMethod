package ru.dolika.experiment.measurement;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс измерений. Хранит в себе одну точку измерения. Каждой точке присвоено
 * время, частота измерения, массив возможных вычисленных температур и массив
 * вычисленных значений коэффициента температуропроводности
 * 
 * @author Mike
 * 
 */
public class Measurement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7462056475933664988L;
	/**
	 * Время измерения
	 */
	public long time;
	/**
	 * Частота эксперимента
	 */
	public double frequency;
	/**
	 * Массив температур
	 * 
	 * @see Temperature
	 */
	public ArrayList<Temperature> temperature;
	/**
	 * Массив значений температуропроводности
	 * 
	 * @see TemperatureConductivity
	 */
	public ArrayList<TemperatureConductivity> tCond;

	public Measurement() {
		time = System.currentTimeMillis();
		temperature = new ArrayList<Temperature>();
		tCond = new ArrayList<TemperatureConductivity>();
	}

	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("Частота;");
		for (Temperature t : temperature) {
			sb.append(t.getHeader() + ";");
		}
		for (TemperatureConductivity t : tCond) {
			sb.append(t.getHeader() + ";");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%4.1f;", frequency));
		for (Temperature t : temperature) {
			sb.append(t.toString() + ";");
		}
		for (TemperatureConductivity t : tCond) {
			sb.append(t.toString() + ";");
		}
		return sb.toString();
	}
}
