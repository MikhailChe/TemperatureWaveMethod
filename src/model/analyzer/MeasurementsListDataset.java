package model.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

import model.measurement.Diffusivity;
import model.measurement.Measurement;

public class MeasurementsListDataset implements XYDataset {

	// ---- PUBLIC API ---- //

	public MeasurementsListDataset(List<Measurement> mmm, Function<Measurement, Measurement> filter) {
		this(mmm);
		this.filterRule = filter;
	}

	public MeasurementsListDataset(List<Measurement> mmm) {
		measurements = mmm;
		renewCache();
	}

	public void addMeasurement(Measurement m) {
		measurements.add(m);

		updateCache(m, true);
	}

	public void setFilter(Function<Measurement, Measurement> newFilter) {
		filterRule = newFilter;
		renewCache();
	}

	public void changeFetcherX(FetchersX f) {
		switch (f) {
		case FREQUENCY:
			changeFetcherX(XFetchers.FREQUENCY);
			break;
		case TEMPERATURE:
			changeFetcherX(XFetchers.TEMPERATURE);
			break;
		case TIME:
			changeFetcherX(XFetchers.TIME);
			break;
		default:
			break;
		}
	}

	public void changeFetcherY(FetchersY f) {
		switch (f) {
		case AMPLITUDE:
			changeFetcherY(YFetchers.AMPLITUDE);
			break;
		case CAPCITANCE:
			changeFetcherY(YFetchers.CAPACITANCE);
			break;
		case DIFFUSIVITY:
			changeFetcherY(YFetchers.DIFFUSIVITY);
			break;
		case FREQUENCY:
			changeFetcherY(YFetchers.FREQUENCY);
			break;
		case PHASE:
			changeFetcherY(YFetchers.PHASE);
			break;
		case TEMPERATURE:
			changeFetcherY(YFetchers.TEMPERATURE);
			break;
		default:
			break;

		}
	}

	public void setDifferentiationMode(int differentiationMode) {
		int old = this.differentiationMode;
		this.differentiationMode = differentiationMode;
		if (old != differentiationMode) {
			renewCache();
		}
	}

	public static enum FetchersX {
		TEMPERATURE, FREQUENCY, TIME
	}

	public static enum FetchersY {
		DIFFUSIVITY, PHASE, AMPLITUDE, CAPCITANCE, TEMPERATURE, FREQUENCY
	}

	public List<Measurement> getMeasurements() {
		return Collections.unmodifiableList(measurements);
	}

	// -- PRIVATE PART -- //

	final private List<Measurement> measurements;
	private Function<Measurement, Measurement> filterRule;

	static class XFetchers {
		public final static Function<Measurement, Number> TEMPERATURE = m -> m.temperature.isEmpty() ? 0
				: m.temperature.get(0).value;
		public final static Function<Measurement, Number> FREQUENCY = m -> m.frequency;
		public final static Function<Measurement, Number> TIME = m -> ((m.time) / 1000_000.0 - 10000.0);
	}

	static class YFetchers {
		public final static Function<Measurement, List<Number>> DIFFUSIVITY = (
				m) -> m.diffusivity.stream().map(d -> d.diffusivity).collect(Collectors.toList());
		public final static Function<Measurement, List<Number>> PHASE = (
				m) -> m.diffusivity.stream().map(d -> d.phase).collect(Collectors.toList());
		public final static Function<Measurement, List<Number>> AMPLITUDE = (
				m) -> m.diffusivity.stream().map(d -> d.amplitude).collect(Collectors.toList());
		public final static Function<Measurement, List<Number>> CAPACITANCE = (
				m) -> m.diffusivity.stream().map(d -> d.capacitance).collect(Collectors.toList());
		public final static Function<Measurement, List<Number>> TEMPERATURE = m -> Collections
				.singletonList(XFetchers.TEMPERATURE.apply(m));
		public final static Function<Measurement, List<Number>> FREQUENCY = m -> Collections
				.singletonList(XFetchers.FREQUENCY.apply(m));
	}

	public static class DifferentiatorsY {
		public final static int NONE = 0;
		public final static int CHANNEL = 1;
		public final static int FREQUENCY = 2;
	}

	Function<Measurement, Number> xValFetcher = XFetchers.TEMPERATURE;

	Function<Measurement, List<Number>> yValsFetcher = YFetchers.DIFFUSIVITY;

	private int differentiationMode = DifferentiatorsY.CHANNEL | DifferentiatorsY.FREQUENCY;

