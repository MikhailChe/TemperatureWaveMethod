package view.measurements;

import static java.awt.BorderLayout.CENTER;
import static javax.swing.border.BevelBorder.LOWERED;
import static model.experiment.Analyzer.MeasurementsListDataset.DifferentiatorsY.CHANNEL;
import static model.experiment.Analyzer.MeasurementsListDataset.DifferentiatorsY.FREQUENCY;
import static model.experiment.Analyzer.MeasurementsListDataset.DifferentiatorsY.NONE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.SoftBevelBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;

import model.experiment.Analyzer.MeasurementsListDataset;
import model.experiment.Analyzer.MeasurementsListDataset.FetchersX;
import model.experiment.Analyzer.MeasurementsListDataset.FetchersY;
import model.experiment.measurement.Measurement;

public class MeasurementViewer extends JPanel {
    private static final long serialVersionUID = 3555290921726804677L;

    final ChartPanel chartPanel;

    final MeasurementsListDataset dataset = new MeasurementsListDataset(new ArrayList<>(), m -> {
	Measurement nm = new Measurement();
	nm.frequency = m.frequency;
	nm.temperature = m.temperature;
	nm.time = m.time;
	nm.diffusivity = new ArrayList<>(m.diffusivity);
	nm.diffusivity.removeIf(d -> d.amplitude < 100 || d.diffusivity > 3E-5);
	return nm;
    });

    public MeasurementViewer() {
	super();

	NumberAxis xAxis = new NumberAxis(null);
	NumberAxis yAxis = new NumberAxis(null);

	yAxis.setLowerBound(0);
	yAxis.setUpperBound(2E-5);
	yAxis.setAutoRangeIncludesZero(true);
	yAxis.setAutoRange(true);

	xAxis.setAutoRange(true);
	xAxis.setAutoRangeIncludesZero(false);

	JFreeChart chart = new JFreeChart("Измерения", new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer()));
	chartPanel = new ChartPanel(chart);

	Dimension d = new Dimension(640, 480);
	setPreferredSize(d);
	setBorder(new SoftBevelBorder(LOWERED));

	this.setLayout(new BorderLayout());
	this.add(chartPanel, CENTER);

	chartPanel.setMaximumDrawHeight(1080);
	chartPanel.setMinimumDrawHeight(100);
	chartPanel.setMaximumDrawWidth(1920);
	chartPanel.setMinimumDrawWidth(100);

	{
	    JPanel xAxisChooserPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
	    JRadioButton btnTemperature = new JRadioButton("Температура", true);
	    JRadioButton btnFrequency = new JRadioButton("Частота", false);
	    JRadioButton btnTime = new JRadioButton("Время", false);
	    ButtonGroup group = new ButtonGroup();
	    group.add(btnTemperature);
	    group.add(btnFrequency);
	    group.add(btnTime);

	    ActionListener act = (e) -> {
		String actionCommand = e.getActionCommand();
		switch (actionCommand) {
		case ("Температура"): {
		    dataset.changeFetcherX(FetchersX.TEMPERATURE);
		    break;
		}
		case ("Частота"): {
		    dataset.changeFetcherX(FetchersX.FREQUENCY);
		    break;
		}
		case ("Время"): {
		    dataset.changeFetcherX(FetchersX.TIME);
		    break;
		}
		default:
		    break;
		}
	    };
	    Enumeration<AbstractButton> enumer = group.getElements();
	    while (enumer.hasMoreElements()) {
		AbstractButton btn = enumer.nextElement();
		btn.addActionListener(act);
		xAxisChooserPanel.add(btn);
	    }
	    this.add(xAxisChooserPanel, BorderLayout.SOUTH);
	}
	{
	    JPanel yAxisChooserPanel = new JPanel();
	    yAxisChooserPanel.setLayout(new BoxLayout(yAxisChooserPanel, BoxLayout.Y_AXIS));
	    JRadioButton btnDiffusivity = new JRadioButton("a", true);
	    JRadioButton btnPhase = new JRadioButton("φ", false);
	    JRadioButton btnAmplitude = new JRadioButton("Ампл", false);
	    JRadioButton btnCapacitance = new JRadioButton("Сp", false);
	    JRadioButton btnTemperature = new JRadioButton("°K", false);
	    JRadioButton btnFrequency = new JRadioButton("ν", false);
	    ButtonGroup group = new ButtonGroup();
	    group.add(btnDiffusivity);
	    group.add(btnPhase);
	    group.add(btnAmplitude);
	    group.add(btnCapacitance);
	    group.add(btnTemperature);
	    group.add(btnFrequency);

	    ActionListener act = (e) -> {
		String actionCommand = e.getActionCommand();
		switch (actionCommand) {
		case "a": {
		    dataset.changeFetcherY(FetchersY.DIFFUSIVITY);
		    dataset.setDifferentiationMode(CHANNEL | FREQUENCY);
		    break;
		}
		case "φ": {
		    dataset.changeFetcherY(FetchersY.PHASE);
		    dataset.setDifferentiationMode(CHANNEL | FREQUENCY);
		    break;
		}
		case "Ампл": {
		    dataset.changeFetcherY(FetchersY.AMPLITUDE);
		    dataset.setDifferentiationMode(CHANNEL);
		    break;
		}
		case "Сp": {
		    dataset.changeFetcherY(FetchersY.CAPCITANCE);
		    dataset.setDifferentiationMode(CHANNEL | FREQUENCY);
		    break;
		}
		case "°K": {
		    dataset.changeFetcherY(FetchersY.TEMPERATURE);
		    dataset.setDifferentiationMode(NONE);
		    break;
		}
		case "ν": {
		    dataset.changeFetcherY(FetchersY.FREQUENCY);
		    dataset.setDifferentiationMode(CHANNEL);
		    break;
		}
		default:
		    break;

		}
	    };
	    Enumeration<AbstractButton> enumer = group.getElements();

	    yAxisChooserPanel.add(Box.createGlue());
	    while (enumer.hasMoreElements()) {
		AbstractButton btn = enumer.nextElement();
		btn.addActionListener(act);
		yAxisChooserPanel.add(btn);
	    }
	    yAxisChooserPanel.add(Box.createGlue());

	    this.add(yAxisChooserPanel, BorderLayout.WEST);
	}
    }

    public void addMeasurement(Measurement m) {
	dataset.addMeasurement(m);
    }

}