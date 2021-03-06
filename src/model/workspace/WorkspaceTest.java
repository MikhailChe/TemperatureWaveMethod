package model.workspace;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import model.signalID.AdjustmentSignalID;
import model.signalID.BaseSignalID;
import model.signalID.DCsignalID;

public class WorkspaceTest {

	@SuppressWarnings("static-method")
	@Test
	public void testSave() {
		Workspace w = Workspace.getInstance();
		w.setSampleFile(
				new File(
						"C:\\Users\\Mikhail\\Documents\\testnew.smpl"));
		w.getSignalIDs().clear();
		w.getSignalIDs().addAll(
				asList(new BaseSignalID(), new DCsignalID(),
						new AdjustmentSignalID(), null));
		w.save();

		Workspace deser = Workspace.open();
		assertNotNull(deser);

		assertEquals(w.getSampleFile(),
				deser.getSampleFile());

		assertEquals(w.getSignalIDs(),
				deser.getSignalIDs());

	}
}
