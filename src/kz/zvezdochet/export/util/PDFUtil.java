package kz.zvezdochet.export.util;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;

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
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

import kz.zvezdochet.core.bean.ITextGender;
import kz.zvezdochet.core.bean.TextGender;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.core.util.StringUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.bean.Bar;

/**
 * Набор утилит для pdf-экспорта
 * @author Nataly Didenko
 *
 */
public class PDFUtil {
	/**
	 * Каталог размещения обычных шрифтов
	 */
	public static String FONTDIR = "/usr/share/fonts/truetype/freefont";

	/**
	 * Наименование обычного шрифта
	 */
	public static String FONTFILE = "FreeSans.ttf";
	/**
	 * Наименование символьного шрифта
	 */
	public static String FONTSYMBOLFILE = "Cardo-Regular.ttf";

	/**
	 * Цвет заголовков
	 */
	public static BaseColor FONTCOLORH = new BaseColor(51, 51, 102);
	/**
	 * Цвет ссылок и подзаголовков
	 */
	public static BaseColor FONTCOLOR = new BaseColor(102, 102, 153);
	/**
	 * Цвет примечаний
	 */
	public static BaseColor FONTCOLORGRAY = new BaseColor(102, 102, 102);


	/**
	 * Отображение информации о копирайте
	 * @param baseFont базовый шрифт
	 * @return Paragraph абзац
	 */
	public static Paragraph printCopyright() {
        Paragraph p = new Paragraph();
		try {
			BaseFont baseFont = getBaseFont();
			Font font = new Font(baseFont, 10, Font.NORMAL);
			Font fonta = new Font(baseFont, 10, Font.UNDERLINE, FONTCOLOR);

	        p.setAlignment(Element.ALIGN_CENTER);
	        Chunk chunk = new Chunk("© 1998-" + Calendar.getInstance().get(Calendar.YEAR) + " Астрологический сервис ", font);
	        p.add(chunk);
	        chunk = new Chunk("Звездочёт", fonta);
	        chunk.setAnchor(WEBSITE);
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
		return BaseFont.createFont(FONTDIR + "/" + FONTFILE, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	}

	/**
	 * Поиск шрифта для обычного текста
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getRegularFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL);
	}

	/**
	 * Поиск шрифта для гиперссылки
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getLinkFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.UNDERLINE, FONTCOLOR);
	}

	/**
	 * Поиск шрифта для подзаголовка
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getHeaderFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 14, Font.BOLD, FONTCOLOR);
	}

	public static String AUTHOR = "Наталья Диденко";
	public static String WEBSITE = "https://zvezdochet.guru";

	/**
	 * Поиск метаданных по умолчанию
	 * @param doc документ
	 * @param title название документа
	 */
	public static void getMetaData(Document doc, String title) {
        doc.addTitle(title);
        doc.addSubject("Астрологический сервис Звездочёт");
        doc.addKeywords("гороскоп, звездочёт, сидерическая астрология");
        doc.addAuthor(AUTHOR);
        doc.addCreator(AUTHOR);
        doc.addCreationDate();
	}

    /**
     * Печать заголовка документа
     * @param p абзац-контейнер
     * @param text текст
     * @param baseFont базовый шрифт
     * http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java
     */
	public static void printHeader(Paragraph p, String text) {
		try {
			BaseFont baseFont = getBaseFont();
			Font font = new Font(baseFont, 18, Font.BOLD, FONTCOLORH);
	        p.setAlignment(Element.ALIGN_CENTER);
			p.add(new Phrase(text, font));
			p.add(Chunk.NEWLINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Печать секции
	 * @param chapter раздел документа
	 * @param title наименование секции
	 * @return секция
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Section printSection(Chapter chapter, String title) throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		Font fonth3 = new Font(baseFont, 16, Font.BOLD, FONTCOLORH);
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
		p.add(new Chunk(new LineSeparator(2, 100, FONTCOLORH, Element.ALIGN_CENTER, 0)));	
	}

	/**
	 * Конвертация HTML-цвета в базовый
	 * @param htmlColor код HTML-цвета
	 * @return базовый цвет
	 */
	public static BaseColor htmlColor2Base(String htmlColor) {
		BaseColor color = BaseColor.BLACK;
		if (htmlColor.equals("maroon"))
			color = new BaseColor(102, 0, 51);
		else if (htmlColor.equals("teal"))
			color = new BaseColor(0, 102, 102);
		else if (htmlColor.equals("purple"))
			color = new BaseColor(204, 102, 51);
		else if (htmlColor.equals("navy"))
			color = new BaseColor(0, 51, 102);
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
	    else if (type.equals("health"))
	    	return "Здоровье";
	    else
	    	return "";
	}

	/**
	 * Возвращает наименование обычного шрифта
	 * @return наименование шрифта
	 */
	public static String getFontName() {
		return "FreeSans";
	}

	/**
	 * Возвращает наименование символьного шрифта
	 * @return наименование шрифта
	 */
	public static String getFontSymbolName() {
		return "Cardo-Regular";
	}

	/**
	 * Генерация круговой диаграммы
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printPie(PdfWriter writer, String title, Bar[] bars, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
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
				if (bar != null)
					dataset.setValue(bar.getName(), bar.getValue());

		    JFreeChart chart = ChartFactory.createPieChart(title, dataset, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            PiePlot plot = (PiePlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            if (legend)
            	chart.getLegend().setItemFont(sfont);

            for (Bar bar : bars) {
            	if (null == bar)
            		continue;
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
	 * Генерация столбцовых диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @param vertical true|false вертикальная|горизонтальная диаграмма
	 * @return изображение диаграммы
	 */
	public static Image printBars(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, 
			float width, float height, boolean legend, boolean vertical) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
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

			PlotOrientation orientation = vertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
		    JFreeChart chart = ChartFactory.createBarChart(title, cattitle, valtitle, dataset, orientation, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setTickLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);

            BarRenderer renderer = (BarRenderer)plot.getRenderer();
            renderer.setBarPainter(new StandardBarPainter());
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
	 * @param writer обработчик генерации PDF-файла
	 * @param maxval максимальное число значения
	 * @param bars массив категорий диаграммы
	 * @param title наименование диаграммы
	 * @param baseFont базовый шрифт
	 * @return табличная диаграмма
	 */
	public static PdfPTable printTableChart(PdfWriter writer, double maxval, Bar[] bars, String title) {
        PdfPTable table = new PdfPTable(3);
        float height = 20;
        float factor = 14;
        float maxvalue = (float)maxval * factor;
        try {
	        table.setWidths(new float[] { maxvalue, 20, 120 });
	        table.setSpacingBefore(10);
	        table.setSummary(title);
			BaseFont baseFont = getBaseFont();
	        Font font = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
			for (Bar bar : bars) {
				double val = bar.getValue();
				//элемент диаграммы
		        PdfTemplate tpl = writer.getDirectContent().createTemplate(maxvalue, height);
		        Color color = bar.getColor();
		        tpl.setColorFill(new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
		        tpl.rectangle(0, 0, (float)val * factor, height);
		        tpl.fill();
		        writer.releaseTemplate(tpl);
		        PdfPCell cell = new PdfPCell(Image.getInstance(tpl));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setPadding(2);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
	
				//числовое значение
		        cell = new PdfPCell(new Phrase(String.valueOf(val), font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
	
				//подпись
		        cell = new PdfPCell(new Phrase(bar.getName(), font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * Конвертация HTML-кода в PDF-текст
	 * @param html HTML-код
	 * @return фраза с текстом
	 */
	public static Phrase html2pdf(String html) {
	    Phrase phrase = new Phrase();
		try {
			//преобразуем html-абзацы в html-блоки
			//чтобы pdf-абзацы выглядели раздельно, а не слитно.
			//если этого не сделать, абзацы будут выполнять роль span, а не p
			html = html.replace("<p>", "<div>")
				.replace("</p>", "</div>");

			InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
			FileInputStream fis = new FileInputStream(PlatformUtil.getPath(kz.zvezdochet.export.Activator.PLUGIN_ID, "/export.css").getPath());
		    HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
            ElementList elements = new ElementList();
            ElementHandlerPipeline pdf = new ElementHandlerPipeline(elements, null);
            HtmlPipeline pipeline = new HtmlPipeline(htmlContext, pdf);
			CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
			CssFile cssFile = XMLWorkerHelper.getCSS(fis);
			cssResolver.addCss(cssFile);
			CssResolverPipeline css = new CssResolverPipeline(cssResolver, pipeline);
			XMLWorker worker = new XMLWorker(css, true);
		    XMLParser p = new XMLParser(worker);
		    p.parse(is, Charset.forName("UTF-8"));
		    phrase.setFont(getRegularFont());
		    phrase.addAll(elements);
		} catch (Exception e) {
			System.out.println(html);
			e.printStackTrace();
		}
		return phrase;
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * Генерация диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printStackChart(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
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
				if (bar != null)
					dataset.setValue(bar.getValue(), bar.getCategory(), bar.getName());

		    JFreeChart chart = ChartFactory.createStackedBarChart(title, cattitle, valtitle, dataset, PlotOrientation.HORIZONTAL, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setTickLabelFont(sfont);
            plot.getRangeAxis().setTickLabelsVisible(false);

            if (legend)
            	chart.getLegend().setItemFont(sfont);

            BarRenderer renderer = (BarRenderer)plot.getRenderer();
            renderer.setBarPainter(new StandardBarPainter());
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация линейных диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printGraphics(PdfWriter writer, String title, String cattitle, String valtitle, XYSeriesCollection dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createXYLineChart(title, cattitle, valtitle, dataset, PlotOrientation.VERTICAL, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);

            XYPlot plot = (XYPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setLabelFont(sfont);
            plot.getRangeAxis().setLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);
            else
            	chart.getLegend().setVisible(false);

//            BarRenderer renderer = (BarRenderer)plot.getRenderer();
//            renderer.setBarPainter(new StandardBarPainter());
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация гендерного толкования
	 * @param section подраздел
	 * @param dict справочник
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void printGender(Section section, ITextGender dict, boolean female, boolean child) throws DocumentException, IOException {
		if (dict != null) {
			List<TextGender> genders = dict.getGenderTexts(female, child);
			for (TextGender gender : genders) {
				Paragraph p = new Paragraph(PDFUtil.getGenderHeader(gender.getType()), getHeaderFont());
				p.setSpacingBefore(10);
				section.add(p);
				section.add(new Paragraph(StringUtil.removeTags(gender.getText()), getRegularFont()));
			};
			section.add(Chunk.NEWLINE);
		}
	}

	/**
	 * Генерация временных диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printTimeChart(PdfWriter writer, String title, String cattitle, String valtitle, TimeSeriesCollection dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createTimeSeriesChart(title, cattitle, valtitle, dataset, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            XYPlot plot = (XYPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setLabelFont(sfont);
            plot.getRangeAxis().setLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);
            else
            	chart.getLegend().setVisible(false);

            DateAxis axis = (DateAxis)plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
            axis.setAutoTickUnitSelection(false);
            axis.setVerticalTickLabels(true);

            final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
            int scnt = dataset.getSeries().size();
            for (int i = 0; i < scnt; i++)
            	renderer.setSeriesStroke(i, new BasicStroke(3));

			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Поиск базового шрифта для астрологических символов
	 * @return BaseFont базовый шрифт
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static BaseFont getAstroFont() throws DocumentException, IOException {
		String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/font/Cardo-Regular.ttf").getPath();
		return BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	}

	/**
	 * Поиск шрифта для подзаголовка
	 * @param baseFont базовый шрифт
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getHeaderAstroFont() throws DocumentException, IOException {
		BaseFont baseFont = getAstroFont();
		return new Font(baseFont, 14, Font.NORMAL, FONTCOLOR);
	}

	/**
	 * Поиск базового шрифта для спецсимволов
	 * @return BaseFont базовый шрифт
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static Font getHeaderSymbolFont() throws DocumentException, IOException {
		return new Font(Font.getFamily(BaseFont.SYMBOL), 14, Font.NORMAL, FONTCOLOR);
	}
}
