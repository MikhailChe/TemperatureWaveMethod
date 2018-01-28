package model.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import controller.lambda.Predicates;
import debug.Debug;
import model.sample.Sample;
import model.sample.SampleFactory;
import model.signalID.SignalIdentifier;

@XmlAccessorType(XmlAccessType.NONE)
public class Workspace {

	final static String defaultWorkspace = "workspace.xml";
	private static Workspace instance = null;

	public static Workspace getInstance() {
		if (instance == null) {
			synchronized (Workspace.class) {
				if (instance == null) {
					Debug.println("Opening instance");
					instance = open();
				}
			}
		}
		return instance;
	}

	public static Workspace open() {
		Debug.println("Opening default workspace [static Workspace.open()]");
		Workspace opened = null;
		try {
			opened = open(defaultWorkspace);
		} catch (Exception ignore) {
			Debug.println("Could not open " + defaultWorkspace + "\r\n" + ignore.getMessage());
		}

		if (opened == null) {
			Debug.println("There was no workspace file, creating new one");
			opened = new Workspace();
			opened.save();
		}
		return opened;

	}

	public static Workspace open(String filename) {
		Debug.println("Opening workspace [static Workspace.open(" + filename + ")]");

		File f = new File(filename);
		if (!f.exists())
			return null;
		return JAXB.unmarshal(f, Workspace.class);
	}

	public static void save(String filename, Workspace w) {
		w.save(filename);
	}

	private transient Sample sample;
	@XmlElement
	private File samplefile;

	@XmlElementWrapper(nillable = true)
	private List<SignalIdentifier> signalIDs;

	private Workspace() {
		Debug.println("Workpsace contructor called");
	}

	public Sample getSample() {
		if (sample == null) {
			if (samplefile != null) {
				Debug.println("Opening sample xml file " + samplefile);
				try {
					sample = SampleFactory.forXML(samplefile.toString());
				} catch (DataBindingException e) {
					Debug.println("Ошибка сбора данных из файла образца: " + e.getLocalizedMessage());
				}
				if (sample == null) {
					samplefile = null;
				}
			}
		}
		return sample;
	}

	public File getSampleFile() {
		return samplefile;
	}

	public List<SignalIdentifier> getSignalIDs() {
		if (signalIDs == null) {
			signalIDs = new ArrayList<>();
		}

		return signalIDs;
	}

	public synchronized void save() {
		save(defaultWorkspace);
	}

	public synchronized void save(String filename) {
		Debug.println("Сохраняю рабочее пространство " + filename);
		JAXB.marshal(this, new File(filename));
	}

	public Sample setSample(Sample newsample) {
		sample = newsample;
		return sample;
	}

	public File setSampleFile(File newsamplefile) {
		samplefile = newsamplefile;
		save();
		return samplefile;
	}

	@Override
	public boolean equals(Object o) {
		return Predicates.areEqual(Workspace.class, this, o,
				Arrays.asList(Workspace::getSampleFile, Workspace::getSignalIDs));
	}

	@Override
	public int hashCode() {
		return Objects.hash(samplefile, signalIDs);
	}

}
