package kz.zvezdochet.export.bean;

import org.eclipse.swt.graphics.Color;

/**
 * Класс, представляющий элемент диаграммы
 * @author nataly
 *
 */
public class Bar {
	/**
	 * Описание
	 */
	private String name;
	/**
	 * Значение
	 */
	private double value;
	/**
	 * Цвет
	 */
	private Color color;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
}
