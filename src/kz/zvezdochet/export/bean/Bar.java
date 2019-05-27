package kz.zvezdochet.export.bean;

import org.eclipse.swt.graphics.Color;

/**
 * Элемент диаграммы
 * @author Natalie Didenko
 *
 */
public class Bar {

	public Bar() {
		super();
	}

	public Bar(String name, double value, Color color, String category) {
		super();
		this.name = name;
		this.value = value;
		this.color = color;
		this.category = category;
	}
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
	/**
	 * Категория
	 */
	private String category;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
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
