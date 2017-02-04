package kz.zvezdochet.export.util;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Calendar;

import org.eclipse.swt.graphics.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import kz.zvezdochet.export.bean.Bar;

/**
 * Набор утилит для pdf-экспорта
 * @author Nataly Didenko
 *
 */
public class PDFUtil {
	/**
	 * Отображение информации о копирайте
	 * @param baseFont базовый шрифт
	 * @return Paragraph абзац
	 */
	public static Paragraph printCopyright(BaseFont baseFont) {
        Paragraph p = new Paragraph();
		try {
			Font font = new Font(baseFont, 10, Font.NORMAL);
			Font fonta = new Font(baseFont, 10, Font.UNDERLINE, new BaseColor(102, 102, 153));

	        p.setAlignment(Element.ALIGN_CENTER);
	        Chunk chunk = new Chunk("© 1998-" + Calendar.getInstance().get(Calendar.YEAR) + " Астрологический сервис ", font);
	        p.add(chunk);
	        chunk = new Chunk("Звездочёт", fonta);
	        chunk.setAnchor("https://zvezdochet.guru");
	        p.add(chunk);
		} catch (Exception e) {
		    e.printStackTrace();
		}
        return p;
	}

	/**
	 * Поиск базового шрифта для кириллицы
	 * @return BaseFont базовый шрифт
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static BaseFont getBaseFont() throws DocumentException, IOException {
		return BaseFont.createFont("/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-R.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	}

	/**
	 * Поиск шрифта для обычного текста
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 */
	public static Font getRegularFont(BaseFont baseFont) {
		return new Font(baseFont, 12, Font.NORMAL);
	}

	/**
	 * Поиск шрифта для гиперссылки
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 */
	public static Font getLinkFont(BaseFont baseFont) {
		return new Font(baseFont, 12, Font.UNDERLINE, new BaseColor(102, 102, 153));
	}

	/**
	 * Поиск шрифта для подзаголовка
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 */
	public static Font getHeaderFont(BaseFont baseFont) {
		return new Font(baseFont, 14, Font.BOLD, new BaseColor(102, 102, 153));
	}

	/**
	 * Поиск метаданных по умолчанию
	 * @param doc документ
	 * @param title название документа
	 */
	public static void getMetaData(Document doc, String title) {
        doc.addTitle(title);
        doc.addSubject("Астрологический сервис Звездочёт");
        doc.addKeywords("гороскоп, звездочёт, сидерическая астрология");
        doc.addAuthor("Наталья Диденко");
        doc.addCreator("Наталья Диденко");
        doc.addCreationDate();
	}

