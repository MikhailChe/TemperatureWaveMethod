package view.binder;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static view.binder.Binder.boundJCheckBox;
import static view.binder.Binder.boundJTextField;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

public class BinderTest {

	class SimpleBean implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8405129845336949775L;
		private String name = "";
		private boolean correct = false;
		final private PropertyChangeSupport pcs = new PropertyChangeSupport(
				this);

		public String getName() {
			return name;
		}

		public void setName(String name) {
			String old = this.name;
			this.name = name;
			pcs.firePropertyChange("name", old, this.name);
		}

		public boolean isCorrect() {
			return correct;
		}

		public void setCorrect(boolean val) {
			boolean old = this.correct;
			this.correct = val;

			pcs.firePropertyChange("correct", old, this.correct);
		}

		public void addPropertyChangeListener(String property,
				PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(property, listener);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}
	}

	SimpleBean bean;

	@Before
	public void initBean() {
		bean = new SimpleBean();
	}
@Test
	public void testBinding() throws InterruptedException {

		JTextField field = new JTextField();

		boundJTextField(bean, "name", bean::addPropertyChangeListener,
				bean::setName, field);

		org.junit.Assert.assertEquals("", bean.getName());
		org.junit.Assert.assertEquals("", field.getText());

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(field);
		frame.pack();
		frame.setVisible(true);

		for (int i = 0; i < 10; i++) {
			System.out
					.println(i + " " + field.getText() + " " + bean.getName());
			if (i == 1) {
				bean.setName("YO");
			}
			Thread.sleep(100);

		}

		org.junit.Assert.assertEquals("YO", field.getText());
		org.junit.Assert.assertEquals("YO", bean.getName());

	}
@Test
	public void testCheckBox() {
		JCheckBox box = new JCheckBox();
		boundJCheckBox(bean, "correct", bean::addPropertyChangeListener,
				bean::setCorrect, box);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(box);
		frame.pack();
		frame.setVisible(true);

		for (int i = 0; i < 10; i++) {
			if (i == 1) {
				bean.setCorrect(true);
			}
			if (i == 5) {
				bean.setCorrect(false);
			}
			System.out.println(
					i + " " + box.isSelected() + " " + bean.isCorrect());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	@Test
	public void checkIntrospection() {
		try {
			BeanInfo info = java.beans.Introspector
					.getBeanInfo(bean.getClass());
			System.out.println(info);
			System.out.println("\r\n");
			System.out.println(info.getBeanDescriptor());
			System.out.println("\r\n");
			System.out.println(asList(info.getPropertyDescriptors()).stream()
					.map(a -> a.toString()).collect(joining("\r\n")));
			System.out.println("\r\n");
			System.out.println(asList(info.getMethodDescriptors()).stream()
					.map(a -> a.toString()).collect(joining("\r\n")));
			System.out.println("\r\n");
			System.out.println(asList(info.getEventSetDescriptors()).stream()
					.map(a -> a.toString()).collect(joining("\r\n")));
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
