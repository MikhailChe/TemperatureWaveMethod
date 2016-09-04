package ru.dolika.binder;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.management.NotCompliantMBeanException;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.sun.jmx.mbeanserver.Introspector;

public class Binder<T extends Component, U> implements PropertyChangeListener {

	private static <S extends Component, R> Binder<S, R> defaultBound(
			Object bean, String propertyName,
			BiConsumer<String, PropertyChangeListener> beanAddPropertyChangeListener,
			Consumer<? super R> beanSetValue, S control,
			Consumer<? super R> controlSetValue, Class<R> type) {

		Binder<S, R> binder = new Binder<>(bean, control, beanSetValue,
				controlSetValue, type);
		beanAddPropertyChangeListener.accept(propertyName, binder);
		return binder;
	}

	/**
	 * Binder boundJCheckBox(Object <b>bean</b>, String <b>propertyName</b>,
	 * BiConsumer <b>beanAddPropertyChangeListener</b>, Consumer
	 * <b>beanSetValue</b>, JCheckBox <b>control</b>)
	 * 
	 * @param bean
	 * @param propertyName
	 * @param beanAddPropertyChangeListener
	 * @param beanSetValue
	 * @param control
	 * @return
	 */
	public static Binder<JCheckBox, Boolean> boundJCheckBox(Object bean,
			String propertyName,
			BiConsumer<String, PropertyChangeListener> beanAddPropertyChangeListener,
			Consumer<? super Boolean> beanSetValue, JCheckBox control) {

		Binder<JCheckBox, Boolean> binder = defaultBound(bean, propertyName,
				beanAddPropertyChangeListener, beanSetValue, control,
				control::setSelected, Boolean.class);
		control.addActionListener(e -> {
			binder.propertyChange(new PropertyChangeEvent(control, propertyName,
					null, control.isSelected()));
		});
		return binder;

	}

	public static Binder<JTextField, String> boundJTextField(Object bean,
			String propertyName,
			BiConsumer<String, PropertyChangeListener> beanAddPropertyChangeListener,
			Consumer<? super String> beanSetValue, JTextField control) {

		Binder<JTextField, String> binder = defaultBound(bean, propertyName,
				beanAddPropertyChangeListener, beanSetValue, control,
				control::setText, String.class);
		control.addActionListener(e -> {
			binder.propertyChange(new PropertyChangeEvent(control, propertyName,
					null, control.getText()));
		});
		return binder;
	}

	final private Class<U> type;
	final private Object bean;
	final private T control;
	final private Consumer<? super U> beanSetValue;
	final private Consumer<? super U> controlSetValue;

	private Binder(Object bean, T control, Consumer<? super U> beanSetValue,
			Consumer<? super U> controlSetValue, Class<U> type) {
		this.bean = bean;
		this.control = control;
		this.beanSetValue = beanSetValue;
		this.controlSetValue = controlSetValue;
		this.type = type;
	}

	public T control() {
		return control;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("FIrefighth" + evt.getSource() + " "
				+ bean.getClass().getName());

		if (evt.getSource().equals(control)) {
			System.out.println("Control fired." + evt.getPropertyName() + ","
					+ evt.getOldValue() + "->" + evt.getNewValue());
			if (type.isInstance(evt.getNewValue())) {
				beanSetValue.accept((type.cast(evt.getNewValue())));
			}
		} else if (evt.getSource().equals(bean)) {
			System.out.println("Bean fired." + evt.getPropertyName() + ","
					+ evt.getOldValue() + "->" + evt.getNewValue());
			if (type.isInstance(evt.getNewValue())) {
				controlSetValue.accept((type.cast(evt.getNewValue())));
			}
		}

	}

}