	Function<Measurement, List<String>> yNamesFetcher = (m) -> {
		List<String> yNames = new ArrayList<>();

		for (Diffusivity diff : m.diffusivity) {
			if (diff != null) {
				String differentiator = "";
				if (differentiationMode == DifferentiatorsY.NONE) {
					yNames.add("");
				} else {
					if ((differentiationMode & DifferentiatorsY.FREQUENCY) != 0) {
						differentiator += (diff.frequency + "Гц");

					}
					if ((differentiationMode & DifferentiatorsY.CHANNEL) != 0) {
						differentiator += "#" + diff.channelNumber;
					}
					yNames.add(differentiator);
				}
			}
		}
		if (yNames.isEmpty())
			yNames.add("");
		return yNames;
	};

	private void changeFetcherX(Function<Measurement, Number> fetcher) {
		Function<Measurement, Number> old = xValFetcher;
		xValFetcher = fetcher;
		if (!old.equals(fetcher)) {
			renewCache();
		}
	}

	private void changeFetcherY(Function<Measurement, List<Number>> fetcher) {
		Function<Measurement, List<Number>> old = yValsFetcher;
		yValsFetcher = fetcher;
		if (!old.equals(fetcher)) {
			renewCache();
		}
	}

	private void renewCache() {
		clearCache(false);
		updateCache(measurements);
		notifyListeners();
	}

	private void updateCache(List<Measurement> mms) {
		if (filterRule == null) {
			filterRule = (m) -> m;
		}
		mms.forEach(m -> updateCache(m, false));
		notifyListeners();
	}

	private void updateCache(Measurement m, boolean notify) {
		m = filterRule.apply(m);
		synchronized (cacheLock) {
			Number xVal = xValFetcher.apply(m);

			List<Number> yVals = yValsFetcher.apply(m);
			List<String> yNames = yNamesFetcher.apply(m);

			for (int i = 0; i < yVals.size(); i++) {
				Number val = yVals.get(i);
				String name = yNames.get(i);

				if (!seriesByNames.containsKey(name)) {
					addSeries(name);
				}
				seriesVals.get(seriesByNames.get(name)).add(new Pair<>(xVal, val));
			}
		}
		if (notify)
			notifyListeners();
	}

	private void clearCache(boolean notify) {
		synchronized (cacheLock) {
			seriesByNames.clear();
			seriesByInds.clear();
			seriesVals.clear();
			counter = 0;
		}
		if (notify)
			notifyListeners();
	}

	private void notifyListeners() {
		listeners.forEach(l -> SwingUtilities.invokeLater(() -> l
				.datasetChanged(new DatasetChangeEvent(MeasurementsListDataset.this, MeasurementsListDataset.this))));
	}

	private static class Pair<T> {
		T x;
		T y;

		public Pair(T x, T y) {
			this.x = x;
			this.y = y;
		}
	}

	final Object cacheLock = new Object();
	private int counter = 0;
	final Map<String, Integer> seriesByNames = new HashMap<>(10);
	final Map<Integer, String> seriesByInds = new HashMap<>(10);
	final Map<Integer, List<Pair<Number>>> seriesVals = new HashMap<>(10);

	private void addSeries(String name) {
		final int cntr = counter++;
		seriesByNames.computeIfAbsent(name, key -> cntr);
		seriesByInds.computeIfAbsent(cntr, key -> name);
		seriesVals.computeIfAbsent(cntr, key -> new ArrayList<>());
	}

	// -- XYDATASET INTERFACE IMPLEMENTATION -- //
	@Override
	public int getSeriesCount() {

		return seriesByNames.size();
	}

	@Override
	public Comparable<?> getSeriesKey(int series) {
		return seriesByInds.get(series);
	}

	@Override
	public int indexOf(Comparable seriesKey) {
		return seriesByNames.get(seriesKey);
	}

	List<DatasetChangeListener> listeners = new ArrayList<>();

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		listeners.remove(listener);
	}

	DatasetGroup grp;

	@Override
	public DatasetGroup getGroup() {
		return grp;
	}

	@Override
	public void setGroup(DatasetGroup group) {
		grp = group;

	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.NONE;
	}

	@Override
	public int getItemCount(int series) {
		return seriesVals.get(series).size();
	}

	@Override
	public Number getX(int series, int item) {
		return seriesVals.get(series).get(item).x;
	}

	@Override
	public double getXValue(int series, int item) {
		return seriesVals.get(series).get(item).x.doubleValue();
	}

	@Override
	public Number getY(int series, int item) {
		return seriesVals.get(series).get(item).y;
	}

	@Override
	public double getYValue(int series, int item) {
		return seriesVals.get(series).get(item).y.doubleValue();
	}
}