    /**
     * Печать заголовка документа
     * @param p абзац-контейнер
     * @param text текст
     * @param baseFont базовый шрифт
     * http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java
     */
	public static void printHeader(Paragraph p, String text, BaseFont baseFont) {
		try {
			Font fonth3w = new Font(baseFont, 10, Font.BOLD, new BaseColor(255, 255, 255));

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(1);
//            table.setPadding(4);
            // t.setSpacing(4);
            // t.setBorderWidth(1);

            PdfPCell cell = new PdfPCell(new Phrase(text, fonth3w));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new BaseColor(153, 153, 204));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(5);
            table.addCell(cell);
            p.add(table);
			
//	        Chunk chunk = new Chunk(text, fonth3w);
//	        chunk.setBackground(new BaseColor(153, 153, 204));
//	        chunk.setLineHeight(14);
//	        Paragraph p = new Paragraph();
//	        p.setAlignment(Element.ALIGN_CENTER);
//	//        p.setSpacingBefore(1);
//	//        p.setSpacingAfter(1);
//	        p.setLeading(0, 4);
//	        p.add(chunk);
//	//        p.setIndentationLeft(5);
//	//        p.setIndentationRight(5);
//			doc.add(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Печать секции
	 * @param chapter раздел документа
	 * @param title наименование секции
	 * @param baseFont базовый шрифт
	 * @return секция
	 */
	public static Section printSection(Chapter chapter, String title, BaseFont baseFont) {
		Font fonth3 = new Font(baseFont, 16, Font.BOLD, new BaseColor(102, 102, 153));
		Paragraph p = new Paragraph(title, fonth3);
		p.setSpacingBefore(10);
		p.add(Chunk.NEWLINE);
		printHr(p);
		return chapter.addSection(p);
	}

	/**
	 * Печать разделителя
	 * @param p абзац
	 */
	private static void printHr(Paragraph p) {
		p.add(new Chunk(new LineSeparator(2, 100, new BaseColor(102, 102, 153), Element.ALIGN_CENTER, 0)));	
	}

	/**
	 * Конвертация HTML-цвета в базовый
	 * @param htmlColor код HTML-цвета
	 * @return базовый цвет
	 */
	public static BaseColor htmlColor2Base(String htmlColor) {
		BaseColor color = BaseColor.BLACK;
		if (htmlColor.equals("maroon"))
			color = BaseColor.RED;
		else if (htmlColor.equals("teal"))
			color = BaseColor.GREEN;
		else if (htmlColor.equals("purple"))
			color = BaseColor.MAGENTA;
		else if (htmlColor.equals("navy"))
			color = BaseColor.BLUE;
		return color;
	}

	/**
	 * Поиск заголовка для гендерного текста
	 * @param type тип толкования
	 * @return заголовок
	 */
	public static String getGenderHeader(String type) {
	    if (type.equals("male"))
	    	return "Мужчина";
	    else if (type.equals("female"))
	    	return "Женщина";
	    else if (type.equals("child"))
	    	return "Ребёнок";
	    else
	    	return "";
	}

	/**
	 * Поиск каталога размещения шрифтов
	 * @return путь к каталогу
	 */
	public static String getFontDir() {
		return "/usr/share/fonts/truetype/ubuntu-font-family";
	}

	/**
	 * Возвращает наименование используемого шрифта
	 * @return наименование шрифта
	 */
	public static String getFontName() {
		return "Ubuntu";
	}

	/**
	 * Генерация круговой диаграммы
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 */
	public static Image printPie(PdfWriter writer, String title, Bar[] bars, float width, float height) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultPieDataset dataset = new DefaultPieDataset();
			for (Bar bar : bars)
				dataset.setValue(bar.getName(), bar.getValue());

		    JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            PiePlot plot = (PiePlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(204, 204, 255));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            chart.getLegend().setItemFont(sfont);

            for (Bar bar : bars) {
            	Color color = bar.getColor();
            	plot.setSectionPaint(bar.getName(), new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
            	plot.setLabelFont(sfont);
            }
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация диаграмм знаков
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 */
	public static Image printChart(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, float width, float height) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (Bar bar : bars)
				dataset.setValue(bar.getValue(), bar.getCategory(), bar.getName());

		    JFreeChart chart = ChartFactory.createBarChart(title, cattitle, valtitle, dataset);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(204, 204, 255));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            chart.getLegend().setItemFont(sfont);

            ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());
            BarRenderer renderer = (BarRenderer)chart.getCategoryPlot().getRenderer();
            int i = -1;
            for (Bar bar : bars) {
            	Color color = bar.getColor();
            	renderer.setSeriesPaint(++i, new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
            }
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * @param colnum число колонок таблицы диаграммы
	 * @param bars массив категорий диаграммы
	 * @param title наименование диаграммы
	 * @return тег диаграммы
	 */
	public static PdfPTable printTableChart(int colnum, Bar[] bars, String title, BaseFont baseFont) {
//		Tag div = new Tag("div", "class=chart style=height:" + (40 * bars.length) + "px");
        PdfPTable table = new PdfPTable(colnum + 1);
//        table.setWidthPercentage(100);
        table.setSpacingBefore(1);
//        table.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);
        table.setSummary(title);

        Font font = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
		for (Bar bar : bars) {
			double val = bar.getValue();
			int value = (int)(val * 2);
			//элемент диаграммы
	        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(val), font));
	        cell.setColspan(value);
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        Color color = bar.getColor();
	        cell.setBackgroundColor(new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
	        cell.setBorder(PdfPCell.NO_BORDER);
	        cell.setPadding(5);
	        table.addCell(cell);

			//числовое значение
	        cell = new PdfPCell();
	        cell.setColspan(colnum - value);
	        table.addCell(cell);

			//подпись
	        cell = new PdfPCell(new Phrase(bar.getName(), font));
	        table.addCell(cell);
		}
		return table;
	}
}