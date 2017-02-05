package kz.zvezdochet.export.handler;

import java.awt.Paint;

import org.jfree.chart.renderer.category.BarRenderer;

/**
 * Отрисовщик разноцветных категорий внутри серии диаграммы 
 * @author Nataly Didenko
 *
 */
public class ExportBarRenderer extends BarRenderer {
	private static final long serialVersionUID = -5360633474575860788L;

	private Paint[] colors;

    /**
     * Creates a new renderer.
     *
     * @param colors  the colors.
     */
    public ExportBarRenderer(final Paint[] colors) {
        this.colors = colors;
    }

    /**
     * Returns the paint for an item.  Overrides the default behaviour inherited from
     * AbstractSeriesRenderer.
     *
     * @param row  the series.
     * @param column  the category.
     *
     * @return The item color.
     */
    public Paint getItemPaint(final int row, final int column) {
        return colors[column];
    }
}