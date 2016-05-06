package ru.dolika.experiment.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.signalID.SignalIdentifier;

public class Workspace implements Serializable {

	/**
	 * @author Mikey
	 * @serialField serialVersionUID
	 */
	private static final long serialVersionUID = -2757711622043028895L;
	private static Workspace instance = null;
	final static String defaultWorkspace = "workspace.expws";

	public static boolean debug = true;

	public synchronized static Workspace getInstance() {
		if (instance == null) {
			if (debug)
				System.out.println("Opening instance");
			instance = open();
		}
		return instance;
	}

	public synchronized static Workspace open() {
		if (debug)
			System.out.println("Opening default workspace [static Workspace.open()]");
		Workspace opened = open(defaultWorkspace);

		if (opened == null) {
			if (debug)
				System.out.println("There was no workspace file, creating new one");
			opened = new Workspace();
			opened.save();
		}
		return opened;

	}

	public synchronized static Workspace open(String filename) {
		if (debug)
			System.out.println("Opening workspace [static Workspace.open(" + filename + ")]");

		File f = new File(filename);
		if (!f.exists())
			return null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			Object o = ois.readObject();
			if (o instanceof Workspace) {
				Workspace wspace = (Workspace) o;
				if (debug)
					System.out.println("Opened workspace " + filename);
				return wspace;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public synchronized static void save(String filename, Workspace w) {
		w.save(filename);
	}

	private transient Sample sample = null;
	private File samplefile = null;
	private ArrayList<SignalIdentifier> signalIDs = null;

	private Workspace() {
		if (debug)
			System.out.println("Workpsace contructor called");
	}

	public synchronized void save() {
		save(defaultWorkspace);
	}

	public synchronized void save(String filename) {
		if (debug) {
			System.out.println("Сохраняю рабочее пространство " + filename);
		}

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		System.out.println("Deserializing workspace");
	}

	public Sample getSample() {
		if (sample == null) {
			if (samplefile != null) {
				if (debug)
					System.out.println("Opening sample binary " + samplefile);
				sample = SampleFactory.forBinary(samplefile.toString());
			}
		}
		return sample;
	}

	public Sample setSample(Sample newsample) {
		sample = newsample;
		return sample;
	}

	public ArrayList<SignalIdentifier> getSignalIDs() {
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
