package model.experiment.workspace;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import debug.Debug;
import model.experiment.sample.Sample;
import model.experiment.sample.SampleFactory;
import model.experiment.signalID.SignalIdentifier;

public class Workspace implements Serializable {

	/**
	 * @author Mikey
	 * @serialField serialVersionUID
	 */
	final private static long	serialVersionUID	= -2757711622043028895L;
	private static Workspace	instance			= null;
	final static String			defaultWorkspace	= "workspace.xml";

	public synchronized static Workspace getInstance() {
		if (instance == null) {
			Debug.println("Opening instance");
			instance = open();
		}
		return instance;
	}

	public synchronized static Workspace open() {
		Debug.println("Opening default workspace [static Workspace.open()]");
		Workspace opened = open(defaultWorkspace);

		if (opened == null) {
			Debug.println("There was no workspace file, creating new one");
			opened = new Workspace();
			opened.save();
		}
		return opened;

	}

	public synchronized static Workspace open(String filename) {
		Debug.println(
				"Opening workspace [static Workspace.open(" + filename + ")]");

		File f = new File(filename);
		if (!f.exists()) return null;
		return JAXB.unmarshal(new File(filename), Workspace.class);
	}

	public synchronized static void save(String filename, Workspace w) {
		w.save(filename);
	}

	private transient Sample		sample;
	@XmlElement
	private File					samplefile;

	@XmlElement
	@XmlElementWrapper(name = "signalIDs")
	private List<SignalIdentifier>	signalIDs;

	private Workspace() {
		Debug.println("Workpsace contructor called");
	}

	public synchronized void save() {
		save(defaultWorkspace);
	}

	public synchronized void save(String filename) {
		Debug.println("Сохраняю рабочее пространство " + filename);
		JAXB.marshal(this, new File(filename));
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		Debug.println("Deserializing workspace");
	}

	public Sample getSample() {
		if (sample == null) {
			if (samplefile != null) {
				Debug.println("Opening sample binary " + samplefile);
				sample = SampleFactory.forBinary(samplefile.toString());
			}
		}
		return sample;
	}

	public Sample setSample(Sample newsample) {
		sample = newsample;
		return sample;
	}

	public List<SignalIdentifier> getSignalIDs() {
		if (signalIDs == null) {
			signalIDs = new ArrayList<>();
		}

		return signalIDs;
	}

	public File getSampleFile() {
		return samplefile;
	}

	public File setSampleFile(File newsamplefile) {
		samplefile = newsamplefile;
		save();
		return samplefile;
	}
}
