package ru.dolika.binder;

import static ru.dolika.binder.Binder.boundJCheckBox;
import static ru.dolika.binder.Binder.boundJTextField;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

public class BinderTest {

	class SimpleBean {
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
			Thread.sleep(1000);

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
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

}
